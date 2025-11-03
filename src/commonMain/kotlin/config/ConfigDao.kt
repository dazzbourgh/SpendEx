package config

import arrow.core.Either

interface ConfigDao {
    suspend fun loadPlaidConfig(): Either<String, PlaidConfig>
}
