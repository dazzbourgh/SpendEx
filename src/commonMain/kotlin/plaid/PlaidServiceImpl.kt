package plaid

import arrow.core.Either
import arrow.core.raise.either
import browser.BrowserLauncher
import config.ConfigDao
import config.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PlaidServiceImpl(
    private val httpClient: HttpClient,
    private val configDao: ConfigDao,
    private val browserLauncher: BrowserLauncher,
    private val oauthRedirectServerFactory: () -> OAuthRedirectServer,
    private val environmentConfig: config.EnvironmentConfig,
) : PlaidService {
    override suspend fun createLinkToken(): Either<String, String> =
        either {
            val config = configDao.loadPlaidConfig().bind()
            val request =
                LinkTokenCreateRequest(
                    clientId = config.client_id,
                    secret = config.secret,
                    clientName = Constants.Plaid.CLIENT_NAME,
                    user = LinkTokenUser(clientUserId = Constants.Plaid.CLIENT_USER_ID),
                    products = listOf(PlaidProduct.AUTH, PlaidProduct.TRANSACTIONS),
                    countryCodes = listOf(PlaidCountryCode.US),
                    language = Constants.Plaid.LANGUAGE,
                    redirectUri = Constants.OAuth.REDIRECT_URL,
                )

            try {
                httpClient.post("${environmentConfig.plaidBaseUrl}${Constants.Plaid.Endpoints.LINK_TOKEN_CREATE}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidLinkTokenResponse>().linkToken
            } catch (exception: Exception) {
                "${Constants.Plaid.ErrorMessages.LINK_TOKEN_FAILED}: ${exception.message}"
            }
        }

    override suspend fun exchangePublicToken(publicToken: String): Either<String, PlaidAccessTokenResponse> =
        either {
            val config = configDao.loadPlaidConfig().bind()
            val request =
                PublicTokenExchangeRequest(
                    clientId = config.client_id,
                    secret = config.secret,
                    publicToken = publicToken,
                )

            try {
                httpClient.post("${environmentConfig.plaidBaseUrl}${Constants.Plaid.Endpoints.PUBLIC_TOKEN_EXCHANGE}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidAccessTokenResponse>()
            } catch (exception: Exception) {
                raise("${Constants.Plaid.ErrorMessages.TOKEN_EXCHANGE_FAILED}: ${exception.message}")
            }
        }

    override suspend fun getAccounts(accessToken: String): Either<String, PlaidAccountsResponse> =
        either {
            val config = configDao.loadPlaidConfig().bind()
            val request =
                AccountsGetRequest(
                    clientId = config.client_id,
                    secret = config.secret,
                    accessToken = accessToken,
                )

            try {
                httpClient.post("${environmentConfig.plaidBaseUrl}${Constants.Plaid.Endpoints.ACCOUNTS_GET}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidAccountsResponse>()
            } catch (exception: Exception) {
                raise("${Constants.Plaid.ErrorMessages.ACCOUNTS_GET_FAILED}: ${exception.message}")
            }
        }

    override suspend fun performLinkFlow(
        linkToken: String,
        redirectUrl: String,
        port: Int,
    ): Either<String, String> =
        either {
            coroutineScope {
                oauthRedirectServerFactory().use { server ->
                    val serverDeferred = async { server.startAndWaitForCallback(port, linkToken) }
                    browserLauncher.openPlaidLink(linkToken, redirectUrl).bind()
                    println(Constants.OAuth.Messages.WAITING_FOR_AUTH)
                    serverDeferred.await().bind()
                }
            }
        }

    override suspend fun saveConfig(
        clientId: String,
        clientSecret: String,
    ): Either<String, Unit> {
        val plaidConfig = config.PlaidConfig(client_id = clientId, secret = clientSecret)
        return configDao.savePlaidConfig(plaidConfig)
    }
}
