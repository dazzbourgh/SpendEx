package dao

import command.BankDetails
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.serialization.encodeToString
import platform.posix.chmod
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fwrite
import platform.posix.getenv
import platform.posix.mkdir

@OptIn(ExperimentalForeignApi::class)
actual class JsonAccountDaoImpl : AccountDao {
    private val dataDir: String
    private val dataFile: String
    private val json = JsonConfig.json

    init {
        val home = getenv("HOME")?.toKString() ?: throw IllegalStateException("HOME environment variable not set")
        dataDir = "$home/.spndx"
        dataFile = "$dataDir/banks.json"
        ensureDirectoryExists()
    }

    private fun ensureDirectoryExists() {
        mkdir(dataDir, 0x1C0u) // 0700 permissions - owner read/write/execute only
    }

    actual override suspend fun save(bankDetails: BankDetails) {
        val currentAccounts = list().toMutableList()
        currentAccounts.add(bankDetails)
        val jsonContent = json.encodeToString(currentAccounts)
        writeFile(jsonContent)
    }

    actual override suspend fun list(): Iterable<BankDetails> {
        val content = readFile() ?: return emptyList()
        if (content.isBlank()) return emptyList()
        return try {
            json.decodeFromString<List<BankDetails>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun readFile(): String? {
        val file = fopen(dataFile, "r") ?: return null
        try {
            val buffer = ByteArray(65536)
            val result = StringBuilder()
            var bytesRead: ULong
            while (true) {
                bytesRead =
                    buffer.usePinned { pinned ->
                        fread(pinned.addressOf(0), 1u, buffer.size.toULong(), file)
                    }
                if (bytesRead == 0uL) break
                result.append(buffer.decodeToString(0, bytesRead.toInt()))
            }
            return result.toString()
        } finally {
            fclose(file)
        }
    }

    private fun writeFile(content: String) {
        val file = fopen(dataFile, "w") ?: throw IllegalStateException("Cannot open file for writing: $dataFile")
        try {
            val bytes = content.encodeToByteArray()
            bytes.usePinned { pinned ->
                fwrite(pinned.addressOf(0), 1u, bytes.size.toULong(), file)
            }
        } finally {
            fclose(file)
        }
        // Set file permissions to 0600 (owner read/write only)
        chmod(dataFile, 0x180u)
    }
}
