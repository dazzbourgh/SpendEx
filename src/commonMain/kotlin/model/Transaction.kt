package model

import kotlinx.datetime.LocalDate
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
