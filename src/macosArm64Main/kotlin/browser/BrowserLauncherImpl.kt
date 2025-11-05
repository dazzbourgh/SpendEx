package browser

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import config.Constants
import dao.FileSystemHelper
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Clock
import platform.posix.pclose
import platform.posix.popen

@OptIn(ExperimentalForeignApi::class)
class BrowserLauncherImpl : BrowserLauncher {
    companion object {
        private const val LINK_HTML_TEMPLATE = """<!DOCTYPE html>
<html>
<head>
<script src="https://cdn.plaid.com/link/v2/stable/link-initialize.js"></script>
</head>
<body>
<script>
const linkToken = "{{LINK_TOKEN}}";
const redirectBase = "{{REDIRECT_URL}}";

// Check if this is a redirect from OAuth
const urlParams = new URLSearchParams(window.location.search);
const oauthStateId = urlParams.get('oauth_state_id');

const config = {
  token: linkToken,
  onSuccess: function(public_token, metadata) {
    const redirectUrl = redirectBase + "?public_token=" + encodeURIComponent(public_token);
    console.log("Redirecting to:", redirectUrl);
    window.location.href = redirectUrl;
  },
  onExit: function(err, metadata) {
    console.log("User exited Plaid Link", err, metadata);
  }
};

// If returning from OAuth, include the receivedRedirectUri
if (oauthStateId) {
  config.receivedRedirectUri = window.location.href;
}

const handler = Plaid.create(config);
handler.open();
</script>
</body>
</html>"""
    }

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
            // Substitute placeholders in HTML template
            val htmlContent =
                LINK_HTML_TEMPLATE
                    .replace("{{LINK_TOKEN}}", linkToken)
                    .replace("{{REDIRECT_URL}}", redirectUrl)

            // Create temp file path with current timestamp
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val tempFilePath = "/tmp/spendex-link-$timestamp.html"

            // Write HTML content to temp file
            FileSystemHelper.writeFile(tempFilePath, htmlContent)

            // Open the file with default browser
            openUrl("file://$tempFilePath")
        } catch (e: Exception) {
            "${Constants.Browser.ErrorMessages.OPEN_FAILED}: ${e.message}".left()
        }
}
