package dao

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import config.Constants
import config.EnvironmentConfig
import config.PlaidConfig
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

    actual override suspend fun savePlaidConfig(config: PlaidConfig): Either<String, Unit> =
        try {
            val home =
                getenv(Constants.FileSystem.HOME_ENV_VAR)?.toKString()
                    ?: return Constants.FileSystem.ErrorMessages.HOME_NOT_SET.left()
            val appDir = "$home/${Constants.FileSystem.APP_DIR_NAME}"

            FileSystemHelper.ensureDirectoryExists(appDir)

            val configJson = json.encodeToString(PlaidConfig.serializer(), config)
            FileSystemHelper.writeFile(configFile, configJson)
            Unit.right()
        } catch (e: Exception) {
            "Failed to save Plaid configuration: ${e.message}".left()
        }
}
