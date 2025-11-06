package config

import arrow.core.Either

expect class JsonConfigDao(environmentConfig: EnvironmentConfig) : ConfigDao {
    override suspend fun loadPlaidConfig(): Either<String, PlaidConfig>

    override suspend fun savePlaidConfig(config: PlaidConfig): Either<String, Unit>
}
