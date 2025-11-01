package cli

import com.github.ajalt.clikt.core.CliktCommand

/**
 * Main CLI application entry point using Clikt.
 * Delegates execution to the provided CommandExecutor.
 */
object FinancialAdvisor : CliktCommand(
    name = "financial-advisor",
    help = "CLI tool for managing financial accounts and transactions",
) {
    override fun run() = Unit
}
