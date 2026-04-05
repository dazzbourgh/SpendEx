package interpreter

import account.AccountLinkingService
import account.AccountService
import arrow.core.Either
import model.BankDetails

/**
 * Command interpreter for account-related commands.
 *
 * @property accountLinkingService Application service responsible for linking accounts
 * @property accountService Query service for listing linked accounts
 */
class AccountCommandInterpreterImpl(
    private val accountLinkingService: AccountLinkingService,
    private val accountService: AccountService,
) : AccountCommandInterpreter {
    override suspend fun addAccount(): Either<String, Unit> = accountLinkingService.linkDefaultAccount()

    override suspend fun listAccounts(): Either<String, Iterable<BankDetails>> = accountService.listAccounts()
}
