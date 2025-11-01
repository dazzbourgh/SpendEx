import command.AccountAddCommand
import command.AccountCommand
import cli.FinancialAdvisor
import com.github.ajalt.clikt.core.subcommands
import interpreter.Interpreter
import interpreter.InterpreterFactory

fun main(args: Array<String>) {
    val interpreter: Interpreter = InterpreterFactory.get("prod")
    FinancialAdvisor
        .subcommands(
            AccountCommand
                .subcommands(
                    AccountAddCommand(
                        interpreter.accountCommandInterpreter::addAccount
                    ),
                ),
        )
        .main(args)
}
