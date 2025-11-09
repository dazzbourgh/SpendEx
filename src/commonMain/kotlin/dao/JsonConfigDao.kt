package dao

import arrow.core.Either
import config.EnvironmentConfig
import config.PlaidConfig

expect class JsonConfigDao(environmentConfig: EnvironmentConfig) : ConfigDao {
    override suspend fun loadPlaidConfig(): Either<String, PlaidConfig>

    override suspend fun savePlaidConfig(config: PlaidConfig): Either<String, Unit>
}
