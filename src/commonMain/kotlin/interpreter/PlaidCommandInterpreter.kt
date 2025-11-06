package interpreter

interface PlaidCommandInterpreter {
    suspend fun configure(
        clientId: String,
        clientSecret: String,
    )
}
