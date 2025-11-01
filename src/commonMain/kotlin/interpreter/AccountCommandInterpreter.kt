package interpreter

import account.Bank

interface AccountCommandInterpreter {
    suspend fun addAccount(bank: Bank)
}
