# Architecture Map

## Layered flow

SpendEx follows this main flow:

1. `command/` parses CLI input with Clikt
2. `interpreter/` orchestrates command-facing business logic
3. feature modules like `account/`, `plaid/`, and `transaction/` implement domain behavior
4. `dao/` persists local state
5. `macosArm64Main/` provides platform-specific implementations

## Composition root

The dependency graph is assembled in `src/commonMain/kotlin/interpreter/InterpreterFactory.kt`.
This is the main place to inspect when you need to understand:

- service and DAO wiring
- interpreter construction
- environment-specific configuration
- platform adapter instantiation

## Runtime path

Application startup roughly follows this sequence:

1. `main(args)` extracts `--environment`
2. `InterpreterFactory.get(...)` builds the object graph
3. `RootCommand` attaches group and leaf commands
4. command classes delegate to interpreters
5. interpreters coordinate services and DAOs

## Highest-value code areas

- `src/commonMain/kotlin/Main.kt`
- `src/commonMain/kotlin/interpreter/`
- `src/commonMain/kotlin/command/`
- `src/commonMain/kotlin/plaid/`
- `src/commonMain/kotlin/transaction/`
- `src/commonMain/kotlin/dao/`
- `src/macosArm64Main/kotlin/`

## Platform seams

The repository uses `expect`/`actual` for browser launching, OAuth redirect handling, and filesystem-backed DAO implementations.
Use `code-index.json` to follow common-to-platform symbol mappings quickly.
