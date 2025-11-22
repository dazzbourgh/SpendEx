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
import interpreter.InterpreterFactory

fun main(args: Array<String>) {
    // Parse environment flag first by creating a temporary root command
    val rootCmd = RootCommand()

    // Pre-parse to get environment (we need to parse twice unfortunately due to Clikt's design)
    // First parse to extract environment
    val environment = extractEnvironment(args)

    // Create interpreter based on environment
    val interpreter = InterpreterFactory.get(environment)

    // Set up full command tree with interpreter
    rootCmd
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

/**
 * Extract environment flag from args, defaulting to prod.
 */
private fun extractEnvironment(args: Array<String>): String {
    val envIndex = args.indexOf("--environment")
    return if (envIndex != -1 && envIndex + 1 < args.size) {
        when (args[envIndex + 1]) {
            Constants.App.ENVIRONMENT_SANDBOX -> Constants.App.ENVIRONMENT_SANDBOX
            Constants.App.ENVIRONMENT_PROD -> Constants.App.ENVIRONMENT_PROD
            else -> Constants.App.ENVIRONMENT_PROD
        }
    } else {
        Constants.App.ENVIRONMENT_PROD
    }
}
