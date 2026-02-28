package uk.co.signstr.app.nip46

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.*
import uk.co.signstr.app.crypto.*
import uk.co.signstr.app.data.*
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NIP46Service(
    private val context: Context,
    private val onSigningRequest: (NIP46Request) -> Unit,
    private val onConnectionRequest: (SignstrConnection) -> Unit,
    private val onEventLogged: (EventLogEntry) -> Unit
) : NostrRelay.RelayListener {

    data class NIP46Request(
        val requestId: String,
        val method: String,
        val params: List<String>,
        val clientPubkey: String,
        val connection: SignstrConnection,
        val identityId: String,
        val eventKind: Int? = null
    )

    private val relays = ConcurrentHashMap<String, NostrRelay>()
    private val processedEventIds = ConcurrentHashMap.newKeySet<String>()
    // Map identity pubkey -> current bunker secret for connect validation
    private val pendingSecrets = ConcurrentHashMap<String, String>()
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val TAG = "NIP46Service"
        const val FALLBACK_RELAY = "wss://relay.damus.io"
        val SAFE_KINDS = setOf(0, 3, 10000, 10001, 10002, 22242)
    }

    fun start() {
        val identities = SignstrPreferences.getIdentities(context)
        val connections = SignstrPreferences.getConnections(context)
        if (identities.isEmpty()) return

        val allRelays = mutableSetOf(FALLBACK_RELAY)
        connections.filter { it.isActive }.forEach { conn -> allRelays.addAll(conn.relays) }
        allRelays.addAll(SignstrPreferences.getDefaultRelays(context))

        for (url in allRelays) {
            if (!relays.containsKey(url)) {
                val relay = NostrRelay(url, this)
                relays[url] = relay
                relay.connect()
            }
        }
    }

    fun stop() {
        relays.values.forEach { it.close() }
        relays.clear()
    }

    fun restart() {
        stop()
        start()
    }

    override fun onConnected(relay: String) {
        Log.d(TAG, "Relay connected: $relay")
        subscribeForIdentities(relay)
    }

    override fun onDisconnected(relay: String) {
        Log.d(TAG, "Relay disconnected: $relay")
    }

    override fun onError(relay: String, error: String) {
        Log.e(TAG, "Relay error on $relay: $error")
    }

    override fun onOk(relay: String, eventId: String, accepted: Boolean, message: String) {
        Log.d(TAG, "OK from $relay: eventId=$eventId accepted=$accepted $message")
    }

    override fun onEvent(relay: String, subscriptionId: String, eventJson: JsonObject) {
        val eventId = eventJson["id"]?.jsonPrimitive?.content ?: return
        if (!processedEventIds.add(eventId)) return // Deduplicate

        val kind = eventJson["kind"]?.jsonPrimitive?.int ?: return
        if (kind != 24133) return

        val senderPubkey = eventJson["pubkey"]?.jsonPrimitive?.content ?: return
        val content = eventJson["content"]?.jsonPrimitive?.content ?: return

        // Find which identity this is targeted at
        val pTags = eventJson["tags"]?.jsonArray?.filter {
            it.jsonArray[0].jsonPrimitive.content == "p"
        }?.map { it.jsonArray[1].jsonPrimitive.content } ?: return

        val identities = SignstrPreferences.getIdentities(context)
        for (identity in identities) {
            if (identity.pubkeyHex !in pTags) continue

            val privkey = SignstrKeyStore.loadPrivateKey(context, identity.id) ?: continue
            try {
                val convKey = NIP44.conversationKey(privkey, senderPubkey)
                val decrypted = NIP44.decrypt(convKey, content)
                Log.d(TAG, "Decrypted NIP-46 message from $senderPubkey: $decrypted")
                handleDecryptedMessage(decrypted, senderPubkey, identity, privkey)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decrypt from $senderPubkey: ${e.message}")
            } finally {
                privkey.fill(0)
            }
            return
        }
    }

    private fun handleDecryptedMessage(
        decrypted: String,
        clientPubkey: String,
        identity: SignstrIdentity,
        privkey: ByteArray
    ) {
        val msg = json.parseToJsonElement(decrypted).jsonObject
        val requestId = msg["id"]?.jsonPrimitive?.content ?: return
        val method = msg["method"]?.jsonPrimitive?.content ?: return
        // Params can contain JSON objects (e.g. sign_event sends an event object as a string)
        // or plain strings. Handle both cases.
        val params = msg["params"]?.jsonArray?.map { element ->
            if (element is JsonPrimitive) element.content
            else element.toString()
        } ?: emptyList()

        Log.d(TAG, "NIP-46 method=$method requestId=$requestId from=$clientPubkey")

        when (method) {
            "connect" -> handleConnect(requestId, params, clientPubkey, identity, privkey)
            "get_public_key" -> handleGetPublicKey(requestId, clientPubkey, identity, privkey)
            "sign_event" -> {
                val eventKind = try {
                    val eventObj = json.parseToJsonElement(params[0]).jsonObject
                    eventObj["kind"]?.jsonPrimitive?.int
                } catch (_: Exception) { null }

                val connection = findOrCreateConnection(clientPubkey, identity)
                val request = NIP46Request(
                    requestId = requestId,
                    method = method,
                    params = params,
                    clientPubkey = clientPubkey,
                    connection = connection,
                    identityId = identity.id,
                    eventKind = eventKind
                )

                // Auto-approve safe kinds
                if (identity.autoApproveEnabled && eventKind != null && eventKind in (identity.safeKinds)) {
                    handleSignEvent(requestId, params, clientPubkey, identity, privkey)
                    logEvent(identity, connection, method, eventKind, true)
                } else {
                    onSigningRequest(request)
                }
            }
            "nip44_encrypt", "nip44_decrypt", "nip04_encrypt", "nip04_decrypt" -> {
                val connection = findOrCreateConnection(clientPubkey, identity)
                val request = NIP46Request(
                    requestId = requestId,
                    method = method,
                    params = params,
                    clientPubkey = clientPubkey,
                    connection = connection,
                    identityId = identity.id
                )
                onSigningRequest(request)
            }
            else -> {
                Log.w(TAG, "Unsupported method: $method")
                sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey,
                    error = "Unsupported method: $method")
            }
        }
    }

    private fun handleConnect(
        requestId: String,
        params: List<String>,
        clientPubkey: String,
        identity: SignstrIdentity,
        privkey: ByteArray
    ) {
        // NIP-46 connect params: [<remote_user_pubkey>, <secret>]
        // params[0] = signer pubkey (echoed by client), params[1] = secret
        val secret = if (params.size > 1) params[1] else ""

        // Validate secret against our pending bunker secret
        val expectedSecret = pendingSecrets[identity.pubkeyHex]
        if (expectedSecret != null && secret.isNotEmpty() && secret != expectedSecret) {
            Log.w(TAG, "Connect secret mismatch from $clientPubkey")
            sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey,
                error = "Secret mismatch")
            return
        }

        val connections = SignstrPreferences.getConnections(context).toMutableList()
        val existing = connections.find { it.clientPubkeyHex == clientPubkey && it.identityId == identity.id }

        if (existing != null) {
            // Already connected - respond with ack
            sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey, result = secret.ifEmpty { "ack" })
            return
        }

        val relayUrls = relays.keys.toList()
        val connection = SignstrConnection(
            id = UUID.randomUUID().toString(),
            identityId = identity.id,
            clientPubkeyHex = clientPubkey,
            relays = relayUrls,
            secret = secret,
            createdAt = System.currentTimeMillis() / 1000
        )
        connections.add(connection)
        SignstrPreferences.saveConnections(context, connections)

        sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey, result = secret.ifEmpty { "ack" })
        onConnectionRequest(connection)
        Log.d(TAG, "New connection from $clientPubkey for identity ${identity.pubkeyHex}")
    }

    private fun handleGetPublicKey(
        requestId: String,
        clientPubkey: String,
        identity: SignstrIdentity,
        privkey: ByteArray
    ) {
        sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey, result = identity.pubkeyHex)
    }

    private fun handleSignEvent(
        requestId: String,
        params: List<String>,
        clientPubkey: String,
        identity: SignstrIdentity,
        privkey: ByteArray
    ) {
        try {
            val signedEvent = NostrEvent.signEvent(params[0], privkey, identity.pubkeyHex)
            sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey, result = signedEvent)
            Log.d(TAG, "Signed event for $clientPubkey")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing event: ${e.message}")
            sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey,
                error = "Signing failed: ${e.message}")
        }
    }

    fun approveRequest(request: NIP46Request) {
        val identity = SignstrPreferences.getIdentities(context)
            .find { it.id == request.identityId } ?: return
        val privkey = SignstrKeyStore.loadPrivateKey(context, identity.id) ?: return
        try {
            when (request.method) {
                "sign_event" -> handleSignEvent(
                    request.requestId, request.params, request.clientPubkey, identity, privkey)
                "nip44_encrypt" -> {
                    if (request.params.size >= 2) {
                        val thirdPartyPubkey = request.params[0]
                        val plaintext = request.params[1]
                        val convKey = NIP44.conversationKey(privkey, thirdPartyPubkey)
                        val encrypted = NIP44.encrypt(convKey, plaintext)
                        sendResponse(request.requestId, request.clientPubkey,
                            identity.pubkeyHex, privkey, result = encrypted)
                    }
                }
                "nip44_decrypt" -> {
                    if (request.params.size >= 2) {
                        val thirdPartyPubkey = request.params[0]
                        val ciphertext = request.params[1]
                        val convKey = NIP44.conversationKey(privkey, thirdPartyPubkey)
                        val decrypted = NIP44.decrypt(convKey, ciphertext)
                        sendResponse(request.requestId, request.clientPubkey,
                            identity.pubkeyHex, privkey, result = decrypted)
                    }
                }
            }
            logEvent(identity, request.connection, request.method, request.eventKind, true)
        } finally {
            privkey.fill(0)
        }
    }

    fun rejectRequest(request: NIP46Request) {
        val identity = SignstrPreferences.getIdentities(context)
            .find { it.id == request.identityId } ?: return
        val privkey = SignstrKeyStore.loadPrivateKey(context, identity.id) ?: return
        try {
            sendResponse(request.requestId, request.clientPubkey, identity.pubkeyHex, privkey,
                error = "User rejected")
            logEvent(identity, request.connection, request.method, request.eventKind, false)
        } finally {
            privkey.fill(0)
        }
    }

    private fun sendResponse(
        requestId: String,
        clientPubkey: String,
        signerPubkeyHex: String,
        signerPrivkey: ByteArray,
        result: String? = null,
        error: String? = null
    ) {
        val response = buildJsonObject {
            put("id", requestId)
            if (result != null) put("result", result)
            if (error != null) put("error", error)
        }.toString()

        val eventJson = NostrEvent.buildKind24133(
            content = response,
            senderPrivkey = signerPrivkey,
            senderPubkeyHex = signerPubkeyHex,
            recipientPubkeyHex = clientPubkey
        )

        relays.values.forEach { relay ->
            relay.publish(eventJson)
        }
    }

    private fun subscribeForIdentities(relayUrl: String) {
        val identities = SignstrPreferences.getIdentities(context)
        if (identities.isEmpty()) return

        val relay = relays[relayUrl] ?: return
        val since = (System.currentTimeMillis() / 1000) - 60

        for (identity in identities) {
            val subId = "nip46_${identity.id.take(8)}"
            val filter = buildJsonObject {
                put("#p", buildJsonArray { add(JsonPrimitive(identity.pubkeyHex)) })
                put("kinds", buildJsonArray { add(JsonPrimitive(24133)) })
                put("since", since)
            }
            relay.subscribe(subId, filter)
        }
    }

    fun generateBunkerUri(identity: SignstrIdentity): String {
        val secret = NIP44.bytesToHex(ByteArray(16).also { SecureRandom().nextBytes(it) })
        pendingSecrets[identity.pubkeyHex] = secret
        val relays = SignstrPreferences.getDefaultRelays(context)
        val relayParams = relays.joinToString("&") { "relay=$it" }
        return "bunker://${identity.pubkeyHex}?$relayParams&secret=$secret"
    }

    private fun findOrCreateConnection(clientPubkey: String, identity: SignstrIdentity): SignstrConnection {
        val connections = SignstrPreferences.getConnections(context)
        return connections.find { it.clientPubkeyHex == clientPubkey && it.identityId == identity.id }
            ?: SignstrConnection(
                id = UUID.randomUUID().toString(),
                identityId = identity.id,
                clientPubkeyHex = clientPubkey,
                relays = relays.keys.toList(),
                secret = "",
                createdAt = System.currentTimeMillis() / 1000
            )
    }

    private fun logEvent(
        identity: SignstrIdentity,
        connection: SignstrConnection,
        method: String,
        eventKind: Int?,
        approved: Boolean
    ) {
        val entry = EventLogEntry(
            id = UUID.randomUUID().toString(),
            identityId = identity.id,
            connectionId = connection.id,
            clientName = connection.clientName.ifEmpty { connection.clientPubkeyHex.take(12) + "..." },
            method = method,
            eventKind = eventKind,
            approved = approved,
            timestamp = System.currentTimeMillis()
        )
        val log = SignstrPreferences.getEventLog(context).toMutableList()
        log.add(entry)
        SignstrPreferences.saveEventLog(context, log)
        onEventLogged(entry)
    }

    fun addRelay(url: String) {
        if (relays.containsKey(url)) return
        val relay = NostrRelay(url, this)
        relays[url] = relay
        relay.connect()
    }
}
