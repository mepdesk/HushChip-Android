# Signstr — NIP-46 Remote Signing Reference

**Date:** 27 February 2026  
**Purpose:** Definitive reference for how NIP-46 works in Signstr. Captures every troubleshoot, protocol decision, and implementation detail discovered during development. Future sessions should read this FIRST before touching any NIP-46 code.

---

## HOW IT WORKS (HIGH LEVEL)

Signstr is a NIP-46 remote signer (also called a "bunker"). It holds Nostr private keys and signs events on behalf of connected client apps (Primal, Coracle, noStrudel, Amethyst, etc.) over Nostr relays.

```
Client App (Primal)                    Signstr (Signer)
    |                                       |
    |  1. User scans bunker:// QR           |
    |  2. Client sends connect request  --> |
    |      (kind 24133, NIP-44 encrypted)   |
    |                                       |  3. Signstr validates secret
    |  <-- 4. Connect response (NIP-44)     |
    |      (kind 24133, result: secret)     |
    |                                       |
    |  5. Client sends sign_event       --> |
    |      (kind 24133, NIP-44 encrypted)   |
    |                                       |  6. User approves on phone
    |  <-- 7. Signed event returned         |
    |      (kind 24133, NIP-44 encrypted)   |
```

All communication happens via kind 24133 Nostr events sent through shared relays. The signer and client never communicate directly.

---

## THE CONNECTION FLOW

### bunker:// URI Format

```
bunker://<signer-pubkey>?relay=wss://relay1&relay=wss://relay2&secret=<random-secret>
```

Signstr generates this URI and displays it as a QR code. The client scans it and:

1. Creates a throwaway keypair for the session
2. Subscribes to kind 24133 events on the specified relays, filtered to its own pubkey
3. Sends a `connect` request to the signer's pubkey containing the secret
4. Waits for the signer to respond with the secret echoed back

### Client-Initiated Flow (Current Implementation)

When the client scans the QR and sends a connect request, Signstr:

1. Receives the kind 24133 event on subscribed relays
2. Decrypts the content with NIP-44 using the conversation key derived from (signer privkey, client pubkey)
3. Validates the secret matches
4. Sends back a connect response with `result: <secret>`
5. Marks the session as active

**IMPORTANT:** Signstr sends the connect response IMMEDIATELY upon adding the connection, BEFORE receiving any client message. This is because in client-initiated flow, the client is already waiting. Signstr pre-emptively sends the response to all specified relays plus the fallback relay.

---

## ENCRYPTION: NIP-44 (CRITICAL)

### The Bug That Blocked Everything

For days, no client could connect. The root cause was two bugs in NIP-44 key derivation:

1. **`conversationKey()`** used `HKDF.deriveKey` (extract+expand) instead of extract-only. The spec requires `HKDF-extract`: just `HMAC-SHA256(key="nip44-v2", data=ecdh_shared_x)`.

2. **`deriveMessageKeys()`** had parameters swapped (nonce as IKM, conversation_key as salt) AND used extract+expand with `info="nip44-v2"`. The spec requires `HKDF-expand` with `PRK=conversation_key`, `info=nonce`, `L=76`.

These produced completely different keys than nostr-tools (used by Coracle, noStrudel, Primal), so cross-implementation decryption always failed silently.

### NIP-44 Conversation Key Derivation (Correct)

```
1. ECDH shared secret:
   shared_x = secp256k1_ecdh(signer_privkey, '02' + client_pubkey)
   Take only the 32-byte x-coordinate (drop the prefix byte)

2. HKDF-extract ONLY (NOT extract+expand):
   conversation_key = HMAC-SHA256(key="nip44-v2" as UTF-8 bytes, data=shared_x)
   Result: 32 bytes
```

### NIP-44 Message Key Derivation (Correct)

```
Given: conversation_key (32 bytes), nonce (32 bytes, random per message)

HKDF-expand (RFC 5869 Section 2.3):
  PRK = conversation_key
  info = nonce
  L = 76 bytes

Split the 76 bytes:
  chacha_key = bytes[0..31]    (32 bytes)
  chacha_nonce = bytes[32..43] (12 bytes)
  hmac_key = bytes[44..75]     (32 bytes)
```

### NIP-44 Message Format

```
Version byte: 0x02
Nonce: 32 bytes (random)
Ciphertext: variable (ChaCha20-Poly1305 encrypted padded plaintext)
HMAC: 32 bytes (HMAC-SHA256 over nonce + ciphertext, keyed with hmac_key)

Wire format: base64(version || nonce || ciphertext || hmac)
```

### NIP-44 Padding

Plaintext is padded before encryption:
- 2-byte big-endian length prefix
- Padded to next power of 2 (minimum 32 bytes total after prefix)

### Verified Test Vectors

The implementation passes all official NIP-44 spec test vectors:
- 3 conversation key derivation vectors
- 2 message key derivation vectors  
- 2 full encrypt/decrypt vectors including emoji content (pizza, pregnant man)

**Commit:** `5bfbd35` (NIP-44 fix), verified against https://github.com/nostr-protocol/nips/blob/master/44.md

### NIP-04 Detection (Incoming Messages)

For incoming messages, Signstr auto-detects the encryption format:
- If content contains `?iv=` separator → NIP-04
- If content base64-decodes and first byte is `0x02` → NIP-44
- Try NIP-44 first, fall back to NIP-04

### Outgoing Encryption

Currently: **NIP-44 only** for all outgoing messages. This matches what modern clients expect (nostr-tools imports NIP-44 exclusively for NIP-46 decryption).

---

## RELAY COMPATIBILITY

### Working Relays

| Relay | Status | Notes |
|-------|--------|-------|
| `wss://bucket.coracle.social/` | OK | Accepts events, returns OK |
| `wss://relay.damus.io` | OK | Accepts events, returns OK. Used as fallback. |
| `wss://ephemeral.snowflare.cc/` | OK | Accepts events, returns OK |
| `wss://relay.primal.net` | OK | Primal's relay, works well |

### Problematic Relays

| Relay | Issue |
|-------|-------|
| `wss://relay.nsec.app/` | Silently drops all events. No OK, no AUTH, no error. Community reports: rate limits, requires PoW for kind 24133, problematic in Europe. |

### Fallback Relay Strategy

Signstr adds `wss://relay.damus.io` as a fallback relay to every connection, in addition to the client's specified relays. This ensures at least one reliable relay path exists.

### Subscription Filter

```json
{
  "#p": ["<signer_pubkey>"],
  "since": <now - 60 seconds>,
  "kinds": [24133]
}
```

- `since` is set to now minus 60 seconds (was 10s initially, widened for clock skew)
- No `limit` field (ephemeral.snowflare.cc rejected `limit:0`)

---

## NIP-46 METHODS SUPPORTED

| Method | Auth Required | Notes |
|--------|--------------|-------|
| `connect` | No | Validates secret, returns secret |
| `get_public_key` | No | Returns signer pubkey hex directly, no biometrics |
| `sign_event` | Yes (approval) | Computes event ID, signs with Schnorr |
| `nip04_encrypt` | Yes | NIP-04 encryption |
| `nip04_decrypt` | Yes | NIP-04 decryption |
| `nip44_encrypt` | Yes | NIP-44 encryption |
| `nip44_decrypt` | Yes | NIP-44 decryption |

### sign_event Implementation (Critical)

Clients send unsigned events WITHOUT an `id` or `sig` field. The signer must:

1. Parse the event JSON using `JSONSerialization` (not `JSONDecoder` with strict Codable, which requires all fields)
2. Extract `kind`, `content`, `tags`, `created_at` from the dictionary
3. Set `pubkey` to the signer's public key
4. Compute the event ID: `SHA256(JSON_serialize([0, pubkey, created_at, kind, tags, content]))`
5. Sign the event ID hash with Schnorr
6. Return the complete signed event as JSON string in the result field

**Bug fixed in commit `081bfff`:** Originally used `JSONDecoder` which crashed with `keyNotFound: id`.

### get_public_key Implementation

Returns the signer's hex public key directly. Does NOT require biometric authentication — it's public information.

**Bug fixed in commit `081bfff`:** Originally routed through `NostrSigner` which triggered Face ID.

---

## REQUEST QUEUING

Multiple `sign_event` requests can arrive simultaneously (e.g., Primal sends relay lists, profile metadata, contact lists in rapid succession). Only one approval prompt can be shown at a time.

Implementation (commit `081bfff`):
- `requestQueue`: array of pending `(event, session)` tuples
- `isProcessingRequest`: flag preventing concurrent processing
- `enqueueRequest()`: adds to queue
- `processNextInQueue()`: pops next item after current completes

**Known issue (to fix):** Even with queuing, every `sign_event` triggers a user approval prompt. Primal sends 5-8 sign requests on connection, causing approval spam. Needs auto-approve policies for non-destructive event kinds.

---

## KNOWN ISSUES AND FIXES NEEDED

### 1. Approval Spam (Face ID / Sign Request Flooding)

**Symptom:** Connecting Primal triggers 5-8 rapid `sign_event` requests for metadata events (kind 0, 3, 10002). Each one shows an approval prompt with Face ID.

**Fix needed:** Auto-approve policies per connected app. Options:
- Auto-approve read-only event kinds after first manual approval
- Per-app trust level with time-based expiry
- Batch multiple pending requests into one approval screen

### 2. `switch_relays` Method Not Supported

Coracle sends `switch_relays` which Signstr returns as unsupported. This is a non-standard method and can be safely ignored — Coracle continues working without it.

### 3. relay.nsec.app Drops Events

Events sent to `wss://relay.nsec.app/` are silently dropped. The fallback relay strategy mitigates this, but noStrudel users who only specify relay.nsec.app may have issues.

### 4. Duplicate Event Processing

The same event can arrive on multiple relays (e.g., event f03bff3d arrived on both ephemeral.snowflare.cc and bucket.coracle.social). Signstr processes both, sending duplicate responses. Should deduplicate by event ID.

---

## CLIENT COMPATIBILITY MATRIX

| Client | Encryption | Relay | Connect | get_public_key | sign_event |
|--------|-----------|-------|---------|----------------|------------|
| Coracle | NIP-44 | bucket.coracle.social, ephemeral.snowflare.cc | YES | YES | Not tested yet |
| Primal Web | NIP-44 | relay.primal.net | YES | YES | Received but errored (now fixed) |
| noStrudel | NIP-44 | relay.nsec.app (problematic) | Untested (relay issue) | — | — |
| Amethyst | Unknown | Unknown | Untested | — | — |

---

## KEY FILES

| File | Purpose |
|------|---------|
| `NIP46/NIP44.swift` | NIP-44 encryption/decryption, conversation key, message keys |
| `NIP46/NIP04.swift` | NIP-04 legacy encryption |
| `NIP46/NIP46Service.swift` | Core service: relay connections, event routing, connect flow |
| `NIP46/NIP46Session.swift` | Per-client session state, encryption preference |
| `NIP46/NIP46RequestQueue.swift` | Sequential request processing |
| `Views/NIP46TestClientView.swift` | Debug tool with self-test, throwaway keypair |
| `Crypto/NostrEvent.swift` | Event construction, ID computation, signing |
| `Crypto/SoftwareSigner.swift` | In-memory Schnorr signing (no card needed) |

---

## DEBUGGING CHECKLIST

If NIP-46 stops working, check in this order:

1. **Are relays connecting?** Look for `Opening WebSocket to...` and `REQ sent to...` logs
2. **Are events being accepted?** Look for `OK from <relay>: accepted=true`
3. **Is relay.nsec.app involved?** It drops events silently. Add alternative relays.
4. **Can we decrypt incoming messages?** Look for `Decrypted with NIP-44` or decryption errors
5. **Is the conversation key correct?** Compare `Conversation key (first 8 bytes)` between connection setup and incoming message processing — they must match
6. **Is the event ID computation correct?** The serialized array must be `[0, pubkey, created_at, kind, tags, content]` with no extra fields
7. **Is the signature valid?** Look for `Signature self-verify: VALID`
8. **Are we sending to the right pubkey?** The p-tag must be the CLIENT's pubkey, not our own

### Quick Self-Test

Use NIP46TestClientView to generate a throwaway keypair, create a bunker:// URI, and test the full connect flow without needing an external client.

---

## COMMIT HISTORY

| Commit | What |
|--------|------|
| `5bfbd35` | Fixed NIP-44 key derivation (HKDF-extract vs extract+expand, message key parameter order) |
| `081bfff` | Fixed sign_event missing ID field, removed biometrics from get_public_key, added request queue |
| Earlier commits | NIP-04/NIP-44 dual support, relay fallback, OK timeout warnings, NIP-42 AUTH handling |

---

## PROTOCOL REFERENCES

- NIP-46 spec: https://github.com/nostr-protocol/nips/blob/master/46.md
- NIP-44 spec: https://github.com/nostr-protocol/nips/blob/master/44.md
- NIP-01 (event format): https://github.com/nostr-protocol/nips/blob/master/01.md
- nostr-tools NIP-46 implementation: https://github.com/nbd-wtf/nostr-tools/blob/master/nip46.ts
- nostr-tools NIP-44 implementation: https://github.com/nbd-wtf/nostr-tools/blob/master/nip44.ts
