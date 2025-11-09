package plaid

import arrow.core.Either
import model.PlaidAccessTokenResponse
import model.PlaidAccountsResponse
import model.PlaidTransactionsSyncResponse

/**
 * Service for interacting with Plaid API.
 */
interface PlaidService {
    /**
     * Creates a link token for initiating Plaid Link flow.
     * User will select the institution in the Plaid Link UI.
     *
     * @return Either an error message or the link token
     */
    suspend fun createLinkToken(): Either<String, String>

    /**
     * Exchanges a public token for an access token.
     *
     * @param publicToken The public token from Plaid Link
     * @return Either an error message or the access token response
     */
    suspend fun exchangePublicToken(publicToken: String): Either<String, PlaidAccessTokenResponse>

    /**
     * Retrieves account information using an access token.
     *
     * @param accessToken The Plaid access token
     * @return Either an error message or the accounts response
     */
    suspend fun getAccounts(accessToken: String): Either<String, PlaidAccountsResponse>

    /**
     * Syncs transactions using an access token and optional cursor.
     *
     * @param accessToken The Plaid access token
     * @param cursor Optional cursor for pagination
     * @return Either an error message or the transactions sync response
     */
    suspend fun syncTransactions(
        accessToken: String,
        cursor: String? = null,
    ): Either<String, PlaidTransactionsSyncResponse>

    /**
     * Performs the complete Plaid Link OAuth flow.
     * Opens the browser with Plaid Link, starts a local server to receive the callback,
     * and returns the public token when the user completes authentication.
     *
     * @param linkToken The link token for Plaid Link
     * @param redirectUrl The redirect URL for OAuth callbacks
     * @param port The port for the local OAuth redirect server
     * @return Either an error message or the public token
     */
    suspend fun performLinkFlow(
        linkToken: String,
        redirectUrl: String,
        port: Int,
    ): Either<String, String>

    /**
     * Saves Plaid API credentials to the configuration file.
     *
     * @param clientId The Plaid client ID
     * @param clientSecret The Plaid client secret
     * @return Either an error message or Unit on success
     */
    suspend fun saveConfig(
        clientId: String,
        clientSecret: String,
    ): Either<String, Unit>
}
