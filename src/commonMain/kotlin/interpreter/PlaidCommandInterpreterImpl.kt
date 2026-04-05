package interpreter

import config.Constants
import plaid.PlaidConfigurationService

/**
 * Command interpreter for Plaid-specific configuration commands.
 *
 * @property plaidConfigurationService Service responsible for saving Plaid credentials
 */
class PlaidCommandInterpreterImpl(
    private val plaidConfigurationService: PlaidConfigurationService,
) : PlaidCommandInterpreter {
    override suspend fun configure(
        clientId: String,
        clientSecret: String,
    ) {
        plaidConfigurationService.configure(clientId, clientSecret).fold(
            { error -> println("${Constants.Commands.ErrorMessages.PLAID_CONFIGURE_FAILED}: $error") },
            { println(Constants.Commands.ErrorMessages.PLAID_CONFIGURE_SUCCESS) },
        )
    }
}
