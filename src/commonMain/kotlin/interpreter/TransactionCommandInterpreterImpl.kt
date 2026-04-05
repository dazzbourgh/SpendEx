package interpreter

import arrow.core.Either
import arrow.core.raise.either
import config.Constants
import kotlinx.datetime.LocalDate
import model.Transaction
import transaction.TransactionService

/**
 * Command interpreter for transaction queries.
 *
 * @property transactionService Application service for listing transactions
 */
class TransactionCommandInterpreterImpl(
    private val transactionService: TransactionService,
) : TransactionCommandInterpreter {
    override suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>> =
        either {
            // Validate date range
            if (from != null && to != null && from > to) {
                raise(Constants.Commands.ErrorMessages.INVALID_DATE_RANGE)
            }

            transactionService.listTransactions(from, to, institutions).bind()
        }
}
