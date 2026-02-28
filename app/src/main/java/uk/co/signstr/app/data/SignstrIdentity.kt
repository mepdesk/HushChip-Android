package uk.co.signstr.app.data

import kotlinx.serialization.Serializable

@Serializable
data class SignstrIdentity(
    val id: String,
    val name: String,
    val pubkeyHex: String,
    val createdAt: Long,
    val safeKinds: List<Int> = DEFAULT_SAFE_KINDS,
    val autoApproveEnabled: Boolean = true
) {
    companion object {
        val DEFAULT_SAFE_KINDS = listOf(0, 3, 10000, 10001, 10002, 22242)
    }
}
