package dao

import config.Constants
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.InstitutionConnection
import platform.posix.getenv
import provider.ProviderIds

/**
 * macOS JSON-backed institution connection repository.
 */
@OptIn(ExperimentalForeignApi::class)
actual class JsonInstitutionConnectionRepository : InstitutionConnectionRepository {
    private val dataDir: String
    private val dataFile: String
    private val json: Json = Json { ignoreUnknownKeys = true }

    init {
        val home =
            getenv(Constants.FileSystem.HOME_ENV_VAR)?.toKString()
                ?: throw IllegalStateException(Constants.FileSystem.ErrorMessages.HOME_NOT_SET)
        dataDir = "$home/${Constants.FileSystem.APP_DIR_NAME}"
        dataFile = "$dataDir/${Constants.FileSystem.TOKENS_FILE_NAME}"
        FileSystemHelper.ensureDirectoryExists(dataDir)
    }

    actual override suspend fun save(connection: InstitutionConnection) {
        val currentConnections: MutableList<InstitutionConnection> = list().toMutableList()
        currentConnections.removeAll {
            it.providerId == connection.providerId && it.connectionId == connection.connectionId
        }
        currentConnections.add(connection)
        val jsonContent = json.encodeToString(currentConnections)
        FileSystemHelper.writeFile(dataFile, jsonContent)
    }

    actual override suspend fun list(): Iterable<InstitutionConnection> {
        val content = FileSystemHelper.readFile(dataFile) ?: return emptyList()
        if (content.isBlank()) return emptyList()
        return decodeConnections(content)
    }

    private fun decodeConnections(content: String): Iterable<InstitutionConnection> =
        runCatching {
            json.decodeFromString<List<InstitutionConnection>>(content)
        }.getOrElse {
            runCatching {
                json.decodeFromString<List<LegacyPlaidToken>>(content).map { legacyToken ->
                    InstitutionConnection(
                        providerId = ProviderIds.PLAID,
                        institutionName = legacyToken.bankName,
                        connectionId = legacyToken.itemId,
                        providerState = legacyToken.accessToken,
                        createdAt = legacyToken.createdAt,
                    )
                }
            }.getOrDefault(emptyList())
        }

    /**
     * Legacy storage model used to migrate existing Plaid-only connection data.
     */
    @Serializable
    private data class LegacyPlaidToken(
        val bankName: String,
        val accessToken: String,
        val itemId: String,
        val createdAt: Instant,
    )
}
