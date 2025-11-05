package browser

import arrow.core.Either

interface BrowserLauncher {
    suspend fun openUrl(url: String): Either<String, Unit>

    suspend fun openPlaidLink(
        linkToken: String,
        redirectUrl: String,
    ): Either<String, Unit>
}
