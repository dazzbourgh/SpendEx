package interpreter

import command.BankDetails

interface AccountCommandInterpreter {
    suspend fun addAccount(username: String)

    suspend fun listAccounts(): Iterable<BankDetails>
}
