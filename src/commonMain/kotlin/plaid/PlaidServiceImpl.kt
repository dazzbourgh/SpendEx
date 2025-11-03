package plaid

import arrow.core.Either
import arrow.core.flatMap
import config.ConfigDao
import config.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PlaidServiceImpl(
    private val httpClient: HttpClient,
    private val configDao: ConfigDao,
) : PlaidService {
    override suspend fun createLinkToken(username: String): Either<String, String> =
        configDao.loadPlaidConfig().flatMap { config ->
            Either.catch {
                val request =
                    LinkTokenCreateRequest(
                        clientId = config.client_id,
                        secret = config.secret,
                        clientName = Constants.Plaid.CLIENT_NAME,
                        user = LinkTokenUser(clientUserId = username),
                        products = listOf(PlaidProduct.AUTH, PlaidProduct.TRANSACTIONS),
                        countryCodes = listOf(PlaidCountryCode.US),
                        language = Constants.Plaid.LANGUAGE,
                        redirectUri = config.redirect_url,
                    )

                httpClient.post("${Constants.Plaid.BASE_URL}${Constants.Plaid.Endpoints.LINK_TOKEN_CREATE}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidLinkTokenResponse>().linkToken
            }.mapLeft { exception ->
                "${Constants.Plaid.ErrorMessages.LINK_TOKEN_FAILED}: ${exception.message}"
            }
        }

    override suspend fun exchangePublicToken(publicToken: String): Either<String, PlaidAccessTokenResponse> =
        configDao.loadPlaidConfig().flatMap { config ->
            Either.catch {
                val request =
                    PublicTokenExchangeRequest(
                        clientId = config.client_id,
                        secret = config.secret,
                        publicToken = publicToken,
                    )

                httpClient.post("${Constants.Plaid.BASE_URL}${Constants.Plaid.Endpoints.PUBLIC_TOKEN_EXCHANGE}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidAccessTokenResponse>()
            }.mapLeft { exception ->
                "${Constants.Plaid.ErrorMessages.TOKEN_EXCHANGE_FAILED}: ${exception.message}"
            }
        }

    override suspend fun getAccounts(accessToken: String): Either<String, PlaidAccountsResponse> =
        configDao.loadPlaidConfig().flatMap { config ->
            Either.catch {
                val request =
                    AccountsGetRequest(
                        clientId = config.client_id,
                        secret = config.secret,
                        accessToken = accessToken,
                    )

                httpClient.post("${Constants.Plaid.BASE_URL}${Constants.Plaid.Endpoints.ACCOUNTS_GET}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidAccountsResponse>()
            }.mapLeft { exception ->
                "${Constants.Plaid.ErrorMessages.ACCOUNTS_GET_FAILED}: ${exception.message}"
            }
        }
}
