# SpendEx

SpendEx is a CLI tool for managing financial accounts and transactions.

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

## Installation

To install the `spndx` command globally to `/usr/local/bin`, run:


```bash
./install.sh
```

After installation, you can run:

```bash
spndx command add --bank Chase --username john.doe@email.com
spndx command list
```

## Running

To run the CLI tool without installing:

```bash
./gradlew run --args="<command>"
```

Or build and run the native binary directly:

```bash
./gradlew macosArm64Binaries
./build/bin/macosArm64/releaseExecutable/spendex.kexe
```

## Usage

The examples below show commands using the installed `spndx` binary. If you haven't installed it yet, replace `spndx` with `./gradlew run --args="..."`.

### Add an Account

Add a new financial account with a bank name and username:

```bash
spndx command add --bank Chase --username john.doe@email.com
```

### List Accounts

View all registered accounts:

```bash
spndx command list
```

### Help

Display help information:

```bash
spndx --help
```

Or get help for a specific command:

```bash
spndx command add --help
```

## Project Structure

The project follows a feature-based organization:

- `command/` - CLI command definitions and data models
- `interpreter/` - Business logic implementation for commands
- `dao/` - Data access layer for persistent storage

### Data Storage

Account data is stored in JSON format at `~/.spndx/banks.json` with secure permissions (0600 - owner read/write only). The directory is created automatically with 0700 permissions.

## Development

### Code Style

The project uses ktlint for code formatting. Formatting is automatically applied during the build process.

To manually format code:

```bash
./gradlew ktlintFormat
```
