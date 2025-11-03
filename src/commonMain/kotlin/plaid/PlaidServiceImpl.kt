package plaid

import arrow.core.Either
import arrow.core.flatMap
import config.ConfigDao
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
    private val baseUrl = "https://sandbox.plaid.com"

    override suspend fun createLinkToken(username: String): Either<String, String> =
        configDao.loadPlaidConfig().flatMap { config ->
            Either.catch {
                val request =
                    LinkTokenCreateRequest(
                        clientId = config.client_id,
                        secret = config.secret,
                        clientName = "Spendex",
                        user = LinkTokenUser(clientUserId = username),
                        products = listOf(PlaidProduct.AUTH, PlaidProduct.TRANSACTIONS),
                        countryCodes = listOf(PlaidCountryCode.US),
                        language = "en",
                        redirectUri = config.redirect_url,
                    )

                httpClient.post("$baseUrl/link/token/create") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidLinkTokenResponse>().linkToken
            }.mapLeft { exception ->
                "Failed to create link token: ${exception.message}"
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

                httpClient.post("$baseUrl/item/public_token/exchange") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidAccessTokenResponse>()
            }.mapLeft { exception ->
                "Failed to exchange public token: ${exception.message}"
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

                httpClient.post("$baseUrl/accounts/get") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<PlaidAccountsResponse>()
            }.mapLeft { exception ->
                "Failed to get accounts: ${exception.message}"
            }
        }
}
