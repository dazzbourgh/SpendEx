package interpreter

import arrow.core.Either
import arrow.core.raise.either
import command.BankDetails
import config.Constants
import dao.AccountDao
import dao.ConfigDao
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
    override suspend fun addAccount(): Either<String, Unit> =
        either {
            val config = configDao.loadPlaidConfig().bind()
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

            val plaidToken =
                PlaidToken(
                    bankName = institutionName,
                    accessToken = tokenResponse.accessToken,
                    itemId = tokenResponse.itemId,
                    createdAt = now(),
                )
            tokenDao.save(plaidToken)
        }

    override suspend fun listAccounts(): Either<String, Iterable<BankDetails>> = accountDao.list()
}
