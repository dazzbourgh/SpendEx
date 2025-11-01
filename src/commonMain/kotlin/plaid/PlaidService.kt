package plaid

import arrow.core.Either
import command.Bank

/**
 * Service for interacting with Plaid API.
 */
interface PlaidService {
    /**
     * Creates a link token for initiating Plaid Link flow.
     *
     * @param bank The bank to link
     * @param username The username for the account
     * @return Either an error message or the link token
     */
    suspend fun createLinkToken(
        bank: Bank,
        username: String,
    ): Either<String, String>

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
}
