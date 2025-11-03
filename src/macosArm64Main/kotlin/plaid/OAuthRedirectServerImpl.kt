package plaid

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import config.Constants
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
                embeddedServer(CIO, port = port, host = Constants.OAuth.SERVER_HOST) {
                    routing {
                        get(Constants.OAuth.ROOT_PATH) {
                            val publicToken = call.request.queryParameters[Constants.OAuth.PUBLIC_TOKEN_PARAM]

                            if (publicToken != null) {
                                call.respondText(
                                    Constants.OAuth.Html.SUCCESS_PAGE,
                                    contentType = ContentType.Text.Html,
                                    status = HttpStatusCode.OK,
                                )
                                resultDeferred.complete(publicToken.right())
                            } else {
                                call.respondText(
                                    Constants.OAuth.Html.FAILURE_PAGE,
                                    contentType = ContentType.Text.Html,
                                    status = HttpStatusCode.BadRequest,
                                )
                                resultDeferred.complete(Constants.OAuth.Messages.NO_TOKEN_RECEIVED.left())
                            }
                        }
                    }
                }

            server?.start(wait = false)

            withTimeout(Constants.OAuth.TIMEOUT_MILLIS) {
                resultDeferred.await()
            }
        } catch (e: TimeoutCancellationException) {
            Constants.OAuth.Messages.TIMEOUT.left()
        } catch (e: Exception) {
            "${Constants.OAuth.Messages.SERVER_START_FAILED}: ${e.message}".left()
        }

    override fun close() {
        server?.stop(Constants.OAuth.SHUTDOWN_GRACE_MILLIS, Constants.OAuth.SHUTDOWN_TIMEOUT_MILLIS)
        if (!resultDeferred.isCompleted) {
            resultDeferred.complete(Constants.OAuth.Messages.SERVER_CLOSED_EARLY.left())
        }
    }
}
