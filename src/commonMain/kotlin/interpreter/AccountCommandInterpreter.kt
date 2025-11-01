package interpreter

import command.Bank

interface AccountCommandInterpreter {
    suspend fun addAccount(bank: Bank)
}
