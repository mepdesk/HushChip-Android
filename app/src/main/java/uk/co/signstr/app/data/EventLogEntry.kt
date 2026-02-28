package uk.co.signstr.app.data

import kotlinx.serialization.Serializable

@Serializable
data class EventLogEntry(
    val id: String,
    val identityId: String,
    val connectionId: String,
    val clientName: String,
    val method: String,
    val eventKind: Int? = null,
    val contentPreview: String = "",
    val approved: Boolean,
    val autoApproved: Boolean = false,
    val safeKindAutoApproved: Boolean = false,
    val timestamp: Long
) {
    val kindDescription: String get() = when (eventKind) {
        0 -> "Profile metadata"
        1 -> "Short note"
        2 -> "Relay list"
        3 -> "Contact list"
        4 -> "Encrypted DM"
        7 -> "Reaction"
        9735 -> "Zap receipt"
        22242 -> "Auth challenge"
        24133 -> "NIP-46 request"
        30023 -> "Long-form article"
        else -> eventKind?.let { "Event kind $it" } ?: method.replace("_", " ")
    }

    val statusBadge: String get() = when {
        !approved -> "REJECTED"
        safeKindAutoApproved -> "SAFE-AUTO"
        autoApproved -> "AUTO-APPROVED"
        else -> "APPROVED"
    }
}
