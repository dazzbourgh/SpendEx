package cli

/**
 * Interface for executing commands.
 * Implementations contain the business logic for interpreting and executing parsed commands.
 */
interface CommandExecutor {
    /**
     * Executes a parsed command and returns a result.
     * @param command The parsed command to execute
     * @return Result indicating success or failure with an optional message
     */
    suspend fun execute(command: ParsedCommand): Result<String>
}

/**
 * Result type for command execution
 */
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val message: String) : Result<Nothing>()
}
