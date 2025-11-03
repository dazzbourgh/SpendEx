package config

import kotlinx.serialization.Serializable

@Serializable
data class PlaidConfig(
    val client_id: String,
    val secret: String,
    val redirect_url: String,
)
