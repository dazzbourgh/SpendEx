package model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a linked financial institution connection managed by a specific provider.
 *
 * @property providerId Stable identifier of the provider that owns the connection
 * @property institutionName Display name of the linked institution
 * @property connectionId Stable identifier used for local storage and transaction sync state
 * @property providerState Opaque provider-owned state required to refresh transactions later
 * @property createdAt When the connection was created locally
 */
@Serializable
data class InstitutionConnection(
    val providerId: String,
    val institutionName: String,
    val connectionId: String,
    val providerState: String,
    val createdAt: Instant,
)
