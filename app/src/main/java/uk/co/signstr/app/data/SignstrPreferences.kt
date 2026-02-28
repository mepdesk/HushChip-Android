package uk.co.signstr.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SignstrPreferences {
    private const val PREFS_NAME = "signstr"
    private const val KEY_IDENTITIES = "identities"
    private const val KEY_CONNECTIONS = "connections"
    private const val KEY_EVENT_LOG = "event_log"
    private const val KEY_ACTIVE_IDENTITY_ID = "active_identity_id"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_DEFAULT_RELAYS = "default_relays"

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isFirstLaunch(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FIRST_LAUNCH, true)

    fun setFirstLaunchDone(context: Context) =
        prefs(context).edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()

    fun getActiveIdentityId(context: Context): String? =
        prefs(context).getString(KEY_ACTIVE_IDENTITY_ID, null)

    fun setActiveIdentityId(context: Context, id: String) =
        prefs(context).edit().putString(KEY_ACTIVE_IDENTITY_ID, id).apply()

    fun getIdentities(context: Context): List<SignstrIdentity> {
        val raw = prefs(context).getString(KEY_IDENTITIES, null) ?: return emptyList()
        return try { json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
    }

    fun saveIdentities(context: Context, identities: List<SignstrIdentity>) =
        prefs(context).edit().putString(KEY_IDENTITIES, json.encodeToString(identities)).apply()

    fun getConnections(context: Context): List<SignstrConnection> {
        val raw = prefs(context).getString(KEY_CONNECTIONS, null) ?: return emptyList()
        return try { json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
    }

    fun saveConnections(context: Context, connections: List<SignstrConnection>) =
        prefs(context).edit().putString(KEY_CONNECTIONS, json.encodeToString(connections)).apply()

    fun getEventLog(context: Context): List<EventLogEntry> {
        val raw = prefs(context).getString(KEY_EVENT_LOG, null) ?: return emptyList()
        return try { json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
    }

    fun saveEventLog(context: Context, entries: List<EventLogEntry>) {
        val trimmed = if (entries.size > 500) entries.takeLast(500) else entries
        prefs(context).edit().putString(KEY_EVENT_LOG, json.encodeToString(trimmed)).apply()
    }

    fun getDefaultRelays(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_DEFAULT_RELAYS, null)
        return if (raw != null) {
            try { json.decodeFromString(raw) } catch (_: Exception) { DEFAULT_RELAYS }
        } else DEFAULT_RELAYS
    }

    fun saveDefaultRelays(context: Context, relays: List<String>) =
        prefs(context).edit().putString(KEY_DEFAULT_RELAYS, json.encodeToString(relays)).apply()

    val DEFAULT_RELAYS = listOf(
        "wss://relay.damus.io",
        "wss://relay.primal.net",
        "wss://relay.nostr.band"
    )
}
