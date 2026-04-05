package plaid

import arrow.core.Either
import model.InstitutionConnection
import model.StoredTransaction
import model.TransactionLocation
import provider.ProviderIds
import provider.TransactionSyncPage
import provider.TransactionSyncProvider

/**
 * Plaid-backed transaction sync provider.
 *
 * @property plaidService Plaid service gateway
 */
class PlaidTransactionSyncProvider(
    private val plaidService: PlaidService,
) : TransactionSyncProvider {
    override val providerId: String = ProviderIds.PLAID

    override suspend fun syncTransactions(
        connection: InstitutionConnection,
        cursor: String?,
    ): Either<String, TransactionSyncPage> =
        plaidService
            .syncTransactions(connection.providerState, cursor)
            .map { response ->
                TransactionSyncPage(
                    addedTransactions =
                        response.added.map { plaidTransaction ->
                            StoredTransaction(
                                transactionId = plaidTransaction.transactionId,
                                amount = plaidTransaction.amount,
                                date = plaidTransaction.date,
                                name = plaidTransaction.name,
                                merchantName = plaidTransaction.merchantName,
                                category = plaidTransaction.category,
                                location =
                                    plaidTransaction.location?.let { location ->
                                        TransactionLocation(
                                            address = location.address,
                                            city = location.city,
                                            region = location.region,
                                            postalCode = location.postalCode,
                                            country = location.country,
                                            lat = location.lat,
                                            lon = location.lon,
                                            storeNumber = location.storeNumber,
                                        )
                                    },
                                pending = plaidTransaction.pending,
                                authorizedDate = plaidTransaction.authorizedDate,
                            )
                        },
                    nextCursor = response.nextCursor,
                    hasMore = response.hasMore,
                )
            }
}
