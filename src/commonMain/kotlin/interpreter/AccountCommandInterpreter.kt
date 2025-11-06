package interpreter

import arrow.core.Either
import command.BankDetails

interface AccountCommandInterpreter {
    suspend fun addAccount(username: String): Either<String, Unit>

    suspend fun listAccounts(): Either<String, Iterable<BankDetails>>
}
