package plaid

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import config.Constants
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
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

    override suspend fun startAndWaitForCallback(
        port: Int,
        linkToken: String,
    ): Either<String, String> =
        try {
            server =
                embeddedServer(CIO, port = port, host = Constants.OAuth.SERVER_HOST) {
                    routing {
                        // Serve the Plaid Link HTML page
                        get(Constants.OAuth.LINK_PATH) {
                            val html =
                                Constants.OAuth.Html.LINK_PAGE
                                    .replace("{{LINK_TOKEN}}", linkToken)
                                    .replace("{{REDIRECT_URL}}", Constants.OAuth.REDIRECT_URL)

                            call.respondText(
                                html,
                                contentType = ContentType.Text.Html,
                                status = HttpStatusCode.OK,
                            )
                        }

                        // Handle OAuth callbacks
                        get(Constants.OAuth.CALLBACK_PATH) {
                            val publicToken = call.request.queryParameters[Constants.OAuth.PUBLIC_TOKEN_PARAM]
                            val oauthStateId = call.request.queryParameters[Constants.OAuth.OAUTH_STATE_ID_PARAM]

                            when {
                                publicToken != null -> {
                                    // Final callback with public_token - complete successfully
                                    call.respondText(
                                        Constants.OAuth.Html.SUCCESS_PAGE,
                                        contentType = ContentType.Text.Html,
                                        status = HttpStatusCode.OK,
                                    )
                                    resultDeferred.complete(publicToken.right())
                                }

                                oauthStateId != null -> {
                                    // OAuth institution - redirect back to Plaid Link to continue
                                    // Get the full URL that was received (protocol + host + port + path + query)
                                    val receivedRedirectUri =
                                        "http://${Constants.OAuth.SERVER_HOST}${Constants.OAuth.PORT_DELIMITER}$port" +
                                            call.request.local.uri

                                    // Build the Plaid Link continuation URL with URL-encoded parameters
                                    val continuationUrl =
                                        URLBuilder(Constants.Plaid.LINK_URL).apply {
                                            parameters.append("token", linkToken)
                                            parameters.append("receivedRedirectUri", receivedRedirectUri)
                                        }.buildString()

                                    val html =
                                        Constants.OAuth.Html.OAUTH_CONTINUATION_PAGE
                                            .replace("%s", continuationUrl, ignoreCase = false)

                                    call.respondText(
                                        html,
                                        contentType = ContentType.Text.Html,
                                        status = HttpStatusCode.OK,
                                    )
                                    // Don't complete - wait for the second callback with public_token
                                }

                                else -> {
                                    // No token or state ID received - error
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
                }

            server?.start(wait = false)

            withTimeout(Constants.OAuth.TIMEOUT_MILLIS) {
                resultDeferred.await()
            }
        } catch (_: TimeoutCancellationException) {
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
