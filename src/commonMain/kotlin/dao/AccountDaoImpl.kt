package dao

import arrow.core.Either
import arrow.core.right
import command.BankDetails

class AccountDaoImpl : AccountDao {
    private val accounts = mutableSetOf<BankDetails>()

    override suspend fun save(bankDetails: BankDetails): Either<String, Unit> = accounts.add(bankDetails).right().map { }

    override suspend fun list(): Either<String, Iterable<BankDetails>> = accounts.toList().right()
}
