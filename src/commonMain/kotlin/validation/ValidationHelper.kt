package validation

import arrow.core.raise.Raise
import config.PlaidConfig

/**
 * Shared validation helpers for user-provided and persisted configuration.
 */
object ValidationHelper {
    private const val INCOMPLETE_PLAID_CONFIGURATION_MESSAGE =
        "Plaid configuration is incomplete. Please run: spndx plaid configure --client-id <id> --client-secret <secret>"

    /**
     * Ensures the provided Plaid credentials are non-blank before they are persisted.
     *
     * @param clientId Plaid client identifier
     * @param clientSecret Plaid client secret
     */
    fun Raise<String>.ensurePlaidCredentialsValid(
        clientId: String,
        clientSecret: String,
    ) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            raise(INCOMPLETE_PLAID_CONFIGURATION_MESSAGE)
        }
    }

    /**
     * Ensures the persisted Plaid configuration is usable before making API calls.
     *
     * @param config Persisted Plaid configuration to validate
     */
    fun Raise<String>.ensurePlaidConfigValid(config: PlaidConfig) {
        ensurePlaidCredentialsValid(config.client_id, config.secret)
    }
}
