package dao

import model.StoredTransactions

interface TransactionDao {
    /**
     * Loads stored transactions for a specific item ID.
     *
     * @param itemId The Plaid item ID
     * @return The stored transactions, or null if no file exists
     */
    suspend fun loadTransactions(itemId: String): StoredTransactions?

    /**
     * Saves transactions for a specific item ID.
     * Merges new transactions with existing ones to avoid duplicates.
     *
     * @param itemId The Plaid item ID
     * @param storedTransactions The transactions to save
     */
    suspend fun saveTransactions(
        itemId: String,
        storedTransactions: StoredTransactions,
    )
}
