package plaid

import arrow.core.Either

interface OAuthRedirectServer : AutoCloseable {
    suspend fun startAndWaitForCallback(port: Int): Either<String, String>
}
