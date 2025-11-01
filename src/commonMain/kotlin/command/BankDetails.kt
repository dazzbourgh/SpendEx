package command

import kotlinx.datetime.Instant

/**
 * Details about a bank account.
 *
 * @property name The name of the bank
 * @property dateAdded The timestamp when the account was added
 */
data class BankDetails(
    val name: String,
    val dateAdded: Instant,
)
