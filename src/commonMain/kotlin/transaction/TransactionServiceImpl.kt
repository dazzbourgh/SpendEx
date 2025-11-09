package transaction

import arrow.core.Either
import arrow.core.right
import command.Transaction
import kotlinx.datetime.LocalDate

class TransactionServiceImpl : TransactionService {
    override suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>> =
        // Dummy implementation - returns empty list
        emptyList<Transaction>().right()
}
