package dao

import model.StoredTransactions

expect class JsonTransactionDaoImpl() : TransactionDao {
    override suspend fun loadTransactions(itemId: String): StoredTransactions?

    override suspend fun saveTransactions(
        itemId: String,
        storedTransactions: StoredTransactions,
    )
}
