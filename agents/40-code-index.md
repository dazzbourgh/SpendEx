# AI Code Index

The repository includes a lightweight static code index intended as a small LSP-like lookup layer for agents.

## Files

- `build_code_index.py` - generator script
- `code-index.json` - generated machine-readable index

## When to use it

Start with the index before broad repository scans when you need to:

- find where a class or function is declared
- locate runtime or build entrypoints
- follow `expect` to `actual` mappings
- identify likely related types before opening files
- narrow the search space before deeper inspection

## Regenerate the index

```bash
python3 build_code_index.py
```

Regenerate it after:

- adding or renaming symbols
- moving files
- changing package structure
- changing `expect` or `actual` declarations
- making large refactors

## Most useful sections in `code-index.json`

- `meta` - index version and file and symbol counts
- `entry_points` - runtime and build entrypoints
- `expect_actual_pairs` - common-to-platform mappings
- `lookup.by_name` - fastest symbol lookup by simple name, now including declaration, body, and full span ranges
- `lookup.by_id` - direct symbol lookup when you already have an id
- `lookup.by_file` - all indexed symbols in one file, including top-level-only views
- `lookup.by_container` - symbols nested inside a class, object, or interface
- `lookup.by_package` - symbols grouped by package
- `lookup.by_kind` - symbols grouped by type such as class, interface, object, or function
- `lookup.files` - per-file metadata including package, source set, imports, and symbol counts
- `symbols` - full records with signatures, dependencies, imports, references, and explicit declaration/body/full-span line metadata

## Recommended lookup workflow

1. search `lookup.by_name` for the symbol you need
2. use the returned `span`, `declaration`, and `body` ranges to decide the smallest source slice worth reading
3. if you already know the symbol id, use `lookup.by_id` to jump straight to the same navigation summary
4. use `lookup.by_container` or `lookup.by_file.top_level_symbols` to inspect only nearby symbols instead of opening the full file
5. jump to the matching record in `symbols` only when you need richer metadata such as dependencies or references
6. inspect `expect_actual_pairs` when the symbol crosses source sets

## Span fields

Every indexed Kotlin symbol now exposes three navigation ranges:

- `declaration` - signature and declaration header only
- `body` - implementation body when one exists, including expression-bodied functions
- `span` - the full symbol extent used for precise reading

`symbols` also stores the raw scalar fields behind those ranges:

- `declaration_start_line`
- `declaration_end_line`
- `body_start_line`
- `body_end_line`
- `start_line`
- `end_line`
- `span_kind` - one of `declaration_only`, `block_body`, or `expression_body`

## Limitations

This is not a compiler-backed LSP.
Use it for fast symbol discovery and rough relationship mapping, then read the actual source files for exact behavior.
