package interpreter

import arrow.core.Either
import kotlinx.datetime.LocalDate
import model.Transaction

interface TransactionCommandInterpreter {
    suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>>
}
