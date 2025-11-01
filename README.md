# Spendex

Spendex is a CLI tool for managing financial accounts and transactions.

## Features

- Add and manage financial accounts from multiple banks
- List all registered accounts with details

## Supported Banks

- American Express (Amex)
- Bank of America (Bofa)
- Chase
- Goldman Sachs (GoldmanSachs)

## Building

This project uses Gradle with the Kotlin Multiplatform plugin. To build the project:

```bash
./gradlew build
```

## Running

To run the CLI tool:

```bash
./gradlew run --args="<command>"
```

Or build and run the native binary:

```bash
./gradlew macosArm64Binaries
./build/bin/macosArm64/releaseExecutable/spendex.kexe
```

## Usage

### Add an Account

Add a new financial account with a bank name and username:

```bash
./gradlew run --args="command add --bank Chase --username john.doe@email.com"
```

### List Accounts

View all registered accounts:

```bash
./gradlew run --args="command list"
```

### Help

Display help information:

```bash
./gradlew run --args="--help"
```

Or get help for a specific command:

```bash
./gradlew run --args="command add --help"
```

## Project Structure

The project follows a feature-based organization:

- `command/` - CLI command definitions and data models
- `interpreter/` - Business logic implementation for commands

## Development

### Code Style

The project uses ktlint for code formatting. Formatting is automatically applied during the build process.

To manually format code:

```bash
./gradlew ktlintFormat
```

