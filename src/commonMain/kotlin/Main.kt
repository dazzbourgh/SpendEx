import command.RootCommand
import com.github.ajalt.clikt.core.subcommands
import command.AccountAddCommand
import command.AccountCommand
import command.AccountListCommand
import interpreter.Interpreter
import interpreter.InterpreterFactory

fun main(args: Array<String>) {
    val interpreter: Interpreter = InterpreterFactory.get("prod")
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
