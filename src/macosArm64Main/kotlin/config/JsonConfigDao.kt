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
actual class JsonConfigDao : ConfigDao {
    private val configFile: String
    private val json = Json { ignoreUnknownKeys = true }

    init {
        val home = getenv("HOME")?.toKString() ?: throw IllegalStateException("HOME environment variable not set")
        configFile = "$home/.spndx/app-data-sandbox.json"
    }

    actual override suspend fun loadPlaidConfig(): Either<String, PlaidConfig> =
        try {
            FileSystemHelper.readFile(configFile)
                ?.takeIf { it.isNotBlank() }
                ?.let { json.decodeFromString<PlaidConfig>(it).right() }
                ?: "Plaid configuration file not found at $configFile".left()
        } catch (e: Exception) {
            "Failed to load Plaid configuration: ${e.message}".left()
        }
}
