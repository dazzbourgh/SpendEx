# SpendEx

SpendEx is a Kotlin Multiplatform CLI tool for managing financial accounts and transactions via Plaid API integration.

## Features

- Configure Plaid API credentials
- Add financial accounts from multiple banks via secure OAuth flow
- List all registered accounts with details
- View and filter transaction history

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
spndx plaid configure
spndx accounts add
spndx accounts list
spndx transactions list
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

### Configure Plaid API

Before adding accounts, configure your Plaid API credentials:

```bash
spndx plaid configure
```

You'll be prompted to enter your Plaid client ID and secret.

### Add an Account

Add a new financial account (opens browser for OAuth authentication):

```bash
spndx accounts add
```

This will:
1. Open your browser to Plaid Link
2. Let you select your bank and authenticate
3. Securely link your account

### List Accounts

View all registered accounts:

```bash
spndx accounts list
```

### List Transactions

View transactions with optional filters:

```bash
# List all transactions
spndx transactions list

# Filter by date range
spndx transactions list --from 2024-01-01 --to 2024-12-31

# Filter by institution
spndx transactions list --institution "Chase Bank"

# Combine filters
spndx transactions list --from 2024-01-01 --institution "Chase Bank"
```

### Environment

Specify the Plaid environment (sandbox or prod):

```bash
spndx --environment sandbox accounts add
spndx --environment prod accounts add
```

Default is `prod`.

### Help

Display help information:

```bash
spndx --help
```

Or get help for a specific command:

```bash
spndx accounts --help
spndx accounts add --help
spndx transactions list --help
```

## Project Structure

The project follows a three-layer architecture:

- `command/` - CLI command definitions using Clikt framework
- `interpreter/` - Business logic orchestration layer
- `service/` - Domain logic implementation (account, transaction, plaid services)
- `dao/` - Data access layer for persistent storage
- `config/` - Application configuration and constants

### Data Storage

Data is stored securely in the `~/.spndx/` directory:

- `config.json` - Plaid API credentials (0600 permissions)
- `tokens.json` - Plaid access tokens for linked accounts (0600 permissions)
- `transactions.json` - Transaction data (0600 permissions)

All files are created with 0600 permissions (owner read/write only). The directory is created automatically with 0700 permissions.

## Development

### Code Style

The project uses ktlint for code formatting. Formatting is automatically applied during the build process.

To manually format code:

```bash
./gradlew ktlintFormat
```
