package dao

import model.StoredTransactions

expect class JsonTransactionDaoImpl() : TransactionDao {
    override suspend fun loadTransactions(
        providerId: String,
        connectionId: String,
    ): StoredTransactions?

    override suspend fun saveTransactions(
        providerId: String,
        connectionId: String,
        storedTransactions: StoredTransactions,
    )
}
