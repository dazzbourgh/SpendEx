package config

import arrow.core.Either

interface ConfigDao {
    suspend fun loadPlaidConfig(): Either<String, PlaidConfig>

    suspend fun savePlaidConfig(config: PlaidConfig): Either<String, Unit>
}
