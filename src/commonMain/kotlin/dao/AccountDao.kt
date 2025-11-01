package dao

import command.BankDetails

interface AccountDao {
    suspend fun save(bankDetails: BankDetails)

    suspend fun list(): Iterable<BankDetails>
}
