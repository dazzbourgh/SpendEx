package dao

import command.BankDetails

class AccountDaoImpl : AccountDao {
    private val accounts = mutableSetOf<BankDetails>()

    override suspend fun save(bankDetails: BankDetails) {
        accounts.add(bankDetails)
    }

    override suspend fun list(): Iterable<BankDetails> = accounts.toList()
}
