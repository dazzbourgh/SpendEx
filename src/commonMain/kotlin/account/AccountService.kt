package account

import arrow.core.Either
import command.BankDetails

interface AccountService {
    suspend fun listAccounts(): Either<String, Iterable<BankDetails>>
}
