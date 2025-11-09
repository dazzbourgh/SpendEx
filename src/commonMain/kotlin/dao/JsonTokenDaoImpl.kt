package dao

import model.PlaidToken

expect class JsonTokenDaoImpl() : TokenDao {
    override suspend fun save(token: PlaidToken)

    override suspend fun list(): Iterable<PlaidToken>

    override suspend fun findByBankName(bankName: String): PlaidToken?

    override suspend fun delete(bankName: String)
}
