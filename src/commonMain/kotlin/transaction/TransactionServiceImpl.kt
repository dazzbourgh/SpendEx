package transaction

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parMap
import dao.TokenDao
import kotlinx.datetime.LocalDate
import model.PlaidRemovedTransaction
import model.PlaidToken
import model.Transaction
import plaid.PlaidService

class TransactionServiceImpl(
    private val tokenDao: TokenDao,
    private val plaidService: PlaidService,
) : TransactionService {
    override suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>> =
        either {
            val tokens = tokenDao.list()
            val filteredTokens =
                if (institutions.isNotEmpty()) {
                    tokens.filter { it.bankName in institutions }
                } else {
                    tokens
                }

            filteredTokens
                .parMap { token ->
                    fetchTransactionsForToken(token)
                        .bind()
                        .filter { transaction ->
                            val matchesFrom = from == null || transaction.date >= from
                            val matchesTo = to == null || transaction.date <= to
                            matchesFrom && matchesTo
                        }
                }.flatten()
        }

    private suspend fun fetchTransactionsForToken(token: PlaidToken): Either<String, List<Transaction>> =
        either {
            var cursor: String? = null
            val transactionsById = linkedMapOf<String, Transaction>()

            do {
                val response = plaidService.syncTransactions(token.accessToken, cursor).bind()

                (response.added + response.modified).forEach { plaidTransaction ->
                    transactionsById[plaidTransaction.transactionId] = plaidTransaction.toDomainModel(token.bankName)
                }
                removeTransactions(response.removed, transactionsById)

                cursor = response.nextCursor
            } while (response.hasMore)

            transactionsById.values.toList()
        }

    private fun removeTransactions(
        removedTransactions: List<PlaidRemovedTransaction>,
        transactionsById: MutableMap<String, Transaction>,
    ) {
        removedTransactions.forEach { removedTransaction ->
            transactionsById.remove(removedTransaction.transactionId)
        }
    }
}
