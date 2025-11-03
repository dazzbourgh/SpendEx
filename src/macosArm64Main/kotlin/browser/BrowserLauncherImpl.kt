package browser

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.pclose
import platform.posix.popen

@OptIn(ExperimentalForeignApi::class)
class BrowserLauncherImpl : BrowserLauncher {
    override suspend fun openUrl(url: String): Either<String, Unit> =
        try {
            val command = "open \"$url\""
            val process = popen(command, "r") ?: return "Failed to execute open command".left()

            val exitCode = pclose(process)

            if (exitCode == 0) {
                Unit.right()
            } else {
                "Browser launch failed with exit code $exitCode".left()
            }
        } catch (e: Exception) {
            "Failed to open browser: ${e.message}".left()
        }
}
