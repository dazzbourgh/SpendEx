import account.AccountAddCommand
import account.AccountCommand
import cli.FinancialAdvisor
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    FinancialAdvisor
        .subcommands(
            AccountCommand
                .subcommands(
                    AccountAddCommand {
                        println("Adding account: $it")
                    },
                ),
        )
        .main(args)
}
