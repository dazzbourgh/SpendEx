package command

import com.github.ajalt.clikt.core.CliktCommand
import config.Constants

/**
 * Main CLI application entry point using Clikt.
 * Delegates execution to the provided CommandExecutor.
 */
object RootCommand : CliktCommand(
    name = Constants.App.NAME,
    help = Constants.App.DESCRIPTION,
) {
    override fun run() = Unit
}
