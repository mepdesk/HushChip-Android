package uk.co.signstr.app.crypto

import kotlinx.serialization.json.*

object NostrEvent {

    fun computeEventId(
        pubkey: String,
        createdAt: Long,
        kind: Int,
        tags: JsonArray,
        content: String
    ): ByteArray {
        val serialized = buildJsonArray {
            add(JsonPrimitive(0))
            add(JsonPrimitive(pubkey))
            add(JsonPrimitive(createdAt))
            add(JsonPrimitive(kind))
            add(tags)
            add(JsonPrimitive(content))
        }
        val json = serialized.toString()
        return Secp256k1.sha256(json.toByteArray(Charsets.UTF_8))
    }

    fun signEvent(eventJson: String, privkey: ByteArray, pubkeyHex: String): String {
        val json = Json { ignoreUnknownKeys = true }
        val event = json.parseToJsonElement(eventJson).jsonObject

        val kind = event["kind"]?.jsonPrimitive?.int
            ?: throw IllegalArgumentException("Missing kind")
        val content = event["content"]?.jsonPrimitive?.content ?: ""
        val createdAt = event["created_at"]?.jsonPrimitive?.long
            ?: (System.currentTimeMillis() / 1000)
        val tags = event["tags"]?.jsonArray ?: buildJsonArray {}

        val eventId = computeEventId(pubkeyHex, createdAt, kind, tags, content)
        val eventIdHex = NIP44.bytesToHex(eventId)
        val sig = Secp256k1.schnorrSign(eventId, privkey)
        val sigHex = NIP44.bytesToHex(sig)

        val signedEvent = buildJsonObject {
            put("id", eventIdHex)
            put("pubkey", pubkeyHex)
            put("created_at", createdAt)
            put("kind", kind)
            put("tags", tags)
            put("content", content)
            put("sig", sigHex)
        }
        return signedEvent.toString()
    }

    fun buildKind24133(
        content: String,
        senderPrivkey: ByteArray,
        senderPubkeyHex: String,
        recipientPubkeyHex: String
    ): String {
        val conversationKey = NIP44.conversationKey(senderPrivkey, recipientPubkeyHex)
        val encryptedContent = NIP44.encrypt(conversationKey, content)
        val createdAt = System.currentTimeMillis() / 1000
        val tags = buildJsonArray {
            add(buildJsonArray {
                add(JsonPrimitive("p"))
                add(JsonPrimitive(recipientPubkeyHex))
            })
        }

        val eventId = computeEventId(senderPubkeyHex, createdAt, 24133, tags, encryptedContent)
        val sig = Secp256k1.schnorrSign(eventId, senderPrivkey)

        return buildJsonObject {
            put("id", NIP44.bytesToHex(eventId))
            put("pubkey", senderPubkeyHex)
            put("created_at", createdAt)
            put("kind", 24133)
            put("tags", tags)
            put("content", encryptedContent)
            put("sig", NIP44.bytesToHex(sig))
        }.toString()
    }
}
