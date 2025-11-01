package interpreter

import command.Bank
import command.BankDetails
import kotlinx.datetime.Clock

class AccountCommandInterpreterImpl : AccountCommandInterpreter {
    // Mock storage for added accounts with timestamps
    private val accounts = mutableSetOf<BankDetails>()

    override suspend fun addAccount(bank: Bank) {
        val bankDetails =
            BankDetails(
                name = bank.name,
                dateAdded = Clock.System.now(),
            )
        accounts.add(bankDetails)
        println("Account for ${bank.name} added successfully")
    }

    override suspend fun listAccounts(): Iterable<BankDetails> {
        if (accounts.isEmpty()) {
            println("No accounts added yet")
        } else {
            println("Added accounts:")
            accounts.forEachIndexed { index, details ->
                println("  ${index + 1}. ${details.name} (added: ${details.dateAdded})")
            }
        }
        return accounts.toList()
    }
}
