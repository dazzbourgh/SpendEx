package provider

import arrow.core.Either
import browser.BrowserLauncher
import config.EnvironmentConfig
import model.InstitutionConnection
import model.StoredTransaction
import plaid.OAuthRedirectServer

/**
 * Well-known provider identifiers used by the application.
 */
object ProviderIds {
    const val PLAID: String = "plaid"
}

/**
 * Shared platform and environment dependencies that provider modules may use to assemble their runtime graph.
 *
 * @property environmentConfig Environment-specific application configuration
 * @property browserLauncher Browser adapter used by providers that need interactive flows
 * @property oauthRedirectServerFactory Factory for provider-specific OAuth callback handling
 */
data class SharedProviderDependencies(
    val environmentConfig: EnvironmentConfig,
    val browserLauncher: BrowserLauncher,
    val oauthRedirectServerFactory: () -> OAuthRedirectServer,
)

/**
 * Creates provider-specific runtime capabilities from shared infrastructure.
 */
interface FinancialProviderModule {
    /**
     * Stable identifier of the provider managed by this module.
     */
    val providerId: String

    /**
     * Builds the runtime capabilities exposed by the provider.
     *
     * @param sharedDependencies Shared infrastructure available to all providers
     * @return Provider runtime containing only provider-neutral capabilities
     */
    fun createRuntime(sharedDependencies: SharedProviderDependencies): FinancialProviderRuntime
}

/**
 * Provider capability used to create new institution connections.
 */
interface AccountLinkingProvider {
    /**
     * Stable identifier of the owning provider.
     */
    val providerId: String

    /**
     * Starts the provider-specific linking flow and returns a persisted connection model on success.
     *
     * @return Either an error message or the new institution connection
     */
    suspend fun linkAccount(): Either<String, InstitutionConnection>
}

/**
 * Provider capability used to fetch incremental transaction updates for one connection.
 */
interface TransactionSyncProvider {
    /**
     * Stable identifier of the owning provider.
     */
    val providerId: String

    /**
     * Fetches one sync page for a connection.
     *
     * @param connection Linked institution connection to sync
     * @param cursor Opaque provider cursor for incremental sync
     * @return Either an error message or one sync page
     */
    suspend fun syncTransactions(
        connection: InstitutionConnection,
        cursor: String?,
    ): Either<String, TransactionSyncPage>
}

/**
 * Provider-neutral transaction sync page returned by a transaction sync provider.
 *
 * @property addedTransactions New or updated transactions returned by the provider
 * @property nextCursor Cursor to persist for the next incremental sync
 * @property hasMore Whether additional sync pages are available
 */
data class TransactionSyncPage(
    val addedTransactions: List<StoredTransaction>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

/**
 * Provider runtime assembled by a module and consumed by the application.
 *
 * @property providerId Stable identifier of the provider
 * @property accountLinkingProvider Optional account-linking capability
 * @property transactionSyncProvider Optional transaction-sync capability
 */
data class FinancialProviderRuntime(
    val providerId: String,
    val accountLinkingProvider: AccountLinkingProvider? = null,
    val transactionSyncProvider: TransactionSyncProvider? = null,
)

/**
 * Read-only lookup over all configured provider runtimes.
 */
interface FinancialProviderRegistry {
    /**
     * Returns the account-linking provider for the given id, if available.
     *
     * @param providerId Stable provider identifier
     * @return Matching account-linking provider, or null when not registered
     */
    fun getAccountLinkingProvider(providerId: String): AccountLinkingProvider?

    /**
     * Returns the transaction-sync provider for the given id, if available.
     *
     * @param providerId Stable provider identifier
     * @return Matching transaction-sync provider, or null when not registered
     */
    fun getTransactionSyncProvider(providerId: String): TransactionSyncProvider?
}

/**
 * Default immutable provider registry implementation.
 *
 * @param runtimes Provider runtimes keyed by provider id
 */
class FinancialProviderRegistryImpl(
    runtimes: Iterable<FinancialProviderRuntime>,
) : FinancialProviderRegistry {
    private val runtimesByProviderId: Map<String, FinancialProviderRuntime> = runtimes.associateBy { it.providerId }

    override fun getAccountLinkingProvider(providerId: String): AccountLinkingProvider? =
        runtimesByProviderId[providerId]?.accountLinkingProvider

    override fun getTransactionSyncProvider(providerId: String): TransactionSyncProvider? =
        runtimesByProviderId[providerId]?.transactionSyncProvider
}
