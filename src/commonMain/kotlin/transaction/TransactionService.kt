package transaction

import arrow.core.Either
import kotlinx.datetime.LocalDate
import model.Transaction

interface TransactionService {
    suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>>
}
