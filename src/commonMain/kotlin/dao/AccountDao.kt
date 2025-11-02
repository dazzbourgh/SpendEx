package dao

import arrow.core.Either
import command.BankDetails

interface AccountDao {
    suspend fun save(bankDetails: BankDetails): Either<String, Unit>

    suspend fun list(): Either<String, Iterable<BankDetails>>
}
