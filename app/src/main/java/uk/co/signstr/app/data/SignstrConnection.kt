package uk.co.signstr.app.data

import kotlinx.serialization.Serializable

@Serializable
data class SignstrConnection(
    val id: String,
    val identityId: String,
    val clientPubkeyHex: String,
    val clientName: String = "",
    val relays: List<String>,
    val secret: String,
    val createdAt: Long,
    val isActive: Boolean = true
)
