package command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import kotlinx.coroutines.runBlocking

object AccountCommand : CliktCommand(
    name = "command",
    help = "Manage financial accounts",
) {
    override fun run() = Unit
}

class AccountAddCommand(private val addCommand: suspend (Bank) -> Unit) : CliktCommand(
    name = "add",
    help = "Add a new financial account",
) {
    val bank by option().enum<Bank>().required().help("Bank name")

    override fun run() =
        runBlocking {
            addCommand(bank)
        }
}
