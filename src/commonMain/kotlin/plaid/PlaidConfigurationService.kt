package plaid

import arrow.core.Either

/**
 * Persists Plaid API credentials for Plaid-specific commands.
 */
interface PlaidConfigurationService {
    /**
     * Saves Plaid API credentials.
     *
     * @param clientId Plaid client identifier
     * @param clientSecret Plaid client secret
     * @return Either an error message or Unit on success
     */
    suspend fun configure(
        clientId: String,
        clientSecret: String,
    ): Either<String, Unit>
}
