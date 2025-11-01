package cli

import arrow.core.Either

/**
 * Interface for executing commands.
 * Implementations contain the business logic for interpreting and executing parsed commands.
 */
interface CommandExecutor {
    /**
     * Executes a parsed command and returns a result.
     * @param command The parsed command to execute
     * @return Either with Left containing error message or Right containing success value
     */
    suspend fun execute(command: ParsedCommand): Either<String, String>
}
