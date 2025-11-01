package interpreter

import command.Bank
import command.BankDetails
import dao.AccountDao
import dao.TokenDao
import kotlinx.datetime.Clock
import plaid.PlaidService
import plaid.PlaidToken

class AccountCommandInterpreterImpl(
    private val accountDao: AccountDao,
    private val tokenDao: TokenDao,
    private val plaidService: PlaidService,
) : AccountCommandInterpreter {
    override suspend fun addAccount(
        bank: Bank,
        username: String,
    ) {
        println("Initiating Plaid authorization for ${bank.name}...")

        // Step 1: Create link token
        val linkTokenResult = plaidService.createLinkToken(bank, username)
        linkTokenResult.fold(
            { error ->
                println("Error creating link token: $error")
                return
            },
            { linkToken ->
                println("Link token created: ${linkToken.take(20)}...")

                // Step 2: Simulate public token (in real app, user would complete Plaid Link UI)
                // For now, we use the link token as a simulated public token
                val publicToken = "public-sandbox-simulated-${linkToken.takeLast(10)}"
                println("Simulated public token obtained")

                // Step 3: Exchange public token for access token
                val exchangeResult = plaidService.exchangePublicToken(publicToken)
                exchangeResult.fold(
                    { error ->
                        println("Error exchanging token: $error")
                        return
                    },
                    { tokenResponse ->
                        // Step 4: Store the access token
                        val plaidToken =
                            PlaidToken(
                                bankName = bank.name,
                                accessToken = tokenResponse.accessToken,
                                itemId = tokenResponse.itemId,
                                createdAt = Clock.System.now(),
                            )
                        tokenDao.save(plaidToken)
                        println("Access token stored securely")

                        // Step 5: Fetch account details
                        val accountsResult = plaidService.getAccounts(tokenResponse.accessToken)
                        accountsResult.fold(
                            { error ->
                                println("Error fetching accounts: $error")
                                return
                            },
                            { accountsResponse ->
                                // Step 6: Save account details
                                val accountName = accountsResponse.accounts.firstOrNull()?.name ?: "Primary Account"
                                val bankDetails =
                                    BankDetails(
                                        name = bank.name,
                                        username = username,
                                        dateAdded = Clock.System.now(),
                                    )
                                accountDao.save(bankDetails)
                                println("Account '$accountName' for ${bank.name} with username '$username' added successfully")
                                println("Found ${accountsResponse.accounts.size} account(s) linked to this institution")
                            },
                        )
                    },
                )
            },
        )
    }

    override suspend fun listAccounts(): Iterable<BankDetails> {
        val accounts = accountDao.list()
        if (accounts.none()) {
            println("No accounts added yet")
        } else {
            println("Added accounts:")
            accounts.forEachIndexed { index, details ->
                println("  ${index + 1}. ${details.name} - ${details.username} (added: ${details.dateAdded})")
            }
        }
        return accounts
    }
}
