package dao

/**
 * Helper for common file system operations.
 * Handles directory creation and file I/O with proper permissions.
 */
expect object FileSystemHelper {
    /**
     * Ensures a directory exists with secure permissions (0700).
     *
     * @param path The directory path
     */
    fun ensureDirectoryExists(path: String)

    /**
     * Reads the entire contents of a file.
     *
     * @param path The file path
     * @return The file contents, or null if file doesn't exist
     */
    fun readFile(path: String): String?

    /**
     * Writes content to a file with secure permissions (0600).
     *
     * @param path The file path
     * @param content The content to write
     */
    fun writeFile(
        path: String,
        content: String,
    )
}
