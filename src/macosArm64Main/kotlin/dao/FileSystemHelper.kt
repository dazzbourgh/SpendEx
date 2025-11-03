package dao

import config.Constants
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
        mkdir(path, Constants.FileSystem.DIR_PERMISSIONS.toUShort())
    }

    actual fun readFile(path: String): String? {
        val file = fopen(path, Constants.FileSystem.FILE_MODE_READ) ?: return null
        try {
            val buffer = ByteArray(Constants.FileSystem.BUFFER_SIZE)
            val result = StringBuilder()
            var bytesRead: ULong
            while (true) {
                bytesRead =
                    buffer.usePinned { pinned ->
                        fread(pinned.addressOf(0), Constants.FileSystem.ELEMENT_SIZE, buffer.size.toULong(), file)
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
        val file =
            fopen(path, Constants.FileSystem.FILE_MODE_WRITE)
                ?: throw IllegalStateException("${Constants.FileSystem.ErrorMessages.CANNOT_OPEN_FILE}: $path")
        try {
            val bytes = content.encodeToByteArray()
            bytes.usePinned { pinned ->
                fwrite(pinned.addressOf(0), Constants.FileSystem.ELEMENT_SIZE, bytes.size.toULong(), file)
            }
        } finally {
            fclose(file)
        }
        chmod(path, Constants.FileSystem.FILE_PERMISSIONS.toUShort())
    }
}
