package dao

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import command.BankDetails
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.encodeToString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual class JsonAccountDaoImpl : AccountDao {
    private val dataDir: String
    private val dataFile: String
    private val json = JsonConfig.json

    init {
        val home = getenv("HOME")?.toKString() ?: throw IllegalStateException("HOME environment variable not set")
        dataDir = "$home/.spndx"
        dataFile = "$dataDir/banks.json"
        FileSystemHelper.ensureDirectoryExists(dataDir)
    }

    actual override suspend fun save(bankDetails: BankDetails): Either<String, Unit> =
        try {
            list().map { currentAccounts ->
                val jsonContent = json.encodeToString(currentAccounts + bankDetails)
                FileSystemHelper.writeFile(dataFile, jsonContent)
            }
        } catch (exception: Exception) {
            exception.stackTraceToString().left()
        }

    actual override suspend fun list(): Either<String, Iterable<BankDetails>> =
        try {
            FileSystemHelper.readFile(dataFile)
                ?.takeIf { it.isNotBlank() }
                ?.let { json.decodeFromString<List<BankDetails>>(it).right() } ?: emptyList<BankDetails>().right()
        } catch (e: Exception) {
            e.stackTraceToString().left()
        }
}
