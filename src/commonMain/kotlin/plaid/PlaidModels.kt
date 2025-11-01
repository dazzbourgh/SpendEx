package plaid

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
