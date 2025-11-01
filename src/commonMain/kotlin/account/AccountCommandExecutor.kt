package account

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import cli.*

/**
 * Executor for Account feature commands.
 * Contains the business logic for executing account-related operations.
 */
class AccountCommandExecutor(
    private val addAccountAction: suspend (Bank) -> Unit = { /* noop for now */ }
) : CommandExecutor {

    override suspend fun execute(command: ParsedCommand): Either<String, String> {
        return when (val feature = command.feature) {
            is Feature.Account -> executeAccountCommand(feature.command)
        }
    }

    private suspend fun executeAccountCommand(command: AccountCommand): Either<String, String> {
        return when (command) {
            is AccountCommand.Add -> executeAddCommand(command)
        }
    }

    private suspend fun executeAddCommand(command: AccountCommand.Add): Either<String, String> {
        val bank = Bank.fromString(command.bank)
            ?: return "Invalid bank: ${command.bank}. Valid values: ${Bank.entries.joinToString { it.name.lowercase() }}".left()

        return try {
            addAccountAction(bank)
            "Account for ${bank.name} would be added".right()
        } catch (e: Exception) {
            "Failed to add account: ${e.message}".left()
        }
    }
}
