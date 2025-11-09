package account

import arrow.core.Either
import model.BankDetails

interface AccountService {
    suspend fun listAccounts(): Either<String, Iterable<BankDetails>>
}
