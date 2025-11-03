package browser

import arrow.core.Either

interface BrowserLauncher {
    suspend fun openUrl(url: String): Either<String, Unit>
}
