package interpreter

object InterpreterFactory {
    fun get(env: String): Interpreter =
        when (env) {
            "prod" ->
                InterpreterImpl(
                    AccountCommandInterpreterImpl(),
                )
            else -> throw IllegalArgumentException("Unknown environment: $env")
        }
}
