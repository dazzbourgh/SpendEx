package plaid

import arrow.core.Either
import arrow.core.raise.either
import config.PlaidConfig
import dao.ConfigDao
import validation.ValidationHelper

/**
 * Default Plaid configuration service.
 *
 * @property configDao Persistence adapter for Plaid credentials
 */
class PlaidConfigurationServiceImpl(
    private val configDao: ConfigDao,
) : PlaidConfigurationService {
    override suspend fun configure(
        clientId: String,
        clientSecret: String,
    ): Either<String, Unit> =
        either {
            ValidationHelper.run { ensurePlaidCredentialsValid(clientId, clientSecret) }
            configDao.savePlaidConfig(PlaidConfig(client_id = clientId, secret = clientSecret)).bind()
        }
}
