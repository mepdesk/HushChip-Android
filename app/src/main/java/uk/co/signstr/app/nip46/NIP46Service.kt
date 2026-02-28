package uk.co.signstr.app.nip46

import android.content.Context
import kotlinx.serialization.json.*
import uk.co.signstr.app.crypto.*
import uk.co.signstr.app.data.*
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

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
    private val processedEventIds = ConcurrentHashMap<String, Long>()
    private val pendingSecrets = ConcurrentHashMap<String, String>()
    private val json = Json { ignoreUnknownKeys = true }

    // Request queue — prevents pendingRequest overwrite when multiple requests arrive
    private val requestQueue = ConcurrentLinkedQueue<NIP46Request>()
    @Volatile
    private var isProcessingRequest = false

    // Cleanup timer
    private var cleanupThread: Thread? = null
    @Volatile
    private var running = false

    companion object {
        const val FALLBACK_RELAY = "wss://relay.damus.io"
        val SAFE_KINDS = setOf(0, 3, 10000, 10001, 10002, 22242)
        private const val CLEANUP_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
        private const val EVENT_ID_TTL_MS = 5 * 60 * 1000L // 5 minutes
    }

    fun start() {
        val identities = SignstrPreferences.getIdentities(context)
        val connections = SignstrPreferences.getConnections(context)
        if (identities.isEmpty()) return

        running = true
        startCleanupTimer()

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
        running = false
        cleanupThread?.interrupt()
        cleanupThread = null
        relays.values.forEach { it.close() }
        relays.clear()
    }

    fun restart() {
        stop()
        start()
    }

    private fun startCleanupTimer() {
        cleanupThread = Thread {
            while (running) {
                try {
                    Thread.sleep(CLEANUP_INTERVAL_MS)
                    cleanupProcessedEventIds()
                } catch (_: InterruptedException) {
                    break
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun cleanupProcessedEventIds() {
        val cutoff = System.currentTimeMillis() - EVENT_ID_TTL_MS
        val iterator = processedEventIds.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value < cutoff) {
                iterator.remove()
            }
        }
    }

    override fun onConnected(relay: String) {
        subscribeForIdentities(relay)
    }

    override fun onDisconnected(relay: String) { }

    override fun onError(relay: String, error: String) { }

    override fun onOk(relay: String, eventId: String, accepted: Boolean, message: String) { }

    override fun onEvent(relay: String, subscriptionId: String, eventJson: JsonObject) {
        val eventId = eventJson["id"]?.jsonPrimitive?.content ?: return
        // Deduplicate with timestamp for cleanup
        if (processedEventIds.putIfAbsent(eventId, System.currentTimeMillis()) != null) return

        val kind = eventJson["kind"]?.jsonPrimitive?.int ?: return
        if (kind != 24133) return

        val senderPubkey = eventJson["pubkey"]?.jsonPrimitive?.content ?: return
        val content = eventJson["content"]?.jsonPrimitive?.content ?: return

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
                handleDecryptedMessage(decrypted, senderPubkey, identity, privkey)
            } catch (_: Exception) {
                // Decryption failed — skip silently
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
        val params = msg["params"]?.jsonArray?.map { element ->
            if (element is JsonPrimitive) element.content
            else element.toString()
        } ?: emptyList()

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

                // Check auto-approve: safe kind OR per-app timed trust
                val safeKindApprove = identity.autoApproveEnabled &&
                    eventKind != null && eventKind in identity.safeKinds
                val policyApprove = ApprovalPolicyStore.shouldAutoApprove(context, clientPubkey)

                if (safeKindApprove || policyApprove) {
                    handleSignEvent(requestId, params, clientPubkey, identity, privkey)
                    logEvent(identity, connection, method, eventKind, approved = true,
                        autoApproved = true, safeKindAutoApproved = safeKindApprove)
                } else {
                    enqueueRequest(request)
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
                enqueueRequest(request)
            }
            else -> {
                sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey,
                    error = "Unsupported method: $method")
            }
        }
    }

    // --- Request Queue ---

    private fun enqueueRequest(request: NIP46Request) {
        requestQueue.add(request)
        processNextInQueue()
    }

    @Synchronized
    private fun processNextInQueue() {
        if (isProcessingRequest) return
        val next = requestQueue.poll() ?: return
        isProcessingRequest = true
        onSigningRequest(next)
    }

    fun onRequestProcessed() {
        isProcessingRequest = false
        processNextInQueue()
    }

    // --- End Request Queue ---

    private fun handleConnect(
        requestId: String,
        params: List<String>,
        clientPubkey: String,
        identity: SignstrIdentity,
        privkey: ByteArray
    ) {
        val secret = if (params.size > 1) params[1] else ""

        val expectedSecret = pendingSecrets[identity.pubkeyHex]
        if (expectedSecret != null && secret.isNotEmpty() && secret != expectedSecret) {
            sendResponse(requestId, clientPubkey, identity.pubkeyHex, privkey,
                error = "Secret mismatch")
            return
        }

        val connections = SignstrPreferences.getConnections(context).toMutableList()
        val existing = connections.find { it.clientPubkeyHex == clientPubkey && it.identityId == identity.id }

        if (existing != null) {
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
        } catch (e: Exception) {
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
                "sign_event" -> {
                    handleSignEvent(request.requestId, request.params, request.clientPubkey, identity, privkey)
                    ApprovalPolicyStore.recordFirstApproval(context, request.clientPubkey)
                }
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
                "nip04_encrypt" -> {
                    if (request.params.size >= 2) {
                        val thirdPartyPubkey = request.params[0]
                        val plaintext = request.params[1]
                        val encrypted = NIP04.encrypt(privkey, thirdPartyPubkey, plaintext)
                        sendResponse(request.requestId, request.clientPubkey,
                            identity.pubkeyHex, privkey, result = encrypted)
                    }
                }
                "nip04_decrypt" -> {
                    if (request.params.size >= 2) {
                        val thirdPartyPubkey = request.params[0]
                        val ciphertext = request.params[1]
                        val decrypted = NIP04.decrypt(privkey, thirdPartyPubkey, ciphertext)
                        sendResponse(request.requestId, request.clientPubkey,
                            identity.pubkeyHex, privkey, result = decrypted)
                    }
                }
            }
            logEvent(identity, request.connection, request.method, request.eventKind, approved = true)
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
            logEvent(identity, request.connection, request.method, request.eventKind, approved = false)
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
        val relayList = SignstrPreferences.getDefaultRelays(context)
        val relayParams = relayList.joinToString("&") { "relay=$it" }
        return "bunker://${identity.pubkeyHex}?$relayParams&secret=$secret"
    }

    fun addConnection(
        clientPubkey: String,
        relayUrls: List<String>,
        secret: String,
        appName: String,
        identity: SignstrIdentity
    ) {
        val privkey = SignstrKeyStore.loadPrivateKey(context, identity.id) ?: return
        try {
            // Add relay connections
            for (url in relayUrls) {
                if (!relays.containsKey(url)) {
                    val relay = NostrRelay(url, this)
                    relays[url] = relay
                    relay.connect()
                }
            }

            val connections = SignstrPreferences.getConnections(context).toMutableList()
            val existing = connections.find { it.clientPubkeyHex == clientPubkey && it.identityId == identity.id }
            if (existing != null) return

            val connection = SignstrConnection(
                id = UUID.randomUUID().toString(),
                identityId = identity.id,
                clientPubkeyHex = clientPubkey,
                clientName = appName,
                relays = relayUrls,
                secret = secret,
                createdAt = System.currentTimeMillis() / 1000
            )
            connections.add(connection)
            SignstrPreferences.saveConnections(context, connections)

            // Send connect response immediately (client-initiated flow)
            val responseId = UUID.randomUUID().toString()
            sendResponse(responseId, clientPubkey, identity.pubkeyHex, privkey,
                result = secret.ifEmpty { "ack" })

            onConnectionRequest(connection)
        } finally {
            privkey.fill(0)
        }
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
        approved: Boolean,
        autoApproved: Boolean = false,
        safeKindAutoApproved: Boolean = false
    ) {
        val entry = EventLogEntry(
            id = UUID.randomUUID().toString(),
            identityId = identity.id,
            connectionId = connection.id,
            clientName = connection.clientName.ifEmpty { connection.clientPubkeyHex.take(12) + "..." },
            method = method,
            eventKind = eventKind,
            approved = approved,
            autoApproved = autoApproved,
            safeKindAutoApproved = safeKindAutoApproved,
            timestamp = System.currentTimeMillis()
        )
        val log = SignstrPreferences.getEventLog(context).toMutableList()
        log.add(0, entry)
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
