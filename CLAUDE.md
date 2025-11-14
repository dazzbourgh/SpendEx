# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Project Overview
SpendEx is a Kotlin Multiplatform CLI tool for managing financial accounts and transactions via Plaid API integration. The project targets macOS ARM64 and uses a feature-based architecture.

# Build & Development Commands

## Build
```bash
./gradlew build
```
This runs ktlintFormat automatically (configured in build.gradle.kts).

## Compile only (for quick iteration)
```bash
./gradlew compileKotlinMacosArm64
```

## Code formatting
```bash
./gradlew ktlintFormat
```

## Build native binary
```bash
./gradlew macosArm64Binaries
```
Binary output: `build/bin/macosArm64/releaseExecutable/spendex.kexe`

## Install globally
```bash
./install.sh
```
Installs as `spndx` to `/usr/local/bin`

## Run without installing
```bash
./gradlew run --args="accounts list"
```

## Testing the binary
Do not run the built binary to test changes - assume everything is fine.

# Architecture

## Dependency Injection Pattern
The project uses manual dependency injection via the `InterpreterFactory`. All dependencies are wired up in `InterpreterFactory.get()`, which constructs the complete object graph for a given environment (currently only ENVIRONMENT_SANDBOX).

**Key principle:** The factory creates platform-specific implementations using `expect/actual` functions:
- `createBrowserLauncher()`: Platform-specific browser launching
- `createOAuthRedirectServer()`: Platform-specific OAuth server

## Three-Layer Architecture

### 1. Command Layer (`command/`)
Defines CLI structure using Clikt framework. Commands are organized hierarchically:
- `RootCommand` - Entry point
- Group commands: `AccountsCommand`, `PlaidCommand`, `TransactionsCommand`
- Leaf commands: `AccountAddCommand`, `AccountListCommand`, `PlaidConfigureCommand`, `TransactionListCommand`

Commands accept CommandInterpreter dependencies via constructor injection and delegate business logic to interpreters.

### 2. Interpreter Layer (`interpreter/`)
Contains business logic orchestration. Interpreters coordinate between:
- Services (domain logic)
- DAOs (persistence)
- External APIs (Plaid)

**Pattern:** Each command group has a corresponding interpreter interface:
- `AccountCommandInterpreter` → `AccountCommandInterpreterImpl`
- `PlaidCommandInterpreter` → `PlaidCommandInterpreterImpl`
- `TransactionCommandInterpreter` → `TransactionCommandInterpreterImpl`

The main `Interpreter` interface aggregates all command interpreters.

### 3. Service & DAO Layer
- **Services** (`account/`, `transaction/`, `plaid/`): Domain logic implementation
- **DAOs** (`dao/`): Persistence abstraction with JSON file-based implementations

## Multiplatform Structure
- `commonMain/` - Shared cross-platform code (interfaces, business logic)
- `macosArm64Main/` - macOS-specific implementations (file system, browser, OAuth server)

Use `expect` declarations in `commonMain` and `actual` implementations in platform-specific source sets.

# Key Technologies & Libraries

- **Clikt**: CLI framework for command parsing and structure
- **Arrow-KT**: Functional programming, particularly `Either` for typed error handling
- **Ktor**: HTTP client for Plaid API integration and server for OAuth callbacks
- **kotlinx.serialization**: JSON serialization/deserialization
- **kotlinx.coroutines**: Async/suspend function support

# Coding Standards

## Language & Design
- Use Kotlin advanced features appropriately: suspend functions, nullable types, data classes, sealed classes
- Prefer composition over inheritance, code to interfaces
- Use dependency injection via constructor parameters
- Keep components stateless when possible
- Use immutable data structures (data classes with `val`)

## Error Handling
Use Arrow-KT `Either<String, T>` for operations that can fail:
- Left: error message
- Right: success value

Example pattern:
```kotlin
suspend fun operation(): Either<String, Result> =
    dao.load().mapLeft { "Failed to load: $it" }
        .flatMap { process(it) }
```

## File Organization
Group by feature/responsibility, not by type. Each feature directory contains:
- Service interfaces and implementations
- Related model classes
- DAOs specific to that feature (if applicable)

Example: `transaction/` contains both `TransactionService` and `TransactionServiceImpl`, while shared DAOs like `TransactionDao` are in `dao/`.

# Data Storage
- Configuration: `~/.spndx/config.json` (Plaid credentials)
- Tokens: `~/.spndx/tokens.json` (Plaid access tokens)
- Transactions: `~/.spndx/transactions.json`
- All files created with 0600 permissions (owner read/write only)
- Directory created with 0700 permissions

# Plaid Integration Flow
1. User configures Plaid credentials via `PlaidConfigureCommand`
2. `PlaidService.createLinkToken()` generates link token
3. `PlaidService.performLinkFlow()` opens browser and starts local OAuth server
4. User authenticates with bank in browser
5. Plaid redirects to localhost with public token
6. `PlaidService.exchangePublicToken()` gets access token
7. Access token stored via `TokenDao` for future API calls

# Development Guidelines
- Avoid heavy frameworks like Spring Boot (use lightweight libraries)
- Always use immutable data structures when possible
- Prefer Arrow-KT for typed errors over exceptions
- ktlint formatting is mandatory (auto-applied on build)
