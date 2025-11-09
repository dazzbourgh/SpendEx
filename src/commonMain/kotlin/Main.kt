import com.github.ajalt.clikt.core.subcommands
import command.AccountAddCommand
import command.AccountListCommand
import command.AccountsCommand
import command.PlaidCommand
import command.PlaidConfigureCommand
import command.RootCommand
import command.TransactionListCommand
import command.TransactionsCommand
import config.Constants
import interpreter.Interpreter
import interpreter.InterpreterFactory

fun main(args: Array<String>) {
    val interpreter: Interpreter = InterpreterFactory.get(Constants.App.ENVIRONMENT_SANDBOX)
    RootCommand
        .subcommands(
            AccountsCommand
                .subcommands(
                    AccountAddCommand(interpreter.accountCommandInterpreter),
                    AccountListCommand(interpreter.accountCommandInterpreter),
                ),
            PlaidCommand
                .subcommands(
                    PlaidConfigureCommand(interpreter.plaidCommandInterpreter),
                ),
            TransactionsCommand
                .subcommands(
                    TransactionListCommand(interpreter.transactionCommandInterpreter),
                ),
        )
        .main(args)
}
