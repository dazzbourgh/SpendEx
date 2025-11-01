package plaid

import arrow.core.Either
import arrow.core.right
import command.Bank
import kotlin.random.Random

/**
 * Mock implementation of PlaidService for development and testing.
 * Simulates Plaid API responses without making actual network calls.
 */
class MockPlaidServiceImpl : PlaidService {
    override suspend fun createLinkToken(
        bank: Bank,
        username: String,
    ): Either<String, String> {
        // Simulate link token creation
        val linkToken = "link-sandbox-${generateRandomId()}"
        println("Mock: Created link token for ${bank.name}")
        println("Mock: In a real implementation, user would complete Plaid Link UI")
        println("Mock: Using simulated public token for testing")
        return linkToken.right()
    }

    override suspend fun exchangePublicToken(publicToken: String): Either<String, PlaidAccessTokenResponse> {
        // Simulate public token exchange
        val accessToken = "access-sandbox-${generateRandomId()}"
        val itemId = "item-sandbox-${generateRandomId()}"

        val response =
            PlaidAccessTokenResponse(
                accessToken = accessToken,
                itemId = itemId,
                requestId = generateRandomId(),
            )

        println("Mock: Exchanged public token for access token")
        return response.right()
    }

    override suspend fun getAccounts(accessToken: String): Either<String, PlaidAccountsResponse> {
        // Simulate account retrieval
        val accounts =
            listOf(
                PlaidAccount(
                    accountId = "account-${generateRandomId()}",
                    name = "Checking Account",
                    type = "depository",
                    subtype = "checking",
                ),
                PlaidAccount(
                    accountId = "account-${generateRandomId()}",
                    name = "Savings Account",
                    type = "depository",
                    subtype = "savings",
                ),
            )

        val response =
            PlaidAccountsResponse(
                accounts = accounts,
                requestId = generateRandomId(),
            )

        println("Mock: Retrieved ${accounts.size} accounts")
        accounts.forEach { println("  - ${it.name} (${it.type}/${it.subtype})") }
        return response.right()
    }

    private fun generateRandomId(): String = Random.nextLong(100000000, 999999999).toString()
}
