# Data and Storage

## Local storage

SpendEx stores local state under `~/.spndx`.
The repository uses JSON-backed persistence for Plaid configuration, linked account tokens, and transaction sync state.

## Main storage concerns

- Plaid credentials
- linked institution or account tokens
- synced transactions and cursors or transaction state

## Relevant code areas

- shared DAO contracts: `src/commonMain/kotlin/dao/`
- macOS DAO implementations: `src/macosArm64Main/kotlin/dao/`
- serialized models: `src/commonMain/kotlin/model/`

## When to open these files

Open storage-related code when you are changing:

- local file layout
- JSON schemas or serialized models
- transaction persistence behavior
- config or token loading and saving behavior
