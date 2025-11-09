package dao

import arrow.core.Either
import config.PlaidConfig

interface ConfigDao {
    suspend fun loadPlaidConfig(): Either<String, PlaidConfig>

    suspend fun savePlaidConfig(config: PlaidConfig): Either<String, Unit>
}
