package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from Plaid's /link/token/create endpoint
 */
@Serializable
data class PlaidLinkTokenResponse(
    @SerialName("link_token") val linkToken: String,
    val expiration: String,
    @SerialName("request_id") val requestId: String,
)

/**
 * Response from Plaid's /item/public_token/exchange endpoint
 */
@Serializable
data class PlaidAccessTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("request_id") val requestId: String,
)

/**
 * Response from Plaid's /accounts/get endpoint
 */
@Serializable
data class PlaidAccountsResponse(
    val accounts: List<PlaidAccount>,
    @SerialName("request_id") val requestId: String,
)

/**
 * Plaid account information
 */
@Serializable
data class PlaidAccount(
    @SerialName("account_id") val accountId: String,
    val name: String,
    val type: String,
    val subtype: String?,
)

/**
 * Plaid product types
 */
@Serializable
enum class PlaidProduct {
    @SerialName("auth")
    AUTH,

    @SerialName("transactions")
    TRANSACTIONS,

    @SerialName("investments")
    INVESTMENTS,

    @SerialName("liabilities")
    LIABILITIES,
}

/**
 * Plaid country codes
 */
@Serializable
enum class PlaidCountryCode {
    @SerialName("US")
    US,

    @SerialName("CA")
    CA,

    @SerialName("GB")
    GB,
}

/**
 * User information for link token creation
 */
@Serializable
data class LinkTokenUser(
    @SerialName("client_user_id") val clientUserId: String,
)

/**
 * Request to create a link token
 */
@Serializable
data class LinkTokenCreateRequest(
    @SerialName("client_id") val clientId: String,
    val secret: String,
    @SerialName("client_name") val clientName: String,
    val user: LinkTokenUser,
    val products: List<PlaidProduct>,
    @SerialName("country_codes") val countryCodes: List<PlaidCountryCode>,
    val language: String,
    @SerialName("redirect_uri") val redirectUri: String,
)

/**
 * Request to exchange public token for access token
 */
@Serializable
data class PublicTokenExchangeRequest(
    @SerialName("client_id") val clientId: String,
    val secret: String,
    @SerialName("public_token") val publicToken: String,
)

/**
 * Request to get accounts
 */
@Serializable
data class AccountsGetRequest(
    @SerialName("client_id") val clientId: String,
    val secret: String,
    @SerialName("access_token") val accessToken: String,
)
