package interpreter

import arrow.core.Either
import command.Transaction
import kotlinx.datetime.LocalDate
import transaction.TransactionService

class TransactionCommandInterpreterImpl(
    private val transactionService: TransactionService,
) : TransactionCommandInterpreter {
    override suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>> = transactionService.listTransactions(from, to, institutions)
}
