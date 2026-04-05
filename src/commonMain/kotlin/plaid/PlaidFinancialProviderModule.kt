package plaid

import dao.ConfigDao
import provider.FinancialProviderModule
import provider.FinancialProviderRuntime
import provider.ProviderIds
import provider.SharedProviderDependencies

/**
 * Provider module that assembles Plaid-backed runtime capabilities.
 *
 * @property configDao Persistence adapter for Plaid configuration
 */
class PlaidFinancialProviderModule(
    private val configDao: ConfigDao,
) : FinancialProviderModule {
    override val providerId: String = ProviderIds.PLAID

    override fun createRuntime(sharedDependencies: SharedProviderDependencies): FinancialProviderRuntime {
        val httpClient = HttpClientFactory.create()
        val plaidService =
            PlaidServiceImpl(
                httpClient = httpClient,
                configDao = configDao,
                browserLauncher = sharedDependencies.browserLauncher,
                oauthRedirectServerFactory = sharedDependencies.oauthRedirectServerFactory,
                environmentConfig = sharedDependencies.environmentConfig,
            )

        return FinancialProviderRuntime(
            providerId = providerId,
            accountLinkingProvider = PlaidAccountLinkingProvider(plaidService),
            transactionSyncProvider = PlaidTransactionSyncProvider(plaidService),
        )
    }
}
