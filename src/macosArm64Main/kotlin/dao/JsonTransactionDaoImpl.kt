package dao

import config.Constants
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.StoredTransactions
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual class JsonTransactionDaoImpl : TransactionDao {
    private val transactionsDir: String
    private val json = Json { ignoreUnknownKeys = true }

    init {
        val home =
            getenv(Constants.FileSystem.HOME_ENV_VAR)?.toKString()
                ?: throw IllegalStateException(Constants.FileSystem.ErrorMessages.HOME_NOT_SET)
        transactionsDir = "$home/${Constants.FileSystem.APP_DIR_NAME}/${Constants.FileSystem.TRANSACTIONS_DIR_NAME}"
        FileSystemHelper.ensureDirectoryExists(transactionsDir)
    }

    actual override suspend fun loadTransactions(
        providerId: String,
        connectionId: String,
    ): StoredTransactions? {
        val filePath = providerScopedFilePath(providerId, connectionId)
        val content = FileSystemHelper.readFile(filePath) ?: FileSystemHelper.readFile(legacyFilePath(connectionId)) ?: return null
        if (content.isBlank()) return null
        return try {
            json.decodeFromString<StoredTransactions>(content)
        } catch (e: Exception) {
            null
        }
    }

    actual override suspend fun saveTransactions(
        providerId: String,
        connectionId: String,
        storedTransactions: StoredTransactions,
    ) {
        val filePath = providerScopedFilePath(providerId, connectionId)

        // Load existing transactions to merge
        val existing = loadTransactions(providerId, connectionId)
        val mergedTransactions =
            if (existing != null) {
                // Merge transactions, avoiding duplicates by transaction_id
                val existingIds = existing.transactions.map { it.transactionId }.toSet()
                val newTransactions =
                    storedTransactions.transactions.filter {
                        it.transactionId !in existingIds
                    }
                existing.copy(
                    cursor = storedTransactions.cursor,
                    transactions = existing.transactions + newTransactions,
                )
            } else {
                storedTransactions
            }

        val jsonContent = json.encodeToString(mergedTransactions)
        FileSystemHelper.writeFile(filePath, jsonContent)
    }

    /**
     * Builds a provider-scoped transaction file path and ensures the provider directory exists.
     */
    private fun providerScopedFilePath(
        providerId: String,
        connectionId: String,
    ): String {
        val providerDir = "$transactionsDir/${encodePathSegment(providerId)}"
        FileSystemHelper.ensureDirectoryExists(providerDir)
        return "$providerDir/${encodePathSegment(connectionId)}.json"
    }

    /**
     * Resolves the pre-provider-neutral transaction file path for backward-compatible reads.
     */
    private fun legacyFilePath(connectionId: String): String = "$transactionsDir/$connectionId.json"

    /**
     * Encodes an arbitrary identifier into a filesystem-safe UTF-8 hex string.
     */
    private fun encodePathSegment(value: String): String =
        value
            .encodeToByteArray()
            .joinToString(separator = "") { byte -> byte.toUByte().toString(16).padStart(length = 2, padChar = '0') }
}
