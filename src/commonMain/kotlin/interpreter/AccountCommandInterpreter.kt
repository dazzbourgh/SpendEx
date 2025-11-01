package interpreter

import command.Bank
import command.BankDetails

interface AccountCommandInterpreter {
    suspend fun addAccount(bank: Bank, username: String)

    suspend fun listAccounts(): Iterable<BankDetails>
}
