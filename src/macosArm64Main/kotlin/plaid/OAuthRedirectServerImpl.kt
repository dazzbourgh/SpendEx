package plaid

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class OAuthRedirectServerImpl : OAuthRedirectServer {
    private var server: ApplicationEngine? = null
    private val resultDeferred = CompletableDeferred<Either<String, String>>()

    override suspend fun startAndWaitForCallback(port: Int): Either<String, String> =
        try {
            server =
                embeddedServer(CIO, port = port, host = "127.0.0.1") {
                    routing {
                        get("/") {
                            val publicToken = call.request.queryParameters["public_token"]

                            if (publicToken != null) {
                                call.respondText(
                                    """
                                    <!DOCTYPE html>
                                    <html>
                                    <head><title>Authorization Successful</title></head>
                                    <body>
                                        <h1>Authorization Successful!</h1>
                                        <p>You can close this window and return to the terminal.</p>
                                    </body>
                                    </html>
                                    """.trimIndent(),
                                    contentType = ContentType.Text.Html,
                                    status = HttpStatusCode.OK,
                                )
                                resultDeferred.complete(publicToken.right())
                            } else {
                                call.respondText(
                                    """
                                    <!DOCTYPE html>
                                    <html>
                                    <head><title>Authorization Failed</title></head>
                                    <body>
                                        <h1>Authorization Failed</h1>
                                        <p>No public token received. Please try again.</p>
                                    </body>
                                    </html>
                                    """.trimIndent(),
                                    contentType = ContentType.Text.Html,
                                    status = HttpStatusCode.BadRequest,
                                )
                                resultDeferred.complete("No public token received in callback".left())
                            }
                        }
                    }
                }

            server?.start(wait = false)

            // Wait for callback with 5-minute timeout
            withTimeout(300_000) {
                resultDeferred.await()
            }
        } catch (e: TimeoutCancellationException) {
            "Authorization timed out after 5 minutes".left()
        } catch (e: Exception) {
            "Failed to start OAuth redirect server: ${e.message}".left()
        }

    override fun close() {
        server?.stop(1000, 2000)
        if (!resultDeferred.isCompleted) {
            resultDeferred.complete("Server closed before receiving callback".left())
        }
    }
}
