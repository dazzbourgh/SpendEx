package command

import com.github.ajalt.clikt.core.CliktCommand

/**
 * Main CLI application entry point using Clikt.
 * Delegates execution to the provided CommandExecutor.
 */
object RootCommand : CliktCommand(
    name = "spndx",
    help = "Spendex - CLI tool for managing financial accounts and transactions",
) {
    override fun run() = Unit
}
