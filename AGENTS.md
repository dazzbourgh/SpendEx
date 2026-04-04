# AGENTS.md

This file is the entrypoint and table of contents for agent-focused repository documentation.
Use it as a task-first navigation page, then open the smallest linked section needed.

## Recommended reading order

1. [Start Here](agents/10-start-here.md)
   - fastest orientation, main entrypoints, and where to look first
2. [Build, Run, and Validate](agents/20-build-run-validate.md)
   - build, compile, format, run, install, and validation commands
3. [Architecture Map](agents/30-architecture-map.md)
   - layered structure, composition root, runtime path, and key code areas
4. [AI Code Index](agents/40-code-index.md)
   - how to use and regenerate `code-index.json`
5. [Data and Storage](agents/50-data-storage.md)
   - local persistence behavior and the files that own it
6. [Change Workflow](agents/60-change-workflow.md)
   - implementation sequence, repo rules, and doc maintenance conventions

## Task-first shortcuts

- Need symbol lookup or fast navigation? Open [AI Code Index](agents/40-code-index.md).
- Need to understand wiring or execution flow? Open [Architecture Map](agents/30-architecture-map.md).
- Need commands for build or validation? Open [Build, Run, and Validate](agents/20-build-run-validate.md).
- Need persistence details? Open [Data and Storage](agents/50-data-storage.md).
- Need the recommended edit process? Open [Change Workflow](agents/60-change-workflow.md).

## Maintenance rules

- Keep `AGENTS.md` short, stable, and navigational.
- Put durable details in `agents/` section files.
- Use numeric prefixes in steps of ten so new sections can be inserted later.
- Rebuild `code-index.json` after structural source changes.
- Update only the section files affected by a workflow or architecture change.
