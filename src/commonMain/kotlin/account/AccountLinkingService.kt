package account

import arrow.core.Either

/**
 * Links new institution connections through a configured default provider.
 */
interface AccountLinkingService {
    /**
     * Links a new institution connection using the configured default provider.
     *
     * @return Either an error message or Unit on success
     */
    suspend fun linkDefaultAccount(): Either<String, Unit>
}
