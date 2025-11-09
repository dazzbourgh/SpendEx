package transaction

import account.AccountService
import arrow.core.Either
import arrow.core.raise.either
import dao.TokenDao
import dao.TransactionDao
import kotlinx.datetime.LocalDate
import model.PlaidTransaction
import model.StoredTransaction
import model.StoredTransactions
import model.Transaction
import plaid.PlaidService

class TransactionServiceImpl(
    private val accountService: AccountService,
    private val tokenDao: TokenDao,
    private val transactionDao: TransactionDao,
    private val plaidService: PlaidService,
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

            // Sync transactions for each token in parallel using Arrow-KT
            val syncResults =
                filteredTokens.map { token ->
                    syncTransactionsForToken(token)
                }

            // Extract successful results and flatten
            val allTransactionsList =
                syncResults
                    .mapNotNull { eitherResult ->
                        eitherResult.getOrNull()
                    }.flatten()

            // Filter by date range if specified using functional approach
            allTransactionsList.filter { transaction ->
                val dateMatches =
                    when {
                        from != null && to != null -> transaction.date in from..to
                        from != null -> transaction.date >= from
                        to != null -> transaction.date <= to
                        else -> true
                    }
                dateMatches
            }
        }

    private suspend fun syncTransactionsForToken(token: model.PlaidToken): Either<String, List<Transaction>> =
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
            val storedTransactions =
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

            // Save transactions with the latest cursor
            val transactionsToSave =
                StoredTransactions(
                    cursor = cursor,
                    transactions = storedTransactions,
                )
            transactionDao.saveTransactions(token.itemId, transactionsToSave)

            // Convert to domain model and return
            storedTransactions.map { it.toDomainModel(token.bankName) }
        }
}
