# Start Here

SpendEx is a Kotlin Multiplatform CLI for managing financial accounts and transactions through provider-specific integrations.
The active runtime target is a native macOS ARM64 executable, with `main` as the runtime entrypoint.

## Fast orientation

If you are new to the repository, open files in this order:

1. `code-index.json` for fast symbol lookup
2. `src/commonMain/kotlin/Main.kt` for application startup
3. `src/commonMain/kotlin/interpreter/InterpreterFactory.kt` for dependency wiring
4. `src/commonMain/kotlin/command/` for CLI behavior
5. `src/commonMain/kotlin/provider/`, `plaid/`, `transaction/`, and `dao/` for provider wiring, business logic, and persistence

## Core facts

- CLI framework: Clikt
- error model: Arrow `Either<String, T>`
- HTTP and local OAuth server: Ktor
- serialization: `kotlinx.serialization`
- platform split: `commonMain` plus `macosArm64Main`

## Use this doc set

- build or validation commands: `20-build-run-validate.md`
- architecture and key files: `30-architecture-map.md`
- symbol lookup and navigation: `40-code-index.md`
- persistence behavior: `50-data-storage.md`
- change strategy and maintenance rules: `60-change-workflow.md`
