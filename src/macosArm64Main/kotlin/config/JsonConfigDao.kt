package config

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dao.FileSystemHelper
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.Json
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual class JsonConfigDao actual constructor(
    environmentConfig: EnvironmentConfig,
) : ConfigDao {
    private val configFile: String
    private val json = Json { ignoreUnknownKeys = true }

    init {
        val home =
            getenv(Constants.FileSystem.HOME_ENV_VAR)?.toKString()
                ?: throw IllegalStateException(Constants.FileSystem.ErrorMessages.HOME_NOT_SET)
        configFile = "$home/${Constants.FileSystem.APP_DIR_NAME}/${environmentConfig.configFileName}"
    }

    actual override suspend fun loadPlaidConfig(): Either<String, PlaidConfig> =
        try {
            FileSystemHelper.readFile(configFile)
                ?.takeIf { it.isNotBlank() }
                ?.let { json.decodeFromString<PlaidConfig>(it).right() }
                ?: "${Constants.FileSystem.ErrorMessages.CONFIG_NOT_FOUND} $configFile".left()
        } catch (e: Exception) {
            "${Constants.FileSystem.ErrorMessages.CONFIG_LOAD_FAILED}: ${e.message}".left()
        }
}
