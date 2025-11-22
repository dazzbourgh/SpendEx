package command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import config.Constants

/**
 * Main CLI application entry point using Clikt.
 * Delegates execution to the provided CommandExecutor.
 */
class RootCommand : CliktCommand(
    name = Constants.App.NAME,
    help = Constants.App.DESCRIPTION,
) {
    val environment by option(
        "--environment",
        help = "Environment to use (sandbox or prod)",
    ).choice(
        Constants.App.ENVIRONMENT_SANDBOX,
        Constants.App.ENVIRONMENT_PROD,
    ).default(Constants.App.ENVIRONMENT_PROD)

    override fun run() = Unit
}
