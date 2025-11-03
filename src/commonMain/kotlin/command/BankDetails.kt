package command

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Details about a bank account.
 *
 * @property name The name of the bank
 * @property username The app user identifier (not the bank login username)
 * @property dateAdded The timestamp when the account was added
 */
@Serializable
data class BankDetails(
    val name: String,
    val username: String,
    val dateAdded: Instant,
)
