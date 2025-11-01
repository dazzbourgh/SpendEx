package interpreter

import dao.AccountDaoImpl

object InterpreterFactory {
    fun get(env: String): Interpreter =
        when (env) {
            "prod" -> {
                val accountDao = AccountDaoImpl()
                InterpreterImpl(
                    AccountCommandInterpreterImpl(accountDao),
                )
            }
            else -> throw IllegalArgumentException("Unknown environment: $env")
        }
}
