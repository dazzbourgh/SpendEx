package config

import arrow.core.Either

expect class JsonConfigDao() : ConfigDao {
    override suspend fun loadPlaidConfig(): Either<String, PlaidConfig>
}
