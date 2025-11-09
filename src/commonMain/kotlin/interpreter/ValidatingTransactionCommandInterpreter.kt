package interpreter

import arrow.core.Either
import arrow.core.raise.either
import command.Transaction
import config.Constants
import dao.ConfigDao
import kotlinx.datetime.LocalDate

class ValidatingTransactionCommandInterpreter(
    private val delegate: TransactionCommandInterpreter,
    private val configDao: ConfigDao,
) : TransactionCommandInterpreter {
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

    override suspend fun listTransactions(
        from: LocalDate?,
        to: LocalDate?,
        institutions: Set<String>,
    ): Either<String, Iterable<Transaction>> =
        validatingConfig {
            either {
                // Validate date range
                if (from != null && to != null && from > to) {
                    raise(Constants.Commands.ErrorMessages.INVALID_DATE_RANGE)
                }
                delegate.listTransactions(from, to, institutions).bind()
            }
        }
}
