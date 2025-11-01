package interpreter

import dao.JsonAccountDaoImpl
import dao.JsonTokenDaoImpl
import plaid.MockPlaidServiceImpl

object InterpreterFactory {
    fun get(env: String): Interpreter =
        when (env) {
            "prod" -> {
                val accountDao = JsonAccountDaoImpl()
                val tokenDao = JsonTokenDaoImpl()
                val plaidService = MockPlaidServiceImpl()
                InterpreterImpl(
                    AccountCommandInterpreterImpl(accountDao, tokenDao, plaidService),
                )
            }
            else -> throw IllegalArgumentException("Unknown environment: $env")
        }
}
