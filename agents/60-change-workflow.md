# Change Workflow

## Working rules

- Prefer reading `code-index.json` before broad exploration.
- Open the smallest useful set of files.
- Keep changes aligned with the current layered architecture.
- Prefer constructor injection and stateless components.
- Prefer immutable data structures.
- Use Arrow `Either<String, T>` for recoverable failures.
- Prefer editing existing files over creating new ones.
- Rebuild the code index after structural source changes.

## Recommended change sequence

1. locate symbols in `code-index.json`
2. inspect `Main.kt` or `InterpreterFactory.kt` if control flow or wiring matters
3. open only the directly relevant command, interpreter, service, or DAO files
4. implement the change
5. run the smallest meaningful validation command
6. rely on the repository-managed pre-commit hook in `.githooks/pre-commit` to refresh `code-index.json` when indexed inputs are staged
7. update the relevant `agents/` section if workflow or structure changed when the hook flags a docs-sensitive change

## Hook setup

This repository keeps the hook script in `.githooks/pre-commit`.
Configure Git once per clone so commits use the repository-managed hooks:

```bash
git config core.hooksPath .githooks
```

The pre-commit hook currently does two things:

- regenerates and stages `code-index.json` when indexed source or build inputs are staged
- blocks commits that touch docs-sensitive architecture or workflow areas without a matching update to `AGENTS.md` or `agents/*.md`

## Maintenance convention for docs

This folder uses numeric prefixes in steps of ten.
That leaves room to insert future sections without renaming everything.
Keep `AGENTS.md` short, navigational, and task-first.
