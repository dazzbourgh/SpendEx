package plaid

import arrow.core.Either
import arrow.core.raise.either
import config.Constants
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import model.InstitutionConnection
import provider.AccountLinkingProvider
import provider.ProviderIds

/**
 * Plaid-backed account-linking provider.
 *
 * @property plaidService Plaid service gateway
 * @property now Time source used when creating institution connections
 */
class PlaidAccountLinkingProvider(
    private val plaidService: PlaidService,
    private val now: suspend () -> Instant = { Clock.System.now() },
) : AccountLinkingProvider {
    override val providerId: String = ProviderIds.PLAID

    override suspend fun linkAccount(): Either<String, InstitutionConnection> =
        either {
            val linkToken = plaidService.createLinkToken().bind()
            val publicToken =
                plaidService.performLinkFlow(
                    linkToken,
                    Constants.OAuth.REDIRECT_URL,
                    Constants.OAuth.DEFAULT_PORT,
                ).bind()
            val tokenResponse = plaidService.exchangePublicToken(publicToken).bind()
            val accountsResponse = plaidService.getAccounts(tokenResponse.accessToken).bind()
            val institutionName = accountsResponse.accounts.firstOrNull()?.name ?: Constants.Plaid.UNKNOWN_BANK

            InstitutionConnection(
                providerId = providerId,
                institutionName = institutionName,
                connectionId = tokenResponse.itemId,
                providerState = tokenResponse.accessToken,
                createdAt = now(),
            )
        }
}
