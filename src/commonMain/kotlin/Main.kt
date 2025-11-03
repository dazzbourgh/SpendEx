import com.github.ajalt.clikt.core.subcommands
import command.AccountAddCommand
import command.AccountCommand
import command.AccountListCommand
import command.RootCommand
import config.Constants
import interpreter.Interpreter
import interpreter.InterpreterFactory

fun main(args: Array<String>) {
    val interpreter: Interpreter = InterpreterFactory.get(Constants.App.ENVIRONMENT_PROD)
    RootCommand
        .subcommands(
            AccountCommand
                .subcommands(
                    AccountAddCommand(
                        interpreter.accountCommandInterpreter::addAccount,
                    ),
                    AccountListCommand(
                        interpreter.accountCommandInterpreter::listAccounts,
                    ),
                ),
        )
        .main(args)
}
