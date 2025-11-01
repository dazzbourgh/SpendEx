package interpreter

import command.Bank
import command.BankDetails
import kotlinx.datetime.Clock

class AccountCommandInterpreterImpl : AccountCommandInterpreter {
    // Mock storage for added accounts with timestamps
    private val accounts = mutableSetOf<BankDetails>()

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
        accounts.add(bankDetails)
        println("Account for ${bank.name} with username '$username' added successfully")
    }

    override suspend fun listAccounts(): Iterable<BankDetails> {
        if (accounts.isEmpty()) {
            println("No accounts added yet")
        } else {
            println("Added accounts:")
            accounts.forEachIndexed { index, details ->
                println("  ${index + 1}. ${details.name} - ${details.username} (added: ${details.dateAdded})")
            }
        }
        return accounts.toList()
    }
}
