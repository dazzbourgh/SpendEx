package model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Details about a bank account.
 *
 * @property name The name of the bank
 * @property dateAdded The timestamp when the account was added
 */
@Serializable
data class BankDetails(
    val name: String,
    val dateAdded: Instant,
)
