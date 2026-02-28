package uk.co.signstr.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class ApprovalPolicy(val label: String, val description: String, val durationSeconds: Long?) {
    ALWAYS_ASK("Always Ask", "Every request shows approval UI", null),
    TRUST_FOR_SESSION("Trust for Session", "Auto-approve after first approval until app restarts", null),
    TRUST_FOR_15_MIN("Trust for 15 min", "Auto-approve for 15 minutes after first approval", 15 * 60L),
    TRUST_FOR_1_HOUR("Trust for 1 hour", "Auto-approve for 1 hour after first approval", 60 * 60L),
    TRUST_FOR_4_HOURS("Trust for 4 hours", "Auto-approve for 4 hours after first approval", 4 * 60 * 60L),
    TRUST_FOR_24_HOURS("Trust for 24 hours", "Auto-approve for 24 hours after first approval", 24 * 60 * 60L),
    TRUST_FOR_7_DAYS("Trust for 7 days", "Auto-approve for 7 days after first approval", 7 * 24 * 60 * 60L),
    ALWAYS_TRUST("Always Trust", "Auto-approve everything (not recommended)", null);
}

object ApprovalPolicyStore {
    private const val KEY_POLICIES = "signstr.approval_policies"
    private const val KEY_FIRST_APPROVAL_TIMES = "signstr.first_approval_times"
    private const val KEY_DEFAULT_POLICY = "signstr.default_approval_policy"
    private val json = Json { ignoreUnknownKeys = true }

    private fun prefs(context: Context) =
        context.getSharedPreferences("signstr", Context.MODE_PRIVATE)

    fun getPolicy(context: Context, clientPubkey: String): ApprovalPolicy {
        val raw = prefs(context).getString(KEY_POLICIES, null) ?: return getDefaultPolicy(context)
        val map: Map<String, String> = try { json.decodeFromString(raw) } catch (_: Exception) { emptyMap() }
        val name = map[clientPubkey] ?: return getDefaultPolicy(context)
        return try { ApprovalPolicy.valueOf(name) } catch (_: Exception) { getDefaultPolicy(context) }
    }

    fun setPolicy(context: Context, clientPubkey: String, policy: ApprovalPolicy) {
        val raw = prefs(context).getString(KEY_POLICIES, null)
        val map: MutableMap<String, String> = if (raw != null) {
            try { json.decodeFromString<MutableMap<String, String>>(raw) } catch (_: Exception) { mutableMapOf() }
        } else mutableMapOf()
        map[clientPubkey] = policy.name
        prefs(context).edit().putString(KEY_POLICIES, json.encodeToString(map)).apply()
        clearFirstApproval(context, clientPubkey)
    }

    fun getDefaultPolicy(context: Context): ApprovalPolicy {
        val name = prefs(context).getString(KEY_DEFAULT_POLICY, null) ?: return ApprovalPolicy.ALWAYS_ASK
        return try { ApprovalPolicy.valueOf(name) } catch (_: Exception) { ApprovalPolicy.ALWAYS_ASK }
    }

    fun setDefaultPolicy(context: Context, policy: ApprovalPolicy) {
        prefs(context).edit().putString(KEY_DEFAULT_POLICY, policy.name).apply()
    }

    fun shouldAutoApprove(context: Context, clientPubkey: String): Boolean {
        val policy = getPolicy(context, clientPubkey)
        return when (policy) {
            ApprovalPolicy.ALWAYS_ASK -> false
            ApprovalPolicy.ALWAYS_TRUST -> true
            ApprovalPolicy.TRUST_FOR_SESSION -> getFirstApprovalTime(context, clientPubkey) != null
            else -> {
                val firstApproval = getFirstApprovalTime(context, clientPubkey) ?: return false
                val elapsed = (System.currentTimeMillis() / 1000) - firstApproval
                elapsed < (policy.durationSeconds ?: return false)
            }
        }
    }

    fun recordFirstApproval(context: Context, clientPubkey: String) {
        if (getFirstApprovalTime(context, clientPubkey) != null) return
        val raw = prefs(context).getString(KEY_FIRST_APPROVAL_TIMES, null)
        val map: MutableMap<String, Long> = if (raw != null) {
            try { json.decodeFromString(raw) } catch (_: Exception) { mutableMapOf() }
        } else mutableMapOf()
        map[clientPubkey] = System.currentTimeMillis() / 1000
        prefs(context).edit().putString(KEY_FIRST_APPROVAL_TIMES, json.encodeToString(map)).apply()
    }

    private fun getFirstApprovalTime(context: Context, clientPubkey: String): Long? {
        val raw = prefs(context).getString(KEY_FIRST_APPROVAL_TIMES, null) ?: return null
        val map: Map<String, Long> = try { json.decodeFromString(raw) } catch (_: Exception) { return null }
        return map[clientPubkey]
    }

    private fun clearFirstApproval(context: Context, clientPubkey: String) {
        val raw = prefs(context).getString(KEY_FIRST_APPROVAL_TIMES, null) ?: return
        val map: MutableMap<String, Long> = try { json.decodeFromString(raw) } catch (_: Exception) { return }
        map.remove(clientPubkey)
        prefs(context).edit().putString(KEY_FIRST_APPROVAL_TIMES, json.encodeToString(map)).apply()
    }

    fun removePolicy(context: Context, clientPubkey: String) {
        val raw = prefs(context).getString(KEY_POLICIES, null) ?: return
        val map: MutableMap<String, String> = try { json.decodeFromString(raw) } catch (_: Exception) { return }
        map.remove(clientPubkey)
        prefs(context).edit().putString(KEY_POLICIES, json.encodeToString(map)).apply()
        clearFirstApproval(context, clientPubkey)
    }
}
