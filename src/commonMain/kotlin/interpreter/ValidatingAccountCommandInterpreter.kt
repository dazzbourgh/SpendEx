package interpreter

import arrow.core.Either
import arrow.core.raise.either
import command.BankDetails
import config.ConfigDao

class ValidatingAccountCommandInterpreter(
    private val delegate: AccountCommandInterpreter,
    private val configDao: ConfigDao,
) : AccountCommandInterpreter {
    private suspend fun <T> validatingConfig(block: suspend () -> Either<String, T>): Either<String, T> =
        either {
            val config = configDao.loadPlaidConfig().bind()
            if (config.client_id.isBlank() || config.secret.isBlank()) {
                raise(
                    "Plaid configuration is incomplete. Please run: spndx plaid configure --client-id <id> --client-secret <secret>",
                )
            }
            block().bind()
        }

    override suspend fun addAccount(): Either<String, Unit> = validatingConfig { delegate.addAccount() }

    override suspend fun listAccounts(): Either<String, Iterable<BankDetails>> = validatingConfig { delegate.listAccounts() }
}
