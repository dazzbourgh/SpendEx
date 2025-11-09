package model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details about a financial transaction.
 *
 * @property id Unique identifier for the transaction
 * @property date The date of the transaction
 * @property description Description of the transaction
 * @property amount The transaction amount (positive for credit, negative for debit)
 * @property institutionName The name of the financial institution
 */
@Serializable
data class Transaction(
    val id: String,
    val date: LocalDate,
    val description: String,
    val amount: Double,
    val institutionName: String,
)

/**
 * Local storage model for transactions with cursor for pagination
 *
 * @property cursor The cursor for next sync request (nullable)
 * @property transactions List of transactions
 */
@Serializable
data class StoredTransactions(
    val cursor: String? = null,
    val transactions: List<StoredTransaction>,
)

/**
 * Stored transaction with all necessary fields for analysis
 *
 * @property transactionId Unique identifier from Plaid
 * @property amount Transaction amount
 * @property date Transaction date
 * @property name Transaction name/description
 * @property merchantName Merchant name if available
 * @property category Transaction categories
 * @property location Transaction location if available
 * @property pending Whether transaction is pending
 * @property authorizedDate Date when transaction was authorized
 */
@Serializable
data class StoredTransaction(
    @SerialName("transaction_id") val transactionId: String,
    val amount: Double,
    val date: String,
    val name: String,
    @SerialName("merchant_name") val merchantName: String?,
    val category: List<String>?,
    val location: PlaidLocation?,
    val pending: Boolean,
    @SerialName("authorized_date") val authorizedDate: String?,
) {
    /**
     * Converts a StoredTransaction to a Transaction domain model
     */
    fun toDomainModel(institutionName: String): Transaction =
        Transaction(
            id = transactionId,
            date = kotlinx.datetime.LocalDate.parse(date),
            description = merchantName ?: name,
            amount = amount,
            institutionName = institutionName,
        )
}
