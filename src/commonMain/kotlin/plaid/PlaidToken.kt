package plaid

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a stored Plaid access token for a bank account.
 *
 * @property bankName The name of the bank
 * @property accessToken The Plaid access token
 * @property itemId The Plaid item ID
 * @property createdAt When the token was created
 */
@Serializable
data class PlaidToken(
    val bankName: String,
    val accessToken: String,
    val itemId: String,
    val createdAt: Instant,
)
