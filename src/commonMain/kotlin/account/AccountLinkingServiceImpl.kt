package account

import arrow.core.Either
import arrow.core.raise.either
import dao.InstitutionConnectionRepository
import provider.FinancialProviderRegistry

/**
 * Default account-linking application service.
 *
 * @property defaultProviderId Provider used by commands that do not yet accept an explicit provider argument
 * @property providerRegistry Provider capability registry
 * @property institutionConnectionRepository Local storage for linked institution connections
 */
class AccountLinkingServiceImpl(
    private val defaultProviderId: String,
    private val providerRegistry: FinancialProviderRegistry,
    private val institutionConnectionRepository: InstitutionConnectionRepository,
) : AccountLinkingService {
    override suspend fun linkDefaultAccount(): Either<String, Unit> =
        either {
            val accountLinkingProvider =
                providerRegistry.getAccountLinkingProvider(defaultProviderId)
                    ?: raise("Account linking is not available for provider: $defaultProviderId")
            val connection = accountLinkingProvider.linkAccount().bind()
            institutionConnectionRepository.save(connection)
        }
}
