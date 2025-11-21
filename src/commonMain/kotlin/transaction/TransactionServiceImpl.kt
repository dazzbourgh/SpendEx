package transaction

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parMap
import dao.TokenDao
import dao.TransactionDao
import kotlinx.datetime.LocalDate
import model.PlaidToken
import model.PlaidTransaction
import model.StoredTransaction
import model.StoredTransactions
import model.Transaction
import plaid.PlaidService

class TransactionServiceImpl(
    private val tokenDao: TokenDao,
    private val transactionDao: TransactionDao,
    private val plaidService: PlaidService
) : TransactionService {
    override suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>> =
        either {
            // Get all tokens (accounts)
            val tokens = tokenDao.list()

            // Filter by institutions if specified
            val filteredTokens =
                if (institutions.isNotEmpty()) {
                    tokens.filter { it.bankName in institutions }
                } else {
                    tokens
                }

            // Process each token in parallel: load local transactions and conditionally fetch new ones
            filteredTokens
                .parMap { token ->
                    getTransactionsForToken(token, from, to).bind()
                }.flatten()
        }

    private suspend fun getTransactionsForToken(
        token: PlaidToken,
        from: LocalDate?,
        to: LocalDate?,
    ): Either<String, List<Transaction>> =
        either {
            val savedTransactions = transactionDao.loadTransactions(token.itemId)
            // Load existing transactions from local storage
            val localDomainTransactions =
                savedTransactions
                    ?.transactions
                    .orEmpty()
                    .map { it.toDomainModel(token.bankName) }

            // Determine the effective date range
            val effectiveFrom = from ?: localDomainTransactions.minByOrNull { it.date }?.date
            val latestLocalDate = localDomainTransactions.maxByOrNull { it.date }?.date

            // Check if we need to fetch new transactions
            val needsFetch = to == null || (latestLocalDate != null && to > latestLocalDate)

            // If we need to fetch, sync new transactions from Plaid
            val allTransactions =
                if (needsFetch) {
                    syncTransactionsForToken(token).bind()
                } else {
                    localDomainTransactions
                }

            // Filter by date range
            allTransactions.filter { transaction ->
                val matchesFrom = effectiveFrom == null || transaction.date >= effectiveFrom
                val matchesTo = to == null || transaction.date <= to
                matchesFrom && matchesTo
            }
        }

    private suspend fun syncTransactionsForToken(token: PlaidToken): Either<String, List<Transaction>> =
        either {
            // Load existing transactions to get cursor
            val existing = transactionDao.loadTransactions(token.itemId)
            var cursor = existing?.cursor
            val allNewTransactions = mutableListOf<PlaidTransaction>()

            // Sync until no more transactions
            do {
                val response = plaidService.syncTransactions(token.accessToken, cursor).bind()
                allNewTransactions.addAll(response.added)
                cursor = response.nextCursor
            } while (response.hasMore)

            // Convert to stored transactions
            val newStoredTransactions =
                allNewTransactions.map { plaidTx ->
                    StoredTransaction(
                        transactionId = plaidTx.transactionId,
                        amount = plaidTx.amount,
                        date = plaidTx.date,
                        name = plaidTx.name,
                        merchantName = plaidTx.merchantName,
                        category = plaidTx.category,
                        location = plaidTx.location,
                        pending = plaidTx.pending,
                        authorizedDate = plaidTx.authorizedDate,
                    )
                }

            // Combine existing transactions with new ones, avoiding duplicates
            val allStoredTransactions =
                (existing?.transactions.orEmpty() + newStoredTransactions)
                    .distinctBy { it.transactionId }

            // Save combined transactions with the latest cursor
            val transactionsToSave =
                StoredTransactions(
                    cursor = cursor,
                    transactions = allStoredTransactions,
                )
            transactionDao.saveTransactions(token.itemId, transactionsToSave)

            // Convert to domain model and return
            allStoredTransactions.map { it.toDomainModel(token.bankName) }
        }
}
