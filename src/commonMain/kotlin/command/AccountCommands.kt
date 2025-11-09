package command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import config.Constants
import interpreter.AccountCommandInterpreter
import kotlinx.coroutines.runBlocking

object AccountsCommand : CliktCommand(
    name = Constants.Commands.Accounts.NAME,
    help = Constants.Commands.Accounts.HELP,
) {
    override fun run() = Unit
}

class AccountAddCommand(
    private val interpreter: AccountCommandInterpreter,
) : CliktCommand(
        name = Constants.Commands.Accounts.Add.NAME,
        help = Constants.Commands.Accounts.Add.HELP,
    ) {
    override fun run() =
        runBlocking {
            interpreter.addAccount().fold(
                { error -> println("${Constants.Commands.ErrorMessages.ACCOUNT_ADD_FAILED}: $error") },
                { println(Constants.Commands.ErrorMessages.ACCOUNT_ADD_SUCCESS) },
            )
        }
}

class AccountListCommand(
    private val interpreter: AccountCommandInterpreter,
) : CliktCommand(
        name = Constants.Commands.Accounts.List.NAME,
        help = Constants.Commands.Accounts.List.HELP,
    ) {
    override fun run() =
        runBlocking {
            interpreter.listAccounts().fold(
                { error -> println("${Constants.Commands.ErrorMessages.ACCOUNT_LIST_FAILED}: $error") },
                { accounts ->
                    if (accounts.none()) {
                        println("No accounts added yet")
                    } else {
                        accounts.forEach { account ->
                            println("${account.name} (added: ${account.dateAdded})")
                        }
                    }
                },
            )
        }
}
