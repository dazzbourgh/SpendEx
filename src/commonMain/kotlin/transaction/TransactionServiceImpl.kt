package transaction

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parMap
import dao.InstitutionConnectionRepository
import dao.TransactionDao
import kotlinx.datetime.LocalDate
import model.InstitutionConnection
import model.StoredTransactions
import model.Transaction
import provider.FinancialProviderRegistry

/**
 * Default transaction query and sync service.
 *
 * @property institutionConnectionRepository Local storage for linked institution connections
 * @property transactionDao Local transaction cache
 * @property providerRegistry Registry used to dispatch sync work to provider-specific implementations
 */
class TransactionServiceImpl(
    private val institutionConnectionRepository: InstitutionConnectionRepository,
    private val transactionDao: TransactionDao,
    private val providerRegistry: FinancialProviderRegistry,
) : TransactionService {
    override suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>> =
        either {
            // Get all linked institution connections.
            val connections = institutionConnectionRepository.list()

            // Filter by institutions if specified.
            val filteredConnections =
                if (institutions.isNotEmpty()) {
                    connections.filter { it.institutionName in institutions }
                } else {
                    connections
                }

            // Process each connection in parallel: load local transactions and conditionally fetch new ones.
            filteredConnections
                .parMap { connection ->
                    getTransactionsForConnection(connection, from, to).bind()
                }.flatten()
        }

    private suspend fun getTransactionsForConnection(
        connection: InstitutionConnection,
        from: LocalDate?,
        to: LocalDate?,
    ): Either<String, List<Transaction>> =
        either {
            val savedTransactions =
                transactionDao.loadTransactions(
                    providerId = connection.providerId,
                    connectionId = connection.connectionId,
                )
            // Load existing transactions from local storage.
            val localDomainTransactions =
                savedTransactions
                    ?.transactions
                    .orEmpty()
                    .map { it.toDomainModel(connection.institutionName) }

            // Determine the effective date range.
            val effectiveFrom = from ?: localDomainTransactions.minByOrNull { it.date }?.date
            val latestLocalDate = localDomainTransactions.maxByOrNull { it.date }?.date

            // Fetch when the caller wants the latest data or there is no local cache for this connection yet.
            val needsFetch = latestLocalDate == null || to == null || to > latestLocalDate

            // If we need to fetch, sync new transactions from the owning provider.
            val allTransactions =
                if (needsFetch) {
                    syncTransactionsForConnection(connection).bind()
                } else {
                    localDomainTransactions
                }

            // Filter by date range.
            allTransactions.filter { transaction ->
                val matchesFrom = effectiveFrom == null || transaction.date >= effectiveFrom
                val matchesTo = to == null || transaction.date <= to
                matchesFrom && matchesTo
            }
        }

    private suspend fun syncTransactionsForConnection(connection: InstitutionConnection): Either<String, List<Transaction>> =
        either {
            val transactionSyncProvider =
                providerRegistry.getTransactionSyncProvider(connection.providerId)
                    ?: raise("Transaction sync is not available for provider: ${connection.providerId}")

            // Load existing transactions to get cursor.
            val existing =
                transactionDao.loadTransactions(
                    providerId = connection.providerId,
                    connectionId = connection.connectionId,
                )
            var cursor = existing?.cursor
            val allNewTransactions = mutableListOf<model.StoredTransaction>()

            // Sync until no more transactions.
            do {
                val response = transactionSyncProvider.syncTransactions(connection, cursor).bind()
                allNewTransactions.addAll(response.addedTransactions)
                cursor = response.nextCursor
            } while (response.hasMore)

            // Combine existing transactions with new ones, avoiding duplicates.
            val allStoredTransactions =
                (existing?.transactions.orEmpty() + allNewTransactions)
                    .distinctBy { it.transactionId }

            // Save combined transactions with the latest cursor.
            val transactionsToSave =
                StoredTransactions(
                    cursor = cursor,
                    transactions = allStoredTransactions,
                )
            transactionDao.saveTransactions(
                providerId = connection.providerId,
                connectionId = connection.connectionId,
                storedTransactions = transactionsToSave,
            )

            // Convert to domain model and return.
            allStoredTransactions.map { it.toDomainModel(connection.institutionName) }
        }
}
