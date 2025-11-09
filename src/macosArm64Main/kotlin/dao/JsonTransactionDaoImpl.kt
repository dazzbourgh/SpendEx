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

    actual override suspend fun loadTransactions(itemId: String): StoredTransactions? {
        val filePath = "$transactionsDir/$itemId.json"
        val content = FileSystemHelper.readFile(filePath) ?: return null
        if (content.isBlank()) return null
        return try {
            json.decodeFromString<StoredTransactions>(content)
        } catch (e: Exception) {
            null
        }
    }

    actual override suspend fun saveTransactions(
        itemId: String,
        storedTransactions: StoredTransactions,
    ) {
        val filePath = "$transactionsDir/$itemId.json"

        // Load existing transactions to merge
        val existing = loadTransactions(itemId)
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
}
