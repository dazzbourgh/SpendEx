package browser

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import config.Constants
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.pclose
import platform.posix.popen

@OptIn(ExperimentalForeignApi::class)
class BrowserLauncherImpl : BrowserLauncher {
    override suspend fun openUrl(url: String): Either<String, Unit> =
        try {
            val command = "${Constants.Browser.MACOS_OPEN_COMMAND} \"$url\""
            val process =
                popen(command, Constants.FileSystem.FILE_MODE_READ)
                    ?: return Constants.Browser.ErrorMessages.COMMAND_FAILED.left()

            val exitCode = pclose(process)

            if (exitCode == Constants.Browser.SUCCESS_EXIT_CODE) {
                Unit.right()
            } else {
                "${Constants.Browser.ErrorMessages.LAUNCH_FAILED} $exitCode".left()
            }
        } catch (e: Exception) {
            "${Constants.Browser.ErrorMessages.OPEN_FAILED}: ${e.message}".left()
        }

    override suspend fun openPlaidLink(
        linkToken: String,
        redirectUrl: String,
    ): Either<String, Unit> =
        try {
            // Build the server link URL
            val linkUrl =
                "http://${Constants.OAuth.SERVER_HOST}${Constants.OAuth.PORT_DELIMITER}" +
                    "${Constants.OAuth.DEFAULT_PORT}${Constants.OAuth.LINK_PATH}"

            // Open the server URL in browser
            openUrl(linkUrl)
        } catch (e: Exception) {
            "${Constants.Browser.ErrorMessages.OPEN_FAILED}: ${e.message}".left()
        }
}
