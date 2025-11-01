package interpreter

import command.Bank
import command.BankDetails
import dao.AccountDao
import kotlinx.datetime.Clock

class AccountCommandInterpreterImpl(
    private val accountDao: AccountDao,
) : AccountCommandInterpreter {
    override suspend fun addAccount(
        bank: Bank,
        username: String,
    ) {
        val bankDetails =
            BankDetails(
                name = bank.name,
                username = username,
                dateAdded = Clock.System.now(),
            )
        accountDao.save(bankDetails)
        println("Account for ${bank.name} with username '$username' added successfully")
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
