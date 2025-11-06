package interpreter

import config.Constants
import plaid.PlaidService

class PlaidCommandInterpreterImpl(
    private val plaidService: PlaidService,
) : PlaidCommandInterpreter {
    override suspend fun configure(
        clientId: String,
        clientSecret: String,
    ) {
        plaidService.saveConfig(clientId, clientSecret).fold(
            { error -> println("${Constants.Commands.ErrorMessages.PLAID_CONFIGURE_FAILED}: $error") },
            { println(Constants.Commands.ErrorMessages.PLAID_CONFIGURE_SUCCESS) },
        )
    }
}
