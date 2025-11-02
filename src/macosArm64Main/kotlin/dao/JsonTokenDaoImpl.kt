package dao

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.encodeToString
import plaid.PlaidToken
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual class JsonTokenDaoImpl : TokenDao {
    private val dataDir: String
    private val dataFile: String
    private val json = JsonConfig.json

    init {
        val home = getenv("HOME")?.toKString() ?: throw IllegalStateException("HOME environment variable not set")
        dataDir = "$home/.spndx"
        dataFile = "$dataDir/tokens.json"
        FileSystemHelper.ensureDirectoryExists(dataDir)
    }

    actual override suspend fun save(token: PlaidToken) {
        val currentTokens = list().toMutableList()
        // Remove existing token for the same bank if present
        currentTokens.removeAll { it.bankName == token.bankName }
        currentTokens.add(token)
        val jsonContent = json.encodeToString(currentTokens)
        FileSystemHelper.writeFile(dataFile, jsonContent)
    }

    actual override suspend fun list(): Iterable<PlaidToken> {
        val content = FileSystemHelper.readFile(dataFile) ?: return emptyList()
        if (content.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<PlaidToken>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual override suspend fun findByBankName(bankName: String): PlaidToken? = list().firstOrNull { it.bankName == bankName }

    actual override suspend fun delete(bankName: String) {
        val currentTokens = list().toMutableList()
        currentTokens.removeAll { it.bankName == bankName }
        val jsonContent = json.encodeToString(currentTokens)
        FileSystemHelper.writeFile(dataFile, jsonContent)
    }
}
