package account

import cli.*

/**
 * Executor for Account feature commands.
 * Contains the business logic for executing account-related operations.
 */
class AccountCommandExecutor(
    private val addAccountAction: suspend (Bank) -> Unit = { /* noop for now */ }
) : CommandExecutor {

    override suspend fun execute(command: ParsedCommand): Result<String> {
        return when (val feature = command.feature) {
            is Feature.Account -> executeAccountCommand(feature.command)
        }
    }

    private suspend fun executeAccountCommand(command: AccountCommand): Result<String> {
        return when (command) {
            is AccountCommand.Add -> executeAddCommand(command)
        }
    }

    private suspend fun executeAddCommand(command: AccountCommand.Add): Result<String> {
        val bank = Bank.fromString(command.bank)
            ?: return Result.Failure("Invalid bank: ${command.bank}. Valid values: ${Bank.entries.joinToString { it.name.lowercase() }}")

        return try {
            addAccountAction(bank)
            Result.Success("Account for ${bank.name} would be added")
        } catch (e: Exception) {
            Result.Failure("Failed to add account: ${e.message}")
        }
    }
}
