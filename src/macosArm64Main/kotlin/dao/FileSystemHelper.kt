package dao

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.chmod
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fwrite
import platform.posix.mkdir

@OptIn(ExperimentalForeignApi::class)
actual object FileSystemHelper {
    actual fun ensureDirectoryExists(path: String) {
        mkdir(path, 0x1C0u) // 0700 permissions - owner read/write/execute only
    }

    actual fun readFile(path: String): String? {
        val file = fopen(path, "r") ?: return null
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

    actual fun writeFile(
        path: String,
        content: String,
    ) {
        val file = fopen(path, "w") ?: throw IllegalStateException("Cannot open file for writing: $path")
        try {
            val bytes = content.encodeToByteArray()
            bytes.usePinned { pinned ->
                fwrite(pinned.addressOf(0), 1u, bytes.size.toULong(), file)
            }
        } finally {
            fclose(file)
        }
        // Set file permissions to 0600 (owner read/write only)
        chmod(path, 0x180u)
    }
}
