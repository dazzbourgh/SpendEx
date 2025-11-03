package command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import config.Constants
import kotlinx.coroutines.runBlocking

object AccountCommand : CliktCommand(
    name = Constants.Commands.Account.NAME,
    help = Constants.Commands.Account.HELP,
) {
    override fun run() = Unit
}

class AccountAddCommand(
    private val addCommand: suspend (String) -> Unit,
) : CliktCommand(
        name = Constants.Commands.Account.Add.NAME,
        help = Constants.Commands.Account.Add.HELP,
    ) {
    val username by option().required().help(Constants.Commands.Account.Add.USERNAME_HELP)

    override fun run() =
        runBlocking {
            addCommand(username)
        }
}

class AccountListCommand(
    private val listCommand: suspend () -> Unit,
) : CliktCommand(
        name = Constants.Commands.Account.List.NAME,
        help = Constants.Commands.Account.List.HELP,
    ) {
    override fun run() =
        runBlocking {
            listCommand()
        }
}
