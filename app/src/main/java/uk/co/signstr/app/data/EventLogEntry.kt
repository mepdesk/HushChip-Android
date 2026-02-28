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
    val timestamp: Long
)
