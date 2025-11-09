package interpreter

import arrow.core.Either
import model.BankDetails

interface AccountCommandInterpreter {
    suspend fun addAccount(): Either<String, Unit>

    suspend fun listAccounts(): Either<String, Iterable<BankDetails>>
}
