package interpreter

import dao.JsonAccountDaoImpl

object InterpreterFactory {
    fun get(env: String): Interpreter =
        when (env) {
            "prod" -> {
                val accountDao = JsonAccountDaoImpl()
                InterpreterImpl(
                    AccountCommandInterpreterImpl(accountDao),
                )
            }
            else -> throw IllegalArgumentException("Unknown environment: $env")
        }
}
