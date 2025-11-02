package interpreter

import arrow.core.flatMap
import command.Bank
import command.BankDetails
import dao.AccountDao
import dao.TokenDao
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import plaid.PlaidService
import plaid.PlaidToken
import kotlin.collections.emptyList

class AccountCommandInterpreterImpl(
    private val accountDao: AccountDao,
    private val tokenDao: TokenDao,
    private val plaidService: PlaidService,
    private val now: suspend () -> Instant = { Clock.System.now() },
) : AccountCommandInterpreter {
    override suspend fun addAccount(
        bank: Bank,
        username: String,
    ) = plaidService.createLinkToken(bank, username).flatMap { linkToken ->
        val publicToken = "public-sandbox-simulated-${linkToken.takeLast(10)}"
        plaidService.exchangePublicToken(publicToken)
    }.flatMap { tokenResponse ->
        val plaidToken =
            PlaidToken(
                bankName = bank.name,
                accessToken = tokenResponse.accessToken,
                itemId = tokenResponse.itemId,
                createdAt = now(),
            )
        tokenDao.save(plaidToken)
        plaidService.getAccounts(tokenResponse.accessToken)
    }.flatMap { accountsResponse ->
        val bankDetails =
            BankDetails(
                name = bank.name,
                username = username,
                dateAdded = now(),
            )
        accountDao.save(bankDetails)
    }.fold(
        { error -> println("Error adding account: $error") },
        { it },
    )

    override suspend fun listAccounts(): Iterable<BankDetails> =
        accountDao.list().fold({ error ->
            println("Error listing accounts: $error")
            emptyList()
        }, { it })
}
