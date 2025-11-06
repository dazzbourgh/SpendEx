package interpreter

import arrow.core.raise.either
import command.BankDetails
import config.ConfigDao
import config.Constants
import dao.AccountDao
import dao.TokenDao
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import plaid.PlaidService
import plaid.PlaidToken

class AccountCommandInterpreterImpl(
    private val accountDao: AccountDao,
    private val tokenDao: TokenDao,
    private val plaidService: PlaidService,
    private val configDao: ConfigDao,
    private val now: suspend () -> Instant = { Clock.System.now() },
) : AccountCommandInterpreter {
    override suspend fun addAccount(username: String) =
        either {
            val config = configDao.loadPlaidConfig().bind()
            val linkToken = plaidService.createLinkToken(username).bind()

            val publicToken =
                plaidService.performLinkFlow(
                    linkToken,
                    Constants.OAuth.REDIRECT_URL,
                    Constants.OAuth.DEFAULT_PORT,
                ).bind()

            val tokenResponse = plaidService.exchangePublicToken(publicToken).bind()
            val accountsResponse = plaidService.getAccounts(tokenResponse.accessToken).bind()

            val institutionName = accountsResponse.accounts.firstOrNull()?.name ?: Constants.Plaid.UNKNOWN_BANK

            val bankDetails =
                BankDetails(
                    name = institutionName,
                    username = username,
                    dateAdded = now(),
                )

            accountDao.save(bankDetails).bind()

            val plaidToken =
                PlaidToken(
                    bankName = institutionName,
                    accessToken = tokenResponse.accessToken,
                    itemId = tokenResponse.itemId,
                    createdAt = now(),
                )
            tokenDao.save(plaidToken)
        }.fold(
            { error -> println("${Constants.Commands.ErrorMessages.ACCOUNT_ADD_FAILED}: $error") },
            { println(Constants.Commands.ErrorMessages.ACCOUNT_ADD_SUCCESS) },
        )

    override suspend fun listAccounts(): Iterable<BankDetails> =
        accountDao.list().fold({ error ->
            println("${Constants.Commands.ErrorMessages.ACCOUNT_LIST_FAILED}: $error")
            emptyList()
        }, { it })
}
