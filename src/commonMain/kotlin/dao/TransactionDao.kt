package dao

import model.StoredTransactions

interface TransactionDao {
    /**
     * Loads stored transactions for a specific provider-owned connection.
     *
     * @param providerId Stable provider identifier that owns the connection
     * @param connectionId Stable provider-scoped connection identifier used for local transaction storage
     * @return The stored transactions, or null if no file exists
     */
    suspend fun loadTransactions(
        providerId: String,
        connectionId: String,
    ): StoredTransactions?

    /**
     * Saves transactions for a specific provider-owned connection.
     * Merges new transactions with existing ones to avoid duplicates.
     *
     * @param providerId Stable provider identifier that owns the connection
     * @param connectionId Stable provider-scoped connection identifier used for local transaction storage
     * @param storedTransactions The transactions to save
     */
    suspend fun saveTransactions(
        providerId: String,
        connectionId: String,
        storedTransactions: StoredTransactions,
    )
}
