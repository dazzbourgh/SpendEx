package interpreter

import arrow.core.Either
import arrow.core.raise.either
import config.Constants
import dao.ConfigDao
import kotlinx.datetime.LocalDate
import model.Transaction
import transaction.TransactionService
import validation.ValidationHelper.ensurePlaidConfigValid

class TransactionCommandInterpreterImpl(
    private val transactionService: TransactionService,
    private val configDao: ConfigDao,
) : TransactionCommandInterpreter {
    override suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>> =
        either {
            ensurePlaidConfigValid(configDao)

            // Validate date range
            if (from != null && to != null && from > to) {
                raise(Constants.Commands.ErrorMessages.INVALID_DATE_RANGE)
            }

            transactionService.listTransactions(from, to, institutions).bind()
        }
}
