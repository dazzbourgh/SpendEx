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
- `lookup.by_name` - fastest symbol lookup by simple name
- `lookup.by_file` - all indexed symbols in one file
- `lookup.by_package` - symbols grouped by package
- `lookup.by_kind` - symbols grouped by type such as class, interface, object, or function
- `symbols` - full records with signatures, dependencies, imports, and references

## Recommended lookup workflow

1. search `lookup.by_name` for the symbol you need
2. copy the returned `id`, `path`, and `line`
3. jump to the matching record in `symbols`
4. inspect `dependencies` for nearby related types
5. inspect `references` to find imports and usages
6. inspect `expect_actual_pairs` when the symbol crosses source sets

## Limitations

This is not a compiler-backed LSP.
Use it for fast symbol discovery and rough relationship mapping, then read the actual source files for exact behavior.
