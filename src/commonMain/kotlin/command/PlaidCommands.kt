package command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import config.Constants
import kotlinx.coroutines.runBlocking

object PlaidCommand : CliktCommand(
    name = Constants.Commands.Plaid.NAME,
    help = Constants.Commands.Plaid.HELP,
) {
    override fun run() = Unit
}

class PlaidConfigureCommand(
    private val configureCommand: suspend (String, String) -> Unit,
) : CliktCommand(
        name = Constants.Commands.Plaid.Configure.NAME,
        help = Constants.Commands.Plaid.Configure.HELP,
    ) {
    val clientId by option("--client-id").required().help(Constants.Commands.Plaid.Configure.CLIENT_ID_HELP)
    val clientSecret by option("--client-secret").required().help(Constants.Commands.Plaid.Configure.CLIENT_SECRET_HELP)

    override fun run() =
        runBlocking {
            configureCommand(clientId, clientSecret)
        }
}
