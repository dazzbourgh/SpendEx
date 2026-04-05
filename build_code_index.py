#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import subprocess
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parent
OUTPUT_PATH = ROOT / "code-index.json"
SELF_FILE = Path(__file__).name
INDEXABLE_EXTENSIONS = {".kt", ".kts", ".properties", ".xml", ".yml", ".sh"}
KOTLIN_EXTENSIONS = {".kt", ".kts"}
CONTAINER_KINDS = {"class", "data class", "enum class", "sealed class", "annotation class", "value class", "interface", "object"}
CONTINUATION_PREFIXES = (
    ".",
    "?.",
    "?:",
    "!!",
    "&&",
    "||",
    "+",
    "-",
    "*",
    "/",
    "%",
    "as ",
    "is ",
)
CONTINUATION_SUFFIXES = (
    ".",
    "?.",
    "?:",
    ",",
    "(",
    "[",
    "{",
    "=",
    "->",
    "&&",
    "||",
    "+",
    "-",
    "*",
    "/",
    "%",
)
DECLARATION_RE = re.compile(
    r"^\s*(?P<modifiers>(?:(?:private|public|internal|protected|override|suspend|tailrec|inline|operator|infix|expect|actual|open|abstract|final|sealed|data|enum|annotation|const|lateinit|value|external)\s+)*)"
    r"(?P<kind>class|interface|object|fun|typealias)\s+"
    r"(?:<[^>]+>\s*)?"
    r"(?:(?:[A-Za-z_][\w<>?, ]*)\.)?"
    r"(?P<name>[A-Za-z_][A-Za-z0-9_]*)"
)
PACKAGE_RE = re.compile(r"^\s*package\s+([A-Za-z0-9_.]+)")
IMPORT_RE = re.compile(r"^\s*import\s+([A-Za-z0-9_.*]+)")
VISIBILITY_RE = re.compile(r"\b(private|public|internal|protected)\b")
TOP_LEVEL_FILE_NAMES = {"build.gradle.kts", "settings.gradle.kts", "gradle.properties", "detekt.yml", "install.sh"}
NOISE_REFERENCE_NAMES = {"run"}


@dataclass
class LexState:
    in_block_comment: bool = False
    in_string: bool = False
    in_char: bool = False
    in_triple_string: bool = False


@dataclass
class SymbolRecord:
    id: str
    name: str
    simple_name: str
    kind: str
    package: str
    container_id: str | None
    file: str
    source_set: str
    visibility: str
    modifiers: list[str]
    is_expect: bool
    is_actual: bool
    declaration_start_line: int
    declaration_end_line: int
    body_start_line: int | None
    body_end_line: int | None
    span_kind: str
    start_line: int
    end_line: int
    signature: str
    imports: list[str]
    dependencies: list[str]
    references: list[dict[str, Any]]


class KotlinFileIndex:
    def __init__(self, path: Path) -> None:
        self.path = path
        self.relative_path = path.relative_to(ROOT).as_posix()
        self.content = path.read_text(encoding="utf-8")
        self.lines = self.content.splitlines()
        self.sanitized_lines = sanitize_lines(self.lines)
        self.package = self._parse_package()
        self.imports = self._parse_imports()
        self.source_set = detect_source_set(path)
        self.symbols: list[dict[str, Any]] = self._parse_symbols()

    def _parse_package(self) -> str:
        for line in self.lines:
            match = PACKAGE_RE.match(line)
            if match:
                return match.group(1)
        return ""

    def _parse_imports(self) -> list[str]:
        imports: list[str] = []
        for line in self.lines:
            match = IMPORT_RE.match(line)
            if match:
                imports.append(match.group(1))
        return imports

    def _parse_symbols(self) -> list[dict[str, Any]]:
        raw_symbols: list[dict[str, Any]] = []
        for index, line in enumerate(self.lines):
            match = DECLARATION_RE.match(line)
            if not match:
                continue

            modifiers = [modifier for modifier in match.group("modifiers").split() if modifier]
            kind = normalize_kind(match.group("kind"), modifiers)
            name = match.group("name")
            signature, signature_end_line, body_style = collect_signature(self.lines, index)
            spans = calculate_symbol_spans(
                lines=self.lines,
                start_index=index,
                signature_end_line=signature_end_line,
                body_style=body_style,
            )

            raw_symbols.append(
                {
                    "name": name,
                    "simple_name": name,
                    "kind": kind,
                    "package": self.package,
                    "container_id": None,
                    "file": self.relative_path,
                    "source_set": self.source_set,
                    "visibility": parse_visibility(modifiers),
                    "modifiers": modifiers,
                    "is_expect": "expect" in modifiers,
                    "is_actual": "actual" in modifiers,
                    "declaration_start_line": spans["declaration_start_line"],
                    "declaration_end_line": spans["declaration_end_line"],
                    "body_start_line": spans["body_start_line"],
                    "body_end_line": spans["body_end_line"],
                    "span_kind": spans["span_kind"],
                    "start_line": spans["start_line"],
                    "end_line": spans["end_line"],
                    "signature": signature,
                    "imports": self.imports,
                    "dependencies": [],
                    "references": [],
                    "_sort_span": spans["end_line"] - spans["start_line"],
                    "_is_container": kind in CONTAINER_KINDS,
                }
            )

        containers = [symbol for symbol in raw_symbols if symbol["_is_container"]]
        containers.sort(key=lambda symbol: (symbol["start_line"], symbol["_sort_span"]))

        for symbol in raw_symbols:
            containing = [
                container
                for container in containers
                if container is not symbol and container["start_line"] <= symbol["start_line"] <= container["end_line"]
            ]
            if containing:
                containing.sort(key=lambda candidate: (candidate["_sort_span"], candidate["start_line"]))
                container = containing[0]
                symbol["container_id"] = build_symbol_id(
                    package=container["package"],
                    container_id=container["container_id"],
                    name=container["name"],
                    line=container["start_line"],
                )

            symbol["id"] = build_symbol_id(
                package=symbol["package"],
                container_id=symbol["container_id"],
                name=symbol["name"],
                line=symbol["start_line"],
            )

        for symbol in raw_symbols:
            symbol["dependencies"] = []

        return raw_symbols


def normalize_kind(kind: str, modifiers: list[str]) -> str:
    if kind == "class":
        if "enum" in modifiers:
            return "enum class"
        if "data" in modifiers:
            return "data class"
        if "sealed" in modifiers:
            return "sealed class"
        if "annotation" in modifiers:
            return "annotation class"
        if "value" in modifiers:
            return "value class"
    return kind


def parse_visibility(modifiers: list[str]) -> str:
    for modifier in modifiers:
        if VISIBILITY_RE.fullmatch(modifier):
            return modifier
    return "public"


def detect_source_set(path: Path) -> str:
    parts = path.relative_to(ROOT).parts
    if len(parts) >= 2 and parts[0] == "src":
        return parts[1]
    return "project"


def collect_signature(lines: list[str], start_index: int, max_lines: int = 16) -> tuple[str, int, str | None]:
    collected: list[str] = []
    state = LexState()
    paren_depth = 0
    bracket_depth = 0
    brace_depth = 0

    for index in range(start_index, min(len(lines), start_index + max_lines)):
        line = lines[index].rstrip()
        stripped = line.strip()
        if index > start_index and DECLARATION_RE.match(line):
            break
        collected.append(stripped)

        clean, state = sanitize_line(line, state)
        terminator, paren_depth, bracket_depth, brace_depth = scan_signature_line(
            clean,
            paren_depth,
            bracket_depth,
            brace_depth,
        )
        if terminator is not None:
            body_style = "block" if terminator == "{" else "expression"
            return collapse_whitespace(" ".join(collected)), index + 1, body_style

        if paren_depth <= 0 and bracket_depth <= 0 and brace_depth <= 0 and stripped and not stripped.endswith((",", "(", ":")):
            next_non_empty = next_non_empty_line(lines, index + 1)
            if next_non_empty is None or not next_non_empty.startswith((":", "{", "=")):
                return collapse_whitespace(" ".join(collected)), index + 1, None

    return collapse_whitespace(" ".join(collected)), min(len(lines), start_index + max_lines), None


def scan_signature_line(
    clean: str,
    paren_depth: int,
    bracket_depth: int,
    brace_depth: int,
) -> tuple[str | None, int, int, int]:
    for char in clean:
        at_top_level = paren_depth == 0 and bracket_depth == 0 and brace_depth == 0
        if at_top_level and char in {"=", "{"}:
            return char, paren_depth, bracket_depth, brace_depth

        if char == "(":
            paren_depth += 1
        elif char == ")" and paren_depth > 0:
            paren_depth -= 1
        elif char == "[":
            bracket_depth += 1
        elif char == "]" and bracket_depth > 0:
            bracket_depth -= 1
        elif char == "{":
            brace_depth += 1
        elif char == "}" and brace_depth > 0:
            brace_depth -= 1

    return None, paren_depth, bracket_depth, brace_depth


def calculate_symbol_spans(
    lines: list[str],
    start_index: int,
    signature_end_line: int,
    body_style: str | None,
) -> dict[str, Any]:
    declaration_start_line = start_index + 1
    declaration_end_line = signature_end_line
    body_range: tuple[int, int] | None = None
    span_kind = "declaration_only"

    if body_style == "block":
        body_range = find_body_range(lines, start_index)
        span_kind = "block_body" if body_range is not None else "declaration_only"
    elif body_style == "expression":
        body_range = find_expression_range(lines, signature_end_line - 1)
        span_kind = "expression_body" if body_range is not None else "declaration_only"

    return {
        "declaration_start_line": declaration_start_line,
        "declaration_end_line": declaration_end_line,
        "body_start_line": body_range[0] if body_range is not None else None,
        "body_end_line": body_range[1] if body_range is not None else None,
        "span_kind": span_kind,
        "start_line": declaration_start_line,
        "end_line": body_range[1] if body_range is not None else declaration_end_line,
    }


def find_expression_range(lines: list[str], start_index: int) -> tuple[int, int] | None:
    state = LexState()
    paren_depth = 0
    bracket_depth = 0
    brace_depth = 0
    body_start_line = None
    body_end_line = None

    for index in range(start_index, len(lines)):
        raw_line = lines[index]
        if body_start_line is not None and paren_depth == 0 and bracket_depth == 0 and brace_depth == 0 and is_expression_boundary_line(raw_line):
            return body_start_line, body_end_line or index

        clean, state = sanitize_line(raw_line, state)
        if index == start_index:
            clean = text_after_top_level_equals(clean)
        stripped = clean.strip()
        if body_start_line is None and stripped:
            body_start_line = index + 1

        paren_depth, bracket_depth, brace_depth = update_delimiter_depths(
            clean,
            paren_depth,
            bracket_depth,
            brace_depth,
        )
        if stripped:
            body_end_line = index + 1

        if body_start_line is not None and paren_depth == 0 and bracket_depth == 0 and brace_depth == 0:
            next_line = next_non_empty_line(lines, index + 1)
            if expression_can_end(stripped, next_line):
                return body_start_line, body_end_line or (index + 1)

    return None if body_start_line is None else (body_start_line, body_end_line or len(lines))


def text_after_top_level_equals(clean: str) -> str:
    paren_depth = 0
    bracket_depth = 0
    brace_depth = 0

    for index, char in enumerate(clean):
        at_top_level = paren_depth == 0 and bracket_depth == 0 and brace_depth == 0
        if at_top_level and char == "=":
            return clean[index + 1 :]

        if char == "(":
            paren_depth += 1
        elif char == ")" and paren_depth > 0:
            paren_depth -= 1
        elif char == "[":
            bracket_depth += 1
        elif char == "]" and bracket_depth > 0:
            bracket_depth -= 1
        elif char == "{":
            brace_depth += 1
        elif char == "}" and brace_depth > 0:
            brace_depth -= 1

    return clean


def update_delimiter_depths(
    clean: str,
    paren_depth: int,
    bracket_depth: int,
    brace_depth: int,
) -> tuple[int, int, int]:
    for char in clean:
        if char == "(":
            paren_depth += 1
        elif char == ")" and paren_depth > 0:
            paren_depth -= 1
        elif char == "[":
            bracket_depth += 1
        elif char == "]" and bracket_depth > 0:
            bracket_depth -= 1
        elif char == "{":
            brace_depth += 1
        elif char == "}" and brace_depth > 0:
            brace_depth -= 1
    return paren_depth, bracket_depth, brace_depth


def is_expression_boundary_line(line: str) -> bool:
    stripped = line.strip()
    return bool(stripped.startswith("@") or stripped.startswith("}") or DECLARATION_RE.match(line))


def expression_can_end(current_line: str, next_line: str | None) -> bool:
    if not current_line:
        return False
    if current_line.endswith(CONTINUATION_SUFFIXES):
        return False
    if next_line is None:
        return True

    next_stripped = next_line.strip()
    if not next_stripped:
        return True
    if next_stripped.startswith(CONTINUATION_PREFIXES):
        return False
    if next_stripped.startswith(("@", "}")):
        return True
    if DECLARATION_RE.match(next_line):
        return True
    return False


def next_non_empty_line(lines: list[str], start_index: int) -> str | None:
    for index in range(start_index, len(lines)):
        stripped = lines[index].strip()
        if stripped:
            return stripped
    return None


def collapse_whitespace(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip()


def sanitize_lines(lines: list[str]) -> list[str]:
    state = LexState()
    sanitized: list[str] = []
    for line in lines:
        clean, state = sanitize_line(line, state)
        sanitized.append(clean)
    return sanitized


def sanitize_line(line: str, state: LexState) -> tuple[str, LexState]:
    result: list[str] = []
    index = 0
    length = len(line)
    next_state = LexState(
        in_block_comment=state.in_block_comment,
        in_string=state.in_string,
        in_char=state.in_char,
        in_triple_string=state.in_triple_string,
    )

    while index < length:
        if next_state.in_block_comment:
            end = line.find("*/", index)
            if end == -1:
                return "".join(result), next_state
            next_state.in_block_comment = False
            index = end + 2
            continue

        if next_state.in_triple_string:
            end = line.find('"""', index)
            if end == -1:
                return "".join(result), next_state
            next_state.in_triple_string = False
            index = end + 3
            continue

        if next_state.in_string:
            if line[index] == "\\":
                index += 2
                continue
            if line[index] == '"':
                next_state.in_string = False
            index += 1
            continue

        if next_state.in_char:
            if line[index] == "\\":
                index += 2
                continue
            if line[index] == "'":
                next_state.in_char = False
            index += 1
            continue

        if line.startswith("//", index):
            break
        if line.startswith("/*", index):
            next_state.in_block_comment = True
            index += 2
            continue
        if line.startswith('"""', index):
            next_state.in_triple_string = True
            index += 3
            continue
        if line[index] == '"':
            next_state.in_string = True
            index += 1
            continue
        if line[index] == "'":
            next_state.in_char = True
            index += 1
            continue

        result.append(line[index])
        index += 1

    return "".join(result), next_state


def find_body_range(lines: list[str], start_index: int) -> tuple[int, int] | None:
    state = LexState()
    brace_depth = 0
    body_started = False
    body_start_line = None

    for index in range(start_index, len(lines)):
        clean, state = sanitize_line(lines[index], state)
        for char in clean:
            if char == "{":
                brace_depth += 1
                if not body_started:
                    body_started = True
                    body_start_line = index + 1
            elif char == "}":
                if body_started:
                    brace_depth -= 1
                    if brace_depth == 0:
                        return body_start_line or (start_index + 1), index + 1

    return None if body_start_line is None else (body_start_line, len(lines))


def build_symbol_id(package: str, container_id: str | None, name: str, line: int) -> str:
    if container_id:
        return f"{container_id}.{name}@{line}"
    if package:
        return f"{package}.{name}@{line}"
    return f"<root>.{name}@{line}"


def gather_tracked_files() -> list[Path]:
    try:
        completed = subprocess.run(
            ["git", "ls-files"],
            cwd=ROOT,
            check=True,
            capture_output=True,
            text=True,
        )
        relative_paths = [Path(line) for line in completed.stdout.splitlines() if line.strip()]
        files = [ROOT / relative_path for relative_path in relative_paths if (ROOT / relative_path).is_file()]
    except (subprocess.CalledProcessError, FileNotFoundError):
        files = [path for path in ROOT.rglob("*") if path.is_file()]

    filtered: list[Path] = []
    for path in files:
        if path.name in {SELF_FILE, OUTPUT_PATH.name}:
            continue
        relative = path.relative_to(ROOT)
        if relative.parts and relative.parts[0] == "build":
            continue
        if path.suffix in INDEXABLE_EXTENSIONS or path.name in TOP_LEVEL_FILE_NAMES:
            filtered.append(path)
    return sorted(filtered)


def build_dependency_index(symbols: list[dict[str, Any]]) -> None:
    local_symbols_by_name: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for symbol in symbols:
        local_symbols_by_name[symbol["simple_name"]].append(symbol)

    for symbol in symbols:
        token_candidates = set(re.findall(r"\b[A-Z][A-Za-z0-9_]*\b", symbol["signature"]))
        token_candidates.discard(symbol["simple_name"])
        dependencies: list[str] = []
        for token in sorted(token_candidates):
            matches = local_symbols_by_name.get(token, [])
            for match in matches:
                if match["id"] == symbol["id"]:
                    continue
                if match["id"] not in dependencies:
                    dependencies.append(match["id"])
        symbol["dependencies"] = dependencies


def build_reference_index(symbols: list[dict[str, Any]], indexed_files: dict[str, KotlinFileIndex], extra_files: dict[str, list[str]]) -> None:
    searchable_files: dict[str, list[str]] = {}
    for path, kotlin_file in indexed_files.items():
        searchable_files[path] = kotlin_file.sanitized_lines
    searchable_files.update(extra_files)

    references_by_name: dict[str, list[dict[str, Any]]] = defaultdict(list)
    candidate_names = {
        symbol["simple_name"]
        for symbol in symbols
        if symbol["simple_name"] not in NOISE_REFERENCE_NAMES
    }

    for name in sorted(candidate_names, key=len, reverse=True):
        pattern = re.compile(rf"\b{re.escape(name)}\b")
        for file_path, lines in searchable_files.items():
            for line_number, line in enumerate(lines, start=1):
                if not line.strip():
                    continue
                if not pattern.search(line):
                    continue
                references_by_name[name].append(
                    {
                        "path": file_path,
                        "line": line_number,
                        "kind": "import" if line.strip().startswith("import ") else "usage",
                        "snippet": collapse_whitespace(line.strip())[:160],
                    }
                )

    for symbol in symbols:
        references = []
        for reference in references_by_name.get(symbol["simple_name"], []):
            if reference["path"] == symbol["file"] and reference["line"] == symbol["start_line"]:
                continue
            references.append(reference)
        symbol["references"] = references


def read_extra_text_files(paths: list[Path]) -> dict[str, list[str]]:
    extra_files: dict[str, list[str]] = {}
    for path in paths:
        if path.suffix in KOTLIN_EXTENSIONS:
            continue
        try:
            content = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        relative_path = path.relative_to(ROOT).as_posix()
        extra_files[relative_path] = [collapse_whitespace(line) for line in content.splitlines()]
    return extra_files


def build_expect_actual_pairs(symbols: list[dict[str, Any]]) -> list[dict[str, Any]]:
    by_key: dict[tuple[str, str], list[dict[str, Any]]] = defaultdict(list)
    for symbol in symbols:
        key = (symbol["package"], symbol["simple_name"])
        by_key[key].append(symbol)

    pairs: list[dict[str, Any]] = []
    for (package, simple_name), matches in sorted(by_key.items()):
        expect_symbols = [symbol for symbol in matches if symbol["is_expect"]]
        actual_symbols = [symbol for symbol in matches if symbol["is_actual"]]
        if expect_symbols and actual_symbols:
            pairs.append(
                {
                    "package": package,
                    "symbol": simple_name,
                    "expects": [location(symbol) for symbol in expect_symbols],
                    "actuals": [location(symbol) for symbol in actual_symbols],
                }
            )
    return pairs


def build_entry_points(files: dict[str, KotlinFileIndex], all_text_files: dict[str, list[str]]) -> list[dict[str, Any]]:
    entry_points: list[dict[str, Any]] = []
    main_symbol = next((symbol for file in files.values() for symbol in file.symbols if symbol["simple_name"] == "main"), None)
    if main_symbol is not None:
        entry_points.append(
            {
                "name": "main",
                "kind": "function",
                "location": location(main_symbol),
                "source": main_symbol["file"],
            }
        )

    build_file_lines = all_text_files.get("build.gradle.kts", [])
    for index, line in enumerate(build_file_lines, start=1):
        if "entryPoint" in line:
            entry_points.append(
                {
                    "name": "gradle-native-entrypoint",
                    "kind": "build-config",
                    "location": {"path": "build.gradle.kts", "line": index},
                    "source": "build.gradle.kts",
                    "snippet": line,
                }
            )
    return entry_points


def location(symbol: dict[str, Any]) -> dict[str, Any]:
    return range_payload(symbol["file"], symbol["start_line"], symbol["end_line"])


def range_payload(path: str, start_line: int, end_line: int) -> dict[str, Any]:
    return {"path": path, "line": start_line, "end_line": end_line}


def optional_range_payload(path: str, start_line: int | None, end_line: int | None) -> dict[str, Any] | None:
    if start_line is None or end_line is None:
        return None
    return range_payload(path, start_line, end_line)


def symbol_navigation_summary(symbol: dict[str, Any]) -> dict[str, Any]:
    return {
        "id": symbol["id"],
        "name": symbol["name"],
        "simple_name": symbol["simple_name"],
        "kind": symbol["kind"],
        "path": symbol["file"],
        "package": symbol["package"],
        "source_set": symbol["source_set"],
        "container_id": symbol["container_id"],
        "visibility": symbol["visibility"],
        "span_kind": symbol["span_kind"],
        "signature": symbol["signature"],
        "declaration": range_payload(
            symbol["file"],
            symbol["declaration_start_line"],
            symbol["declaration_end_line"],
        ),
        "body": optional_range_payload(
            symbol["file"],
            symbol["body_start_line"],
            symbol["body_end_line"],
        ),
        "span": range_payload(symbol["file"], symbol["start_line"], symbol["end_line"]),
        "dependency_count": len(symbol["dependencies"]),
        "reference_count": len(symbol["references"]),
    }


def build_lookup(symbols: list[dict[str, Any]], files: dict[str, KotlinFileIndex]) -> dict[str, Any]:
    by_id: dict[str, dict[str, Any]] = {}
    by_name: dict[str, list[dict[str, Any]]] = defaultdict(list)
    by_kind: dict[str, list[str]] = defaultdict(list)
    by_file: dict[str, dict[str, Any]] = {}
    by_package: dict[str, list[str]] = defaultdict(list)
    by_container: dict[str, list[dict[str, Any]]] = defaultdict(list)

    symbols_by_file: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for symbol in sorted(symbols, key=lambda item: (item["file"], item["start_line"], item["name"])):
        symbols_by_file[symbol["file"]].append(symbol)

    for symbol in sorted(symbols, key=lambda item: (item["simple_name"], item["file"], item["start_line"])):
        summary = symbol_navigation_summary(symbol)
        by_id[symbol["id"]] = summary
        by_name[symbol["simple_name"]].append(summary)
        by_kind[symbol["kind"]].append(symbol["id"])
        by_package[symbol["package"] or "<root>"] .append(symbol["id"])
        if symbol["container_id"] is not None:
            by_container[symbol["container_id"]].append(summary)

    for path, file_symbols in sorted(symbols_by_file.items()):
        sorted_symbols = sorted(file_symbols, key=lambda item: (item["start_line"], item["end_line"], item["name"]))
        top_level_symbols = [symbol for symbol in sorted_symbols if symbol["container_id"] is None]
        by_file[path] = {
            "symbol_ids": [symbol["id"] for symbol in sorted_symbols],
            "top_level_symbol_ids": [symbol["id"] for symbol in top_level_symbols],
            "symbols": [symbol_navigation_summary(symbol) for symbol in sorted_symbols],
            "top_level_symbols": [symbol_navigation_summary(symbol) for symbol in top_level_symbols],
        }

    return {
        "by_id": dict(sorted(by_id.items())),
        "by_name": dict(sorted(by_name.items())),
        "by_kind": {key: sorted(value) for key, value in sorted(by_kind.items())},
        "by_file": by_file,
        "by_package": {key: sorted(value) for key, value in sorted(by_package.items())},
        "by_container": {key: value for key, value in sorted(by_container.items())},
        "files": {
            path: {
                "package": file.package,
                "source_set": file.source_set,
                "imports": file.imports,
                "symbol_count": len(file.symbols),
                "top_level_symbol_count": len([symbol for symbol in file.symbols if symbol["container_id"] is None]),
            }
            for path, file in sorted(files.items())
        },
    }


def serializable_symbol(symbol: dict[str, Any]) -> dict[str, Any]:
    return {
        "id": symbol["id"],
        "name": symbol["name"],
        "simple_name": symbol["simple_name"],
        "kind": symbol["kind"],
        "package": symbol["package"],
        "container_id": symbol["container_id"],
        "file": symbol["file"],
        "source_set": symbol["source_set"],
        "visibility": symbol["visibility"],
        "modifiers": symbol["modifiers"],
        "is_expect": symbol["is_expect"],
        "is_actual": symbol["is_actual"],
        "span_kind": symbol["span_kind"],
        "declaration_start_line": symbol["declaration_start_line"],
        "declaration_end_line": symbol["declaration_end_line"],
        "body_start_line": symbol["body_start_line"],
        "body_end_line": symbol["body_end_line"],
        "start_line": symbol["start_line"],
        "end_line": symbol["end_line"],
        "signature": symbol["signature"],
        "imports": symbol["imports"],
        "dependencies": symbol["dependencies"],
        "reference_count": len(symbol["references"]),
        "references": symbol["references"],
    }


def build_index() -> dict[str, Any]:
    files = gather_tracked_files()
    kotlin_files = {path.relative_to(ROOT).as_posix(): KotlinFileIndex(path) for path in files if path.suffix in KOTLIN_EXTENSIONS}
    extra_text_files = read_extra_text_files(files)

    symbols = [symbol for file in kotlin_files.values() for symbol in file.symbols]
    build_dependency_index(symbols)
    build_reference_index(symbols, kotlin_files, extra_text_files)

    all_text_files = {**{path: file.lines for path, file in kotlin_files.items()}, **extra_text_files}
    index = {
        "meta": {
            "format": "ai-code-index",
            "version": 2,
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "project_root": ROOT.name,
            "indexed_file_count": len(files),
            "indexed_kotlin_file_count": len(kotlin_files),
            "symbol_count": len(symbols),
        },
        "entry_points": build_entry_points(kotlin_files, all_text_files),
        "expect_actual_pairs": build_expect_actual_pairs(symbols),
        "symbols": [serializable_symbol(symbol) for symbol in sorted(symbols, key=lambda item: (item["file"], item["start_line"], item["name"]))],
        "lookup": build_lookup(symbols, kotlin_files),
    }
    return index


def main() -> None:
    index = build_index()
    OUTPUT_PATH.write_text(json.dumps(index, indent=2, sort_keys=False) + "\n", encoding="utf-8")
    print(f"Wrote {OUTPUT_PATH.relative_to(ROOT).as_posix()} with {index['meta']['symbol_count']} symbols")


if __name__ == "__main__":
    main()
