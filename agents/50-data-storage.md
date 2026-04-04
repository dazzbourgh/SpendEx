# Data and Storage

## Local storage

SpendEx stores local state under `~/.spndx`.
The repository uses JSON-backed persistence for Plaid configuration and linked account tokens only.
Transaction listing now always performs a fresh fetch from the connected institution rather than caching results locally.

## Main storage concerns

- Plaid credentials
- linked institution or account tokens

## Relevant code areas

- shared DAO contracts: `src/commonMain/kotlin/dao/`
- macOS DAO implementations: `src/macosArm64Main/kotlin/dao/`
- serialized models: `src/commonMain/kotlin/model/`

## When to open these files

Open storage-related code when you are changing:

- local file layout
- JSON schemas or serialized models for config or token data
- config or token loading and saving behavior
