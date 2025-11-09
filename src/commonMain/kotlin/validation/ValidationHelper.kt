package validation

import arrow.core.raise.Raise
import dao.ConfigDao

object ValidationHelper {
    suspend fun Raise<String>.ensurePlaidConfigValid(configDao: ConfigDao) {
        val config = configDao.loadPlaidConfig().bind()
        if (config.client_id.isBlank() || config.secret.isBlank()) {
            raise(
                "Plaid configuration is incomplete. Please run: spndx plaid configure --client-id <id> --client-secret <secret>",
            )
        }
    }
}
