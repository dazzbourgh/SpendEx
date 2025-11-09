package transaction

import arrow.core.Either
import command.Transaction
import kotlinx.datetime.LocalDate

interface TransactionService {
    suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>>
}
