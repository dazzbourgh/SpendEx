package command

import arrow.core.Either
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import config.Constants
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate

object TransactionsCommand : CliktCommand(
    name = Constants.Commands.Transactions.NAME,
    help = Constants.Commands.Transactions.HELP,
) {
    override fun run() = Unit
}

class TransactionListCommand(
    private val listCommand: suspend (LocalDate?, LocalDate?, Set<String>) -> Either<String, Iterable<Transaction>>,
) : CliktCommand(
        name = Constants.Commands.Transactions.List.NAME,
        help = Constants.Commands.Transactions.List.HELP,
    ) {
    private val from by option("--from").help(Constants.Commands.Transactions.List.FROM_HELP)
    private val to by option("--to").help(Constants.Commands.Transactions.List.TO_HELP)
    private val institutions by option("--institution").multiple().help(Constants.Commands.Transactions.List.INSTITUTION_HELP)

    override fun run() =
        runBlocking {
            val fromDate = from?.let { parseDate(it) }
            val toDate = to?.let { parseDate(it) }

            if (fromDate == null && from != null) {
                println("${Constants.Commands.ErrorMessages.INVALID_DATE_FORMAT}: $from")
                return@runBlocking
            }

            if (toDate == null && to != null) {
                println("${Constants.Commands.ErrorMessages.INVALID_DATE_FORMAT}: $to")
                return@runBlocking
            }

            listCommand(fromDate, toDate, institutions.toSet()).fold(
                { error -> println("${Constants.Commands.ErrorMessages.TRANSACTION_LIST_FAILED}: $error") },
                { transactions ->
                    if (transactions.none()) {
                        println("No transactions found")
                    } else {
                        transactions.forEach { transaction ->
                            println(
                                "${transaction.date} | ${transaction.institutionName} | ${transaction.description} | ${transaction.amount}",
                            )
                        }
                    }
                },
            )
        }

    private fun parseDate(dateString: String): LocalDate? =
        try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
}
