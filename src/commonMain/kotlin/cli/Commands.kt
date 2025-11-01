package cli

import arrow.core.Either
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

/**
 * Main CLI application entry point using Clikt.
 * Delegates execution to the provided CommandExecutor.
 */
object FinancialAdvisor : CliktCommand(
    name = "financial-advisor",
    help = "CLI tool for managing financial accounts and transactions"
) {
    override fun run() = Unit
}
