package dao

import model.PlaidToken

interface TokenDao {
    suspend fun save(token: PlaidToken)

    suspend fun list(): Iterable<PlaidToken>

    suspend fun findByBankName(bankName: String): PlaidToken?

    suspend fun delete(bankName: String)
}
