package command

import com.github.ajalt.clikt.core.CliktCommand
import config.Constants

/**
 * Main CLI application entry point using Clikt.
 * Environment parsing is handled in Main.kt before interpreter creation.
 */
class RootCommand : CliktCommand(
    name = Constants.App.NAME,
    help = Constants.App.DESCRIPTION,
) {
    override fun run() = Unit
}
