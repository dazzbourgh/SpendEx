package command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking

object AccountCommand : CliktCommand(
    name = "account",
    help = "Manage financial accounts",
) {
    override fun run() = Unit
}

class AccountAddCommand(
    private val addCommand: suspend (String) -> Unit,
) : CliktCommand(
        name = "add",
        help = "Add a new financial account. You will select the bank and login in your browser.",
    ) {
    val username by option().required().help("Your app user identifier (not your bank login)")

    override fun run() =
        runBlocking {
            addCommand(username)
        }
}

class AccountListCommand(
    private val listCommand: suspend () -> Unit,
) : CliktCommand(
        name = "list",
        help = "List all added accounts",
    ) {
    override fun run() =
        runBlocking {
            listCommand()
        }
}
