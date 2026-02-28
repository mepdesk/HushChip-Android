# Signstr Android — Implementation Handover Document

**Date:** 28 February 2026
**Source:** Signstr iOS codebase (feature-complete v1.0)
**Purpose:** Enable an AI or developer to build a feature-identical Android version of Signstr from this document alone. Every screen, protocol detail, algorithm, colour, and edge case is documented below.

---

## TABLE OF CONTENTS

1. [What Signstr Is](#1-what-signstr-is)
2. [Complete User Flow](#2-complete-user-flow)
3. [Every Screen in the App](#3-every-screen-in-the-app)
4. [NIP-46 Protocol Implementation](#4-nip-46-protocol-implementation)
5. [NIP-44 Encryption Implementation](#5-nip-44-encryption-implementation)
6. [Multi-Identity Architecture](#6-multi-identity-architecture)
7. [Per-Identity Approval Policies](#7-per-identity-approval-policies)
8. [Per-App Timed Trust Policies](#8-per-app-timed-trust-policies)
9. [Connection Persistence and Relay Reconnection](#9-connection-persistence-and-relay-reconnection)
10. [Event Log Data Model and Storage](#10-event-log-data-model-and-storage)
11. [Bunker URI Format and QR Code Generation](#11-bunker-uri-format-and-qr-code-generation)
12. [Notification Logic](#12-notification-logic)
13. [Every Setting and What It Controls](#13-every-setting-and-what-it-controls)
14. [The Ghost Design System](#14-the-ghost-design-system)
15. [The Splash Screen](#15-the-splash-screen)
16. [Android-Specific Implementation Notes](#16-android-specific-implementation-notes)

---

## 1. WHAT SIGNSTR IS

Signstr is a **Nostr identity vault and remote signer**. It is NOT a Nostr client. Users do not compose posts, browse feeds, or read messages in Signstr.

Signstr does two things:

1. **Holds the nsec** (Nostr private key) — encrypted on device
2. **Signs events when other apps ask** — via the NIP-46 (Nostr Connect) remote signing protocol

The user's Nostr client (Damus, Primal, Amethyst, noStrudel, Coracle) sends signing requests to Signstr over Nostr relays. Signstr shows the user what is being signed, the user approves (biometrics), Signstr signs and sends the signature back. The nsec NEVER touches the client app.

**Android equivalent:** Amber is the existing Android Nostr signer. Signstr differentiates with multi-identity support, per-identity safe-kind policies, the Ghost dark UI, and future NostrKey NFC card support.

**Product identity:**
- App name: Signstr
- Company: Gridmark Technologies Ltd
- Website: signstr.com
- Licence: GPL-3.0 (source must be public)
- Bundle/package: uk.co.hushchip.signstr

---

## 2. COMPLETE USER FLOW

### First Launch (New User)

```
1. App opens → Splash screen (3-second animated signature)
2. Splash dismisses → Onboarding carousel (3 pages, swipeable)
   Page 1: "Your Nostr identity. Secured."
   Page 2: "Connect your favourite clients."
   Page 3: "Face ID approves every signature." → GET STARTED button
3. Onboarding completes → Key Setup screen
   Option A: CREATE NEW IDENTITY → Generate keypair → Show npub → Back Up Key (required)
   Option B: IMPORT EXISTING NSEC → Paste or scan nsec1... → Validate → Back Up Key (required)
4. Backup acknowledged → Main app (Connections tab active, empty state)
```

### Returning User (Has Identity, Has Connections)

```
1. App opens → Splash (3 seconds)
2. Splash dismisses → Main app (Connections tab)
3. All saved connections restored from storage
4. Relay WebSocket subscriptions re-established
5. Ready to receive signing requests
```

### Adding a Connection

```
1. User taps ADD CONNECTION on Connections tab
2. Camera opens for QR scanning (or paste URI toggle)
3. User scans nostrconnect:// QR from their Nostr client
4. Review screen shows: app name, relay, permissions
5. User taps APPROVE
6. Signstr derives conversation key, subscribes to relays
7. Signstr sends connect response immediately (with echoed secret)
8. Connection appears in list with green status
```

### Receiving a Signing Request

```
1. Client sends sign_event via kind 24133 on relay
2. Signstr receives, decrypts with NIP-44
3. Checks approval policy:
   a. Safe kind + auto-approve enabled → Sign immediately (no prompt, no biometrics)
   b. Per-app trust policy active → Sign immediately
   c. Otherwise → Show approval UI
4. If approval UI shown:
   - Notification fires if app is backgrounded
   - User sees: app name, event kind, content preview
   - User taps APPROVE → Biometric auth → Sign → Send response
   - User taps REJECT → Send rejection response
5. Event logged to signing history
```

### Managing Identities

```
1. User taps Identity tab
2. Sees active identity: avatar, display name, npub QR, copy npub
3. Can: RENAME, COPY NPUB, BACK UP NSEC (biometrics), DELETE
4. Can configure signing policy: safe kinds list, require-all toggle
5. Can add new identity via + button on identity picker
6. Identity picker appears as horizontal chip row at top of Connections and Events tabs
```

---

## 3. EVERY SCREEN IN THE APP

### 3.1 Splash Screen

See [Section 15](#15-the-splash-screen) for full design specification.

### 3.2 Onboarding (3 Pages)

A swipeable carousel with custom dot indicators. Pages are NOT skippable — user must reach the final page.

**Page 1 — OnboardingWelcomeView**
- Icon: Shield with key illustration
- Title: "Your Nostr identity. Secured."
- Body: "Signstr keeps your nsec encrypted in one place. No more pasting it into every app."
- Button: NEXT

**Page 2 — OnboardingInfoView**
- Icon: Document with checkmark and pen illustration
- Title: "Connect your favourite clients."
- Body: "Damus, Primal, and other Nostr clients can request signatures without ever seeing your nsec."
- Button: NEXT

**Page 3 — OnboardingNFCView**
- Icon: Face ID brackets with face illustration
- Title: "Face ID approves every signature."
- Body: "Set approval policies per app. Always ask, trust for a session, or auto-approve by event kind."
- Button: GET STARTED (accent style)

**Dot indicators:** Active dot is a wider capsule shape. Inactive dots are circles. All sgTextMuted colour. The native page indicator is hidden.

**On completion:** Sets `onboardingComplete = true` in persistent storage.

### 3.3 Key Setup

Two large tappable option cards on a single screen:

1. **CREATE NEW IDENTITY** — generates fresh secp256k1 keypair
2. **IMPORT EXISTING NSEC** — paste or scan an nsec1... string

### 3.4 Generate Key

**States:**
1. **Ready** — "GENERATE KEYPAIR" button centered
2. **Generating** — Progress spinner, "Generating keypair..."
3. **Success** — Shows generated npub (bech32, truncated), "BACK UP YOUR KEY" button
4. **Error** — Error message, retry button

**On success:**
- Generates 32 random bytes (cryptographically secure)
- Derives x-only public key (secp256k1)
- Stores private key encrypted in secure storage (Android Keystore / EncryptedSharedPreferences)
- Creates first NostrIdentity with default name "Main"

### 3.5 Import Nsec

**UI elements:**
- Text field with placeholder "nsec1..."
- CLEAR button (X icon, appears when text entered)
- PASTE button — loads from clipboard
- SCAN QR button — opens camera scanner for nsec QR codes
- IMPORT KEY button (disabled until valid nsec entered)

**Validation:**
- Must start with "nsec1"
- Must be valid bech32 encoding
- Must decode to exactly 32 bytes
- Must not already exist as an identity

**On success:**
- Stores key in secure storage
- Clears clipboard for security
- Creates NostrIdentity
- Navigates to backup flow

### 3.6 Back Up Key

Shown after generate OR import. Backup acknowledgement is REQUIRED before proceeding to the main app.

**Content:**
- Warning box (danger colours): "Your Nostr identity depends on this key. If you lose this device and have no backup, your identity is gone forever. There is no recovery. No password reset. No support ticket."
- nsec field: masked by default, tap to reveal, COPY button
- Clipboard auto-clears after 30 seconds
- "I've saved it somewhere safe" confirmation button → proceeds to main app
- "I'll do this later" option → warning about risk → "I understand the risk" → proceeds

**On completion:** Sets `keySetupComplete = true`.

### 3.7 Home / Main App (4 Tabs)

Root navigation is a bottom tab bar with 4 tabs:

| Tab | Label | Icon | View |
|-----|-------|------|------|
| 1 | Connections | Link icon | ConnectionsTabView |
| 2 | Events | Clipboard icon | EventsTabView |
| 3 | Identity | Key icon | IdentityTabView |
| 4 | Settings | Gear icon | SettingsTabView |

### 3.8 Connections Tab

**Header:** Identity picker (horizontal scrolling chip row showing all identities)

**Empty state (no connections):**
- Title: "NO CONNECTIONS"
- Body: "Scan a Nostr Connect QR from your favourite client to get started."

**Connected apps list (when connections exist):**
- Per row:
  - App icon placeholder (circle)
  - App display name (e.g. "Primal", "Coracle")
  - Truncated client pubkey
  - Green connected status indicator
- Tap row → Opens App Settings sheet

**Bottom:** ADD CONNECTION button (with QR/plus icon)

**Pending signing request:** When `pendingRequest` is non-nil, a full-screen signing approval overlay appears.

### 3.9 Add Connection

**States:**
1. **Scan** — Camera preview with overlay frame guide
   - Title: "ADD CONNECTION"
   - Close button (top-left)
   - "Scan a Nostr Connect QR code" instruction text
   - Toggle: "PASTE URI" — switches to text input mode
2. **Paste** — Text input for `nostrconnect://` or `bunker://` URI
   - Paste button to load clipboard
   - CONNECT button
3. **Review** — Shows connection details for approval
   - App icon placeholder
   - "CONNECTION REQUEST" label
   - "[App Name] wants to connect to your signer"
   - Details card: APP, RELAY (domain only, wss:// stripped), PERMISSIONS, FLOW
   - APPROVE button (confirm style)
   - REJECT button (danger style)
4. **Connecting** — Progress spinner
5. **Error** — Error message with retry/cancel

**On approve:**
- Loads active identity's private key from secure storage
- Calls NIP46Service.addConnection()
- Zeros key from memory after use
- Haptic feedback on success
- Dismisses and returns to Connections tab

### 3.10 Signing Request (Full-Screen Overlay)

**Layout:**
- Header: "SIGNING REQUEST" label with signature icon in circle
- App name: "[App Name] wants to sign an event"
- Event details card:
  - APP: client display name
  - EVENT KIND: human-readable label (see kind map below)
  - CONTENT: preview, truncated to 280 characters + "..."
  - CLIENT PUBKEY: truncated
- APPROVE & SIGN button (confirm style)
  - On tap: sets isSigning = true, shows "Signing with biometrics..."
  - Triggers biometric authentication
  - On success: signs and sends response
- REJECT button (danger style)
  - Sends rejection response to client

**Event kind → human-readable label map:**

| Kind | Label |
|------|-------|
| 0 | Profile metadata |
| 1 | Short note |
| 2 | Relay list |
| 3 | Contact list |
| 4 | Encrypted DM |
| 7 | Reaction |
| 9735 | Zap receipt |
| 22242 | Auth challenge |
| 24133 | NIP-46 request |
| 30023 | Long-form article |
| Other | "Event kind N" |

### 3.11 Events Tab

**Header:** Identity picker (filters events by selected identity)

**Empty state:** "NO EVENTS"

**Event list:**
- Count badge: "N EVENT(S)"
- Chronologically ordered (newest first)
- Per entry:
  - Status badge icon with colour:
    - **X** (sgDanger muted) = Rejected
    - **A** (blue muted) = Auto-approved by policy
    - **✓** (green muted) = Manually approved
  - Event kind (human-readable label)
  - Relative timestamp: "now", "Ns ago", "Nm ago", "Nh ago", "Nd ago", "dd MMM"
  - App name (small text)
- Tap entry → Opens Event Detail sheet

### 3.12 Event Detail (Sheet)

- APP: client display name
- TIMESTAMP: full date/time format
- EVENT KIND: kind number + human-readable name
- STATUS: "Approved" / "Auto-approved" / "Rejected"
- CONTENT: full content (if present)
- CLIENT PUBKEY: full hex
- RAW EVENT JSON: formatted pretty-print of the unsigned event
- COPY JSON button

### 3.13 Identity Tab

**Header section:**
- Large profile avatar (80dp, circular, shows initials if no picture, fetched profile picture if available)
- Display name (editable)
- Connection count badge

**QR Code section:**
- QR code of the npub (180×180, white background on dark card)

**NPUB card:**
- Label: "PUBLIC KEY"
- Truncated npub (first 12 + "..." + last 8 characters)
- Full npub available for copy

**Actions (styled list rows):**
- RENAME — inline dialog to change display name
- COPY NPUB — copies to clipboard, shows toast confirmation
- BACK UP NSEC — requires biometrics, opens BackupNsecSheet
- DELETE IDENTITY (only visible if multiple identities exist) — confirmation dialog

**Signing Policy section:**
- REQUIRE APPROVAL FOR ALL — toggle
  - When on: prompts for every event regardless of safe kinds
  - When off: uses safe kinds list for auto-approval
- SAFE EVENT TYPES — shows count, tappable
  - Opens SafeKindsEditor sheet

**Air-Gapped upsell card:**
- "Go Air-Gapped" card promoting NostrKey physical card
- Links to https://signstr.com/card

### 3.14 Backup Nsec Sheet

- WARNING box (danger colours): "Anyone who sees your nsec controls your Nostr identity."
- Nsec field: masked by default (bullet characters), tap eye icon to reveal
- COPY button — copies nsec, auto-clears clipboard after 30 seconds
- DONE button — dismisses sheet

**Prerequisite:** Biometric authentication must succeed before this sheet opens.

### 3.15 Safe Kinds Editor (Sheet)

- Title: "SAFE EVENT TYPES"
- Description: "Events with these kinds are signed automatically without prompting."
- Sorted list of numeric kinds with human-readable labels
  - e.g. "0 — Profile metadata", "3 — Contact list"
  - Minus button per row to remove
- ADD KIND button — dialog to enter numeric kind value
- RESET TO DEFAULTS button — restores default safe kinds set
- Empty state if all kinds removed

### 3.16 App Settings (Per-Connection Sheet)

**Header:** Session display name

**Connection info card:**
- APP: display name
- CONNECTED SINCE: formatted date/time
- RELAY: domain only (wss:// stripped)
- CLIENT PUBKEY: truncated

**Approval policy section:**
- Radio buttons / selector for ApprovalPolicy:
  - Always Ask
  - Trust for Session
  - Trust for 15 min
  - Trust for 1 hour
  - Trust for 4 hours
  - Trust for 24 hours
  - Trust for 7 days
  - Always Trust
- Each has a description shown below when selected
- Clears first-approval timestamp when changed

**Danger zone:**
- "Disconnect [App Name]" button (danger style)
- Confirmation dialog before disconnecting
- Removes stored policy and connection data

### 3.17 Settings Tab

**SIGNING section:**
- Default Approval Policy — picker with all ApprovalPolicy options
  - Applied to new connections that have no per-app policy set
  - Default: Always Ask
- Biometrics toggle — "Require biometrics for signing approval"
  - Detects biometric type: fingerprint, face, iris
  - Default: on (true)
  - Stored as `biometricsEnabled` in preferences
- NIP-46 Relays — shows count of configured relays
  - Tap → opens Relay Config screen

**NOTIFICATIONS section:**
- Notifications toggle — "Alert when signing approval is needed"
  - Default: on (true)
  - Stored as `notificationsEnabled` in preferences

**GENERAL section:**
- About — version, licence, credits, links
- NIP-46 Test Client (debug builds only) — test connect flow without external client
- Developer Options (debug builds only) — view application logs
- Reset Onboarding (debug builds only) — re-show onboarding on next launch

**DANGER ZONE section:**
- Export All Keys — requires biometrics
  - Full-screen sheet listing all identities with their nsecs
  - Per identity: name, COPY button, full nsec (masked, tap to reveal)
  - 30-second clipboard auto-clear
- Delete All Data — 2-step confirmation dialog
  - Disconnects all sessions
  - Deletes all identities and keys
  - Clears approval policies and signing log
  - Returns to Key Setup screen
- Reset App — 2-step confirmation dialog
  - Same as Delete All Data + clears onboarding flag
  - Returns to onboarding screen

### 3.18 About Screen

- Version number (from build config)
- Copyright: "Signstr is a product of Gridmark Technologies Ltd"
- Links:
  - How to Use → https://signstr.com
  - Terms → https://signstr.com/terms
  - Privacy → https://signstr.com/privacy
  - Source Code → https://github.com/signstr/Signstr-iOS
  - Licence → https://github.com/signstr/Signstr-iOS/blob/main/LICENCE

### 3.19 Relay Config

- List of NIP-46 relay URLs
- Add relay (text field for wss:// URL)
- Remove relay (swipe to delete or minus button)
- Default relays (restored on reset):
  - `wss://relay.nsec.app`
  - `wss://relay.damus.io`
  - `wss://nos.lol`

### 3.20 Add Identity (Multi-Identity)

**States:**
1. **Choose** — Generate or Import buttons
2. **ImportNsec** — Text input for nsec
3. **NameIdentity** — Text field for display name
4. **Success** — Avatar with initials + identity name
5. **Error** — Error message with retry

**Flow:**
- Generate: creates random 32 bytes → prompts for display name
- Import: prompts for nsec → validates → prompts for display name
- Both: derive npub, create NostrIdentity, set as active identity

---

## 4. NIP-46 PROTOCOL IMPLEMENTATION

### 4.1 Overview

Signstr is a NIP-46 remote signer ("bunker"). All communication occurs via kind 24133 Nostr events exchanged over shared WebSocket relays. The signer and client never communicate directly.

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

### 4.2 Kind 24133 Event Structure

**Request (client → signer):**
```json
{
  "kind": 24133,
  "pubkey": "<client-pubkey-hex>",
  "created_at": 1234567890,
  "content": "<NIP-44 encrypted JSON-RPC>",
  "tags": [["p", "<signer-pubkey-hex>"]],
  "id": "<sha256-event-id>",
  "sig": "<schnorr-signature>"
}
```

**Decrypted content (JSON-RPC request):**
```json
{
  "id": "<random-request-uuid>",
  "method": "sign_event",
  "params": ["<unsigned-event-json-string>"]
}
```

**Response (signer → client):**
```json
{
  "kind": 24133,
  "pubkey": "<signer-pubkey-hex>",
  "created_at": 1234567890,
  "content": "<NIP-44 encrypted JSON-RPC response>",
  "tags": [["p", "<client-pubkey-hex>"]],
  "id": "<sha256-event-id>",
  "sig": "<schnorr-signature>"
}
```

**Decrypted content (JSON-RPC response):**
```json
{
  "id": "<same-request-uuid>",
  "result": "<result-string-or-null>",
  "error": "<error-string-or-null>"
}
```

**CRITICAL: The p-tag on the response points to the CLIENT's pubkey (the recipient), NOT the signer's own pubkey.**

### 4.3 Connection Flow (Client-Initiated)

This is the primary flow. The client scans a bunker:// QR from Signstr, OR the user scans/pastes a nostrconnect:// URI from the client.

**Step-by-step:**

1. Client generates a throwaway keypair for this session
2. Client creates: `nostrconnect://<client-pubkey>?relay=wss://relay.damus.io&secret=<random>&name=AppName`
3. User scans/pastes this URI in Signstr
4. Signstr parses URI, extracts: client pubkey, relays, secret, app name
5. Signstr derives NIP-44 conversation key: `HKDF-extract(ECDH(signer_privkey, client_pubkey))`
6. Signstr creates NIP46Session object and stores it
7. Signstr subscribes to kind 24133 events on all specified relays + fallback relay
8. **Signstr sends connect response IMMEDIATELY** (does not wait for client message):
   ```json
   {"id": "<random-uuid>", "result": "<echoed-secret>"}
   ```
9. Response encrypted with NIP-44, published to all relays + fallback (`wss://relay.damus.io`)
10. Client receives response, verifies secret matches, session established

### 4.4 Supported NIP-46 Methods

| Method | Auth Required | Implementation |
|--------|--------------|----------------|
| `connect` | No | Returns "ack" with echoed secret |
| `get_public_key` | No | Returns signer's hex pubkey directly. NO biometrics — it's public information. |
| `sign_event` | Yes (approval) | Full signing flow with optional approval UI |
| `nip04_encrypt` | Yes | NIP-04 encryption with signer's key |
| `nip04_decrypt` | Yes | NIP-04 decryption with signer's key |
| `nip44_encrypt` | Yes | NIP-44 encryption with signer's key |
| `nip44_decrypt` | Yes | NIP-44 decryption with signer's key |

### 4.5 sign_event Flow (Critical)

Clients send unsigned events WITHOUT `id` or `sig` fields. Signstr must:

1. **Parse** the event JSON using loose JSON parsing (NOT strict typed deserialization — `id` and `sig` fields may be absent)
2. **Extract** fields: `kind` (Int), `content` (String, default ""), `tags` ([[String]], default []), `created_at` (Int, default now)
3. **Set pubkey** to the signer's x-only public key hex
4. **Compute event ID** (NIP-01):
   ```
   Canonical JSON: [0, "<pubkey>", <created_at>, <kind>, <tags>, "<content>"]
   Event ID = SHA-256(UTF-8 bytes of canonical JSON)
   ```
   - No whitespace in JSON
   - Tags serialized as nested string arrays
   - Strings escaped per RFC 8259 (\", \\, \n, \r, \t, control chars as \uXXXX)
5. **Sign** the 32-byte event ID hash with Schnorr (BIP-340) using the identity's private key
6. **Return** the complete signed event as a JSON string in the `result` field:
   ```json
   {"id":"<64-hex>","pubkey":"<64-hex>","created_at":123,"kind":1,"tags":[],"content":"hello","sig":"<128-hex>"}
   ```

### 4.6 Auto-Approve Logic

Two independent policy systems are checked. If EITHER says auto-approve, the event is signed immediately without prompting.

**Check 1: Per-identity safe-kind policy**
```
identity = lookupIdentityBySignerPubkey(targetPubkey)
isSafeKind = identity.approvalPolicy.safeKinds.contains(eventKind)
safeKindAutoApprove = isSafeKind AND NOT identity.approvalPolicy.requireApprovalForAll
```

**Check 2: Per-app timed trust policy**
```
policyAutoApprove = ApprovalPolicyStore.shouldAutoApprove(for: clientPubkey)
```

**Final decision:**
```
autoApprove = policyAutoApprove OR safeKindAutoApprove
```

**Default safe kinds:**
```
{ 0, 3, 10000, 10001, 10002, 22242 }
```

| Kind | Name | Rationale |
|------|------|-----------|
| 0 | Profile metadata | Read-only profile update, low risk |
| 3 | Contacts list | Follow list sync, low risk |
| 10000 | Mute list | Privacy setting, low risk |
| 10001 | Pin list | Content curation, low risk |
| 10002 | Relay list metadata | Relay preferences, low risk |
| 22242 | Relay authentication (NIP-42) | Auth challenge, required for relay access |

**Non-safe kinds (require approval by default):**
- Kind 1 (text note — user-visible posts)
- Kind 4 (encrypted DM)
- Kind 5 (event deletion)
- Kind 7 (reaction)
- Kind 9735 (zap receipt)
- All others

### 4.7 Fast Path vs. Approval Path

**Fast path (auto-approved):**
1. Sign immediately using Schnorr directly — NO biometrics
2. Fire-and-forget the relay send (non-blocking, unblocks queue immediately)
3. Log as "SAFE-AUTO" or "AUTO-APPROVED"

**Approval path (requires user action):**
1. Fire local notification if app is backgrounded
2. Show SigningRequestView overlay
3. User taps APPROVE → trigger biometric authentication → on success, sign with Schnorr
4. User taps REJECT → send error response: "User rejected the signing request"
5. Log as "APPROVED" or "REJECTED"

### 4.8 Biometric Gate for Non-Safe Kinds

When a sign_event request is NOT auto-approved:

1. Check if biometrics are available on device
2. If available: prompt for fingerprint / face / iris with reason "Sign a Nostr event"
3. If biometrics pass: sign with the identity's private key, return signed event
4. If biometrics fail: return error response "Biometric authentication failed"
5. If biometrics unavailable (e.g. emulator, no hardware): skip biometrics, allow signing

### 4.9 Event Deduplication

The same kind 24133 event can arrive on multiple relays simultaneously (e.g. a client publishes to both relay.damus.io and relay.primal.net).

**Implementation:**
- Maintain a map: `processedEventIDs: Map<String, Long>` (event ID → timestamp)
- Before processing any event, check if its ID is already in the map
- If found: skip silently ("Skipping duplicate event")
- If not found: add to map, process normally
- Cleanup: every 5 minutes, remove entries older than 5 minutes
- This prevents duplicate signing and duplicate approval prompts

### 4.10 Request Queuing

Multiple sign_event requests can arrive simultaneously (e.g. Primal sends kind 0, 3, 10002 in rapid succession on connect). Only one approval prompt can be shown at a time.

**Implementation:**
- Queue: `requestQueue: List<(encryptedContent, session, relayURL, queuedAt)>`
- Flag: `isProcessingRequest: Boolean`
- `enqueueRequest()`: add to queue tail, call `processNextInQueue()`
- `processNextInQueue()`: if not processing and queue not empty, pop head, set flag, process. When done, clear flag, call `processNextInQueue()` again.
- Auto-approved events unblock the queue immediately (fire-and-forget relay send)
- Manual-approval events block the queue until user approves/rejects

### 4.11 Encryption Detection (Incoming Messages)

Signstr auto-detects whether incoming NIP-46 messages use NIP-44 or NIP-04 encryption:

**Heuristic:**
1. If content contains `?iv=` → try NIP-04 first, then NIP-44 fallback
2. Otherwise → try NIP-44 (HKDF conversation key) first, then NIP-44 (raw ECDH, no HKDF) as fallback, then NIP-04 as final fallback

**Outgoing encryption:** Always NIP-44. Modern clients (nostr-tools) expect NIP-44 for NIP-46.

### 4.12 NIP-46 Relay Subscription Filter

```json
{
  "kinds": [24133],
  "#p": ["<identity1_pubkey>", "<identity2_pubkey>", ...],
  "since": <now_unix_timestamp - 60>
}
```

- `#p` includes ALL identity pubkeys (multi-identity support)
- `since` is 60 seconds ago (to handle clock skew between devices and relays)
- No `limit` field (some relays reject `limit: 0`)
- Subscription ID format: `"signstr-<first8chars_of_primary_pubkey>"`

### 4.13 Relay Message Handling

The service handles these relay message types:

| Message | Action |
|---------|--------|
| `["EVENT", subId, eventDict]` | Process kind 24133 events (main flow) |
| `["OK", eventId, accepted, reason]` | Track event acceptance, clear pending OK |
| `["AUTH", challenge]` | Handle NIP-42 auth challenges (sign and send auth event) |
| `["EOSE", subId]` | End of stored events — log and continue |
| `["NOTICE", message]` | Log relay notice |

### 4.14 Client Compatibility

| Client | Encryption | Tested Relays | Status |
|--------|-----------|---------------|--------|
| Coracle | NIP-44 | bucket.coracle.social, ephemeral.snowflare.cc | Working |
| Primal Web | NIP-44 | relay.primal.net | Working |
| noStrudel | NIP-44 | relay.nsec.app (problematic) | Relay issues |
| Amethyst | NIP-44 | Various | Untested |

**Problematic relay:** `wss://relay.nsec.app` silently drops all kind 24133 events (no OK, no AUTH, no error). Mitigation: always add `wss://relay.damus.io` as a fallback relay to every connection.

---

## 5. NIP-44 ENCRYPTION IMPLEMENTATION

### 5.1 Wire Format

```
[version_byte(0x02)] [nonce(32 bytes)] [padded_ciphertext(variable)] [mac(32 bytes)]
```

Entire payload is base64-encoded for transmission in the Nostr event content field.

Minimum payload size: 1 + 32 + 32 + 32 = 97 bytes (before base64 encoding).

### 5.2 Conversation Key Derivation (HKDF-Extract ONLY)

**CRITICAL: This uses HKDF-extract ONLY, NOT extract+expand. Getting this wrong was the bug that blocked iOS development for days.**

```
Step 1: ECDH
  shared_x = secp256k1_ecdh(signer_privkey, 0x02 || client_pubkey_x)
  Result: 32 bytes (x-coordinate only, drop parity byte)

Step 2: HKDF-extract (NOT expand)
  conversation_key = HMAC-SHA256(key = "nip44-v2" as UTF-8 bytes, data = shared_x)
  Result: 32 bytes
```

The conversation key is stable per (signer, client) pair and is reused for all messages in the session.

### 5.3 ECDH Shared Secret

```
Input:  32-byte private key, 32-byte x-only public key
Process:
  1. Parse private key as secp256k1 scalar
  2. Prepend 0x02 to public key to make it compressed format (33 bytes)
  3. Perform secp256k1 ECDH key agreement
  4. Take the 32-byte x-coordinate from the result point (drop the parity byte)
Output: 32 bytes
```

### 5.4 Message Key Derivation (HKDF-Expand)

Per-message. A fresh 32-byte random nonce is generated for each message.

```
Input:
  PRK = conversation_key (32 bytes)
  info = nonce (32 bytes, random per message)
  L = 76 bytes

Algorithm (RFC 5869 §2.3):
  T(1) = HMAC-SHA256(PRK, info || 0x01)                    → 32 bytes
  T(2) = HMAC-SHA256(PRK, T(1) || info || 0x02)            → 32 bytes
  T(3) = HMAC-SHA256(PRK, T(2) || info || 0x03)            → 32 bytes
  OKM = (T(1) || T(2) || T(3))[0:76]                       → 76 bytes

Split:
  chacha_key   = OKM[0:32]    (32 bytes)
  chacha_nonce = OKM[32:44]   (12 bytes)
  hmac_key     = OKM[44:76]   (32 bytes)
```

### 5.5 Padding

Plaintext is padded before encryption:

```
Format: [2-byte big-endian length] [plaintext] [zero padding to reach padded length]

Padded length calculation:
  if length == 0:  padded = 32
  if length <= 32: padded = 32
  else:
    nextPower = next power of 2 >= length
    chunk = max(32, nextPower / 8)
    padded = ceil(length / chunk) * chunk

Minimum total: 2 (length prefix) + 32 (minimum padded content) = 34 bytes
```

**Unpadding:**
```
Read 2-byte big-endian length prefix from start of padded data
Extract exactly `length` bytes starting at offset 2
```

### 5.6 Encryption Flow

```
1. Pad plaintext (2-byte length + zero-pad)
2. Generate 32 random bytes as nonce (SecureRandom)
3. Derive message keys: HKDF-expand(conversation_key, nonce, 76)
4. Encrypt padded plaintext with ChaCha20:
   - Key: chacha_key (32 bytes)
   - Nonce: chacha_nonce (12 bytes)
   - Counter starts at 0
   - Raw ChaCha20 stream cipher (NOT ChaCha20-Poly1305 AEAD)
5. Compute MAC: HMAC-SHA256(hmac_key, nonce || ciphertext)
6. Assemble: 0x02 || nonce || ciphertext || mac
7. Base64-encode entire payload
```

### 5.7 Decryption Flow

```
1. Base64-decode payload
2. Validate: length >= 97 bytes
3. Validate: first byte == 0x02
4. Extract:
   - nonce = bytes[1:33]
   - ciphertext = bytes[33:length-32]
   - received_mac = bytes[length-32:length]
5. Derive message keys: HKDF-expand(conversation_key, nonce, 76)
6. Verify MAC FIRST (before decryption):
   - expected_mac = HMAC-SHA256(hmac_key, nonce || ciphertext)
   - Constant-time comparison: received_mac == expected_mac
   - If mismatch: FAIL (do not attempt decryption)
7. Decrypt ciphertext with ChaCha20 (same operation as encryption)
8. Unpad: read 2-byte length, extract plaintext
9. Decode as UTF-8 string
```

### 5.8 ChaCha20 Implementation (Raw, NOT AEAD)

**CRITICAL: NIP-44 uses plain ChaCha20, NOT ChaCha20-Poly1305. The authentication is provided by the separate HMAC-SHA256.**

**ChaCha20 state layout (16 × 32-bit words):**
```
[ 0x61707865  0x3320646e  0x79622d32  0x6b206574 ]   ← "expand 32-byte k"
[   key[0]      key[1]      key[2]      key[3]    ]
[   key[4]      key[5]      key[6]      key[7]    ]
[  counter     nonce[0]    nonce[1]    nonce[2]   ]
```

All key and nonce words are loaded as little-endian UInt32.

**Quarter round:**
```
a += b; d ^= a; d <<<= 16;
c += d; b ^= c; b <<<= 12;
a += b; d ^= a; d <<<= 8;
c += d; b ^= c; b <<<= 7;
```

**Block function:**
```
20 rounds (10 iterations of: 4 column rounds + 4 diagonal rounds):
  Column:   QR(0,4,8,12)  QR(1,5,9,13)  QR(2,6,10,14) QR(3,7,11,15)
  Diagonal: QR(0,5,10,15) QR(1,6,11,12) QR(2,7,8,13)  QR(3,4,9,14)

After 20 rounds: add original state to working state (mod 2^32)
Serialize 16 words to 64 bytes (little-endian)
```

**Stream cipher:**
```
For each 64-byte block:
  keystream = chacha20_block(key, counter, nonce)
  output[i] = input[i] XOR keystream[i]
  counter += 1
Last block: only XOR the remaining bytes (< 64)
```

### 5.9 HMAC-SHA256

```
MAC = HMAC-SHA256(key = hmac_key, data = nonce || ciphertext)
```

**Constant-time comparison for MAC verification:**
```
function constantTimeEqual(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false
    var diff: Byte = 0
    for (i in a.indices) {
        diff = diff or (a[i] xor b[i])
    }
    return diff == 0
}
```

### 5.10 Test Vectors

All vectors are from the official NIP-44 specification. The iOS implementation passes all of these.

#### Conversation Key Derivation Vectors

**Vector 1:**
```
sec1 = 315e59ff51cb9209768cf7da80791ddcaae56ac9775eb25b6dee1234bc5d2268
pub2 = c2f9d9948dc8c7c38321e4b85c8558872eafa0641cd269db76848a6073e69133
expected_conversation_key = 3dfef0ce2a4d80a25e7a328accf73448ef67096f65f79588e358d9a0eb9013f1
```

**Vector 2:**
```
sec1 = a1e37752c9fdc1273be53f68c5f74be7c8905728e8de75800b94262f9497c86e
pub2 = 03bb7947065dde12ba991ea045132581d0954f042c84e06d8c00066e23c1a800
expected_conversation_key = 4d14f36e81b8452128da64fe6f1eae873baae2f444b02c950b90e43553f2178b
```

**Vector 3:**
```
sec1 = 98a5902fd67518a0c900f0fb62158f278f94a21d6f9d33d30cd3091195500311
pub2 = aae65c15f98e5e677b5050de82e3aba47a6fe49b3dab7863cf35d9478ba9f7d1
expected_conversation_key = 9c00b769d5f54d02bf175b7284a1cbd28b6911b06cda6666b2243561ac96bad7
```

#### Message Key Derivation Vectors

**Conversation key for both:** `a1a3d60f3470a8612633924e91febf96dc5366ce130f658b1f0fc652c20b3b54`

**Vector 1:**
```
nonce      = e1e6f880560d6d149ed83dcc7e5861ee62a5ee051f7fde9975fe5d25d2a02d72
chacha_key = f145f3bed47cb70dbeaac07f3a3fe683e822b3715edb7c4fe310829014ce7d76
chacha_nonce = c4ad129bb01180c0933a160c
hmac_key   = 027c1db445f05e2eee864a0975b0ddef5b7110583c8c192de3732571ca5838c4
```

**Vector 2:**
```
nonce      = e1d6d28c46de60168b43d79dacc519698512ec35e8ccb12640fc8e9f26121101
chacha_key = e35b88f8d4a8f1606c5082f7a64b100e5d85fcdb2e62aeafbec03fb9e860ad92
chacha_nonce = 22925e920cee4a50a478be90
hmac_key   = 46a7c55d4283cb0df1d5e29540be67abfe709e3b2e14b7bf9976e6df994ded30
```

#### Full Encrypt/Decrypt Vectors

**Setup:** sec1 = 0x01 (32 bytes), sec2 = 0x02 (32 bytes)

**Conversation key (sec1=1, sec2=2):** `c41c775356fd92eadc63ff5a0dc1da211b268cbea22316767095b2871ea1412d`

**Vector 1 (plaintext = "a"):**
```
nonce    = 0000000000000000000000000000000000000000000000000000000000000001
expected = AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABee0G5VSK0/9YypIObAtDKfYEAjD35uVkHyB0F4DwrcNaCXlCWZKaArsGrY6M9wnuTMxWfp1RTN9Xga8no+kF5Vsb
```

**Vector 2 (plaintext = "🍕🫃" — pizza emoji + pregnant man emoji):**
```
nonce    = f00000000000000000000000000000f00000000000000000000000000000000f
expected = AvAAAAAAAAAAAAAAAAAAAPAAAAAAAAAAAAAAAAAAAAAPSKSK6is9ngkX2+cSq85Th16oRTISAOfhStnixqZziKMDvB0QQzgFZdjLTPicCJaV8nDITO+QfaQ61+KbWQIOO2Yj
```

### 5.11 NIP-04 Legacy Support

Some older clients still use NIP-04 encryption. Signstr supports it as a fallback for incoming messages only.

**NIP-04 algorithm:**
```
1. ECDH: shared_secret = secp256k1_ecdh(privkey, pubkey) → raw 32 bytes (NO HKDF)
2. Encrypt: AES-256-CBC(key = shared_secret, iv = random 16 bytes, data = plaintext)
3. Format: base64(ciphertext) + "?iv=" + base64(iv)
```

---

## 6. MULTI-IDENTITY ARCHITECTURE

### 6.1 Data Model

```
NostrIdentity {
    id: String (UUID)
    displayName: String
    pubkeyHex: String (64-char hex, 32-byte x-only pubkey)
    createdAt: Date
    approvalPolicy: SigningApprovalPolicy
    pictureURL: String? (fetched from kind 0 metadata on relays)
    userHasRenamed: Boolean (prevents overwriting user's name with relay profile)
}

SigningApprovalPolicy {
    safeKinds: Set<Int>
    requireApprovalForAll: Boolean
}
```

### 6.2 Storage

| Data | Storage | Key Format |
|------|---------|-----------|
| Identity metadata (JSON array) | SharedPreferences/EncryptedPrefs | `"signstr.identities"` |
| Identity private key (hex string) | Android Keystore / EncryptedSharedPreferences | `"signstr.identity.nsec.<identity_uuid>"` |
| Active identity ID | SharedPreferences | `"signstr.active_identity_id"` |
| Onboarding complete flag | SharedPreferences | `"onboardingComplete"` |
| Key setup complete flag | SharedPreferences | `"keySetupComplete"` |

### 6.3 Connection Scoping per Identity

Each NIP-46 connection is associated with a specific identity via the `identityId` field in the saved connection:

```
SavedNIP46Connection {
    clientPubkey: String
    clientName: String?
    relayURLs: [String]
    signerPubkey: String (the identity's pubkey that owns this connection)
    encryption: String ("nip04" or "nip44")
    flow: String ("clientInitiated" or "signerInitiated")
    permissions: String?
    identityId: String? (UUID of owning identity)
}
```

**Storage:**
- Connection metadata: SharedPreferences JSON array at `"signstr.nip46_saved_connections"`
- Per-connection signer private key: Keychain/Keystore at `"signstr.nip46.privkey.<client_pubkey>"`

### 6.4 Incoming Event Routing by P-Tag

When a kind 24133 event arrives from a relay:

```
1. Extract all p-tags from the event
2. Find the first p-tag value that matches any of our identity pubkeys:
   targetPubkey = pTags.find { allSignerPubkeys.contains(it) }
3. Switch the active key context to the matched identity:
   signerPrivateKey = identityKeys[targetPubkey]
   signerPubkeyHex = targetPubkey
4. Look up the session by the sender's pubkey:
   session = sessions[senderPubkey]
5. Process the request using the correct identity's key material
```

### 6.5 Identity Picker

A horizontal scrolling row of circular chips, displayed at the top of the Connections tab and Events tab.

**Per chip:**
- 48dp circular avatar (profile picture if fetched, otherwise initials from display name)
- Display name below (10pt)
- Connection count badge
- Selected state: sgTextBright colour, slightly larger
- Unselected state: sgTextFaint colour
- Tap: sets as active identity, filters displayed connections/events
- Long-press: context menu with Rename, Copy npub, Back Up Key, Delete

**Plus button:** Rightmost chip with + icon, tapping opens Add Identity flow.

### 6.6 Profile Picture Fetching

On app launch or identity creation, Signstr fetches the kind 0 (metadata) event for each identity from relays to get the display name and profile picture URL.

---

## 7. PER-IDENTITY APPROVAL POLICIES

Each identity has its own `SigningApprovalPolicy`:

```
SigningApprovalPolicy {
    safeKinds: Set<Int>          // Default: {0, 3, 10000, 10001, 10002, 22242}
    requireApprovalForAll: Boolean  // Default: false
}
```

**Behaviour:**
- `requireApprovalForAll = false` + event kind in `safeKinds` → auto-approve (no prompt, no biometrics)
- `requireApprovalForAll = true` → always prompt, regardless of kind
- `requireApprovalForAll = false` + event kind NOT in `safeKinds` → check per-app policy, prompt if needed

**User edits:**
- Toggle "Require Approval for All" on Identity tab
- Edit safe kinds list in SafeKindsEditor sheet (add/remove kind numbers, reset to defaults)

---

## 8. PER-APP TIMED TRUST POLICIES

Independent of identity policies. Stored per client pubkey.

### 8.1 Policy Enum

```
ApprovalPolicy:
  ALWAYS_ASK        → Every request shows approval UI
  TRUST_FOR_SESSION → Auto-approve after first approval until app process dies
  TRUST_FOR_15_MIN  → Auto-approve for 15 minutes after first approval
  TRUST_FOR_1_HOUR  → Auto-approve for 1 hour
  TRUST_FOR_4_HOURS → Auto-approve for 4 hours
  TRUST_FOR_24_HOURS → Auto-approve for 24 hours
  TRUST_FOR_7_DAYS  → Auto-approve for 7 days
  ALWAYS_TRUST      → Auto-approve everything (not recommended)
```

### 8.2 Storage

| Data | Storage Key |
|------|------------|
| Per-app policies | `"signstr.approval_policies"` → `Map<clientPubkey, policyRawValue>` |
| First-approval timestamps | `"signstr.first_approval_times"` → `Map<clientPubkey, Double(unixTimestamp)>` |
| Default policy for new connections | `"signstr.default_approval_policy"` |

### 8.3 Auto-Approve Check

```
function shouldAutoApprove(clientPubkey: String): Boolean {
    policy = loadPolicy(clientPubkey) ?: defaultPolicy

    when (policy) {
        ALWAYS_ASK → return false
        ALWAYS_TRUST → return true
        TRUST_FOR_SESSION → return firstApprovalTime(clientPubkey) != null
        TRUST_FOR_15_MIN, TRUST_FOR_1_HOUR, etc. → {
            firstApproval = firstApprovalTime(clientPubkey) ?: return false
            elapsed = now - firstApproval
            return elapsed < policy.durationSeconds
        }
    }
}
```

### 8.4 First-Approval Recording

After the user approves a signing request (whether manually or auto-approved), record the timestamp IF it hasn't been recorded yet:

```
function recordFirstApproval(clientPubkey: String) {
    if (firstApprovalTimes[clientPubkey] == null) {
        firstApprovalTimes[clientPubkey] = currentUnixTimestamp
        persist()
    }
}
```

### 8.5 Policy Change

When the user changes a per-app policy in App Settings:
- Clear the first-approval timestamp for that client pubkey
- The new policy takes effect from next request

---

## 9. CONNECTION PERSISTENCE AND RELAY RECONNECTION

### 9.1 Connection Persistence

**On adding a connection:**
1. Save `SavedNIP46Connection` metadata to SharedPreferences JSON array
2. Save signer private key (hex) to secure storage, keyed by client pubkey

**On app launch (restoreConnections):**
1. Load all identity keys from IdentityManager → register in `identityKeys` map
2. Load all saved connections from SharedPreferences
3. For each saved connection:
   a. Load signer private key from secure storage
   b. Derive signer public key
   c. Re-derive NIP-44 conversation key from signer privkey + client pubkey
   d. Create NIP46Session with restored data
   e. Subscribe to all relays for this session
4. Sessions are ready to handle requests before any user interaction

### 9.2 Relay WebSocket Connection

- Uses platform WebSocket (OkHttp on Android)
- One WebSocket per relay URL (shared across sessions that use the same relay)
- On connect: send REQ subscription with kind 24133 filter
- Listen for messages in a receive loop

### 9.3 Exponential Backoff Reconnection

When a WebSocket disconnects with an error:

```
function scheduleReconnect(relayURL: String) {
    // Only reconnect if at least one session still uses this relay
    if (!sessions.any { it.relays.contains(relayURL) }) {
        return  // No sessions need this relay
    }

    // Cancel any existing reconnect timer
    cancelReconnectTask(relayURL)

    attempt = (reconnectAttempts[relayURL] ?: 0) + 1
    reconnectAttempts[relayURL] = attempt

    // Exponential backoff: 1s, 2s, 4s, 8s, 16s, 30s (capped at 30)
    delay = min(2^(attempt-1), 30) seconds

    scheduleDelayed(delay) {
        if (sessions.any { it.relays.contains(relayURL) } && !isConnected(relayURL)) {
            subscribeToRelay(relayURL)  // Reconnect
        }
    }
}
```

**Backoff sequence:** 1s → 2s → 4s → 8s → 16s → 30s → 30s → 30s → ...

**Reset:** On successful message receipt, reset `reconnectAttempts[relayURL] = 0`.

### 9.4 Relay Cleanup on Disconnect

When a session is removed:
- Check if any remaining session uses the same relay URLs
- If no session uses a relay URL: close the WebSocket, cancel reconnect timers, clear attempt counter
- If other sessions use it: leave the connection open

---

## 10. EVENT LOG DATA MODEL AND STORAGE

### 10.1 Data Model

```
SigningLogEntry {
    id: UUID
    timestamp: Date
    appName: String                // Client app display name
    clientPubkey: String           // Client's 64-char hex pubkey
    eventKind: Int                 // Nostr event kind number
    contentPreview: String         // First 500 chars of event content
    approved: Boolean              // Whether the request was approved
    autoApproved: Boolean          // Any auto-approval type
    safeKindAutoApproved: Boolean  // Specifically safe-kind auto-approved
    eventJSON: String              // Full unsigned event JSON
    identityId: String?            // UUID of owning identity
}
```

### 10.2 Computed Properties

```
kindDescription → human-readable kind label (see kind map in section 3.10)

statusBadge:
    if !approved → "REJECTED"
    if safeKindAutoApproved → "SAFE-AUTO"
    if autoApproved → "AUTO-APPROVED"
    else → "APPROVED"

truncatedContent:
    if contentPreview.length > 120 → contentPreview[0:120] + "..."
    else → contentPreview
```

### 10.3 Storage

- SharedPreferences JSON array at `"signstr.signing_log"`
- Newest entries first (prepended)
- Capped at 200 entries per identity (oldest removed when exceeded)
- Backwards-compatible decoding: older entries that lack `autoApproved`, `safeKindAutoApproved`, or `identityId` fields get default values (false, false, null)

### 10.4 Querying

```
entries(forIdentity identityId) → filter entries where identityId matches
```

---

## 11. BUNKER URI FORMAT AND QR CODE GENERATION

### 11.1 URI Formats

**bunker:// (signer-initiated — Signstr generates this for clients to scan):**
```
bunker://<signer-pubkey-hex>?relay=wss%3A%2F%2Frelay.damus.io&relay=wss%3A%2F%2Fnos.lol&secret=<random-32-hex-chars>
```

**nostrconnect:// (client-initiated — client generates this for Signstr to scan):**
```
nostrconnect://<client-pubkey-hex>?relay=wss%3A%2F%2Frelay.damus.io&secret=<random>&name=AppName&perms=sign_event
```

### 11.2 URI Components

| Component | Description |
|-----------|-------------|
| Scheme | `bunker://` or `nostrconnect://` |
| Authority | 64-character hex pubkey (signer's for bunker://, client's for nostrconnect://) |
| `relay` param | URL-encoded relay URL (wss:// → wss%3A%2F%2F). Can appear multiple times. |
| `secret` param | Random hex string (32 characters, UUID without hyphens or similar) |
| `name` param | Display name of the app (only in nostrconnect://) |
| `perms` param | Comma-separated permissions (only in nostrconnect://, e.g. "sign_event,get_public_key") |

### 11.3 QR Code Generation

- Generate bunker:// URI from active identity's pubkey + configured relays + random secret
- Render as QR code (standard QR encoding, high error correction)
- White QR on dark background (or white background card)
- 180×180dp size on Identity tab

### 11.4 Parsing

```
function parseURI(uri: String): NIP46ConnectionInfo {
    url = parseURL(uri)

    flow = when (url.scheme) {
        "nostrconnect" → CLIENT_INITIATED
        "bunker" → SIGNER_INITIATED
        else → throw InvalidURIError
    }

    pubkey = url.host  // 64-char hex (no 0x prefix)
    validate: pubkey.length == 64, all hex characters

    relays = url.queryParameters.getAll("relay")  // URL-decoded
    secret = url.queryParameters.get("secret")
    name = url.queryParameters.get("name")
    permissions = url.queryParameters.get("perms")

    return NIP46ConnectionInfo(flow, pubkey, relays, secret, name, permissions)
}
```

---

## 12. NOTIFICATION LOGIC

### 12.1 When Notifications Fire

A local notification fires when ALL of these conditions are true:
1. A sign_event request requires manual approval (not auto-approved)
2. The `notificationsEnabled` preference is true (default: true)
3. The app is NOT in the foreground (application state != active)

### 12.2 Notification Content

```
Title: "Signing Request"
Body: "[App Name] wants to sign a kind [N] event"
Sound: default system sound
```

Example: "Primal wants to sign a kind 1 event"

### 12.3 When Notifications Do NOT Fire

- Auto-approved events (safe kinds or per-app trust policy) → no notification, no UI
- App is in the foreground → notification suppressed (the approval UI is already visible)
- Notifications disabled in settings → no notification (but approval UI still shows)
- Non-sign_event methods (connect, get_public_key) → no notification

### 12.4 Notification Suppression (Foreground)

When the app is in the foreground, suppress notification banners/sounds entirely. The signing request approval overlay is already visible to the user.

### 12.5 Notification Tap Behaviour

When the user taps a notification:
- Bring the app to the foreground
- The pending signing request approval overlay is already displayed
- No additional routing needed

### 12.6 Permission Request

Request notification permission on first app launch (in Application/Activity onCreate):
- Options: alert, sound, badge
- Graceful degradation if denied (app still works, just no background notifications)

---

## 13. EVERY SETTING AND WHAT IT CONTROLS

### SIGNING Section

| Setting | Type | Storage Key | Default | Description |
|---------|------|------------|---------|-------------|
| Default Approval Policy | Picker (ApprovalPolicy enum) | `signstr.default_approval_policy` | Always Ask | Applied to new connections that have no per-app policy |
| Biometrics | Toggle | `biometricsEnabled` | true | Require biometric auth for non-auto-approved signing requests |
| NIP-46 Relays | Navigation | — | 3 defaults | Configure relay URLs used for NIP-46 communication |

### NOTIFICATIONS Section

| Setting | Type | Storage Key | Default | Description |
|---------|------|------------|---------|-------------|
| Notifications | Toggle | `notificationsEnabled` | true | Fire local notification when signing approval is needed and app is backgrounded |

### GENERAL Section

| Setting | Type | Visibility | Description |
|---------|------|------------|-------------|
| About | Navigation | Always | Version, licence, credits, links |
| NIP-46 Test Client | Navigation | Debug only | Internal tool to test NIP-46 connect flow |
| Developer Options | Navigation | Debug only | View application logs |
| Reset Onboarding | Button | Debug only | Re-show onboarding on next launch |

### DANGER ZONE Section

| Setting | Type | Auth Required | Description |
|---------|------|--------------|-------------|
| Export All Keys | Button → Sheet | Biometrics | Reveals all identity nsecs (masked, tap to reveal, copy with 30s auto-clear) |
| Delete All Data | Button → 2-step confirm | None | Disconnects sessions, deletes identities+keys, clears policies+logs, returns to Key Setup |
| Reset App | Button → 2-step confirm | None | Same as Delete All Data + clears onboarding flag, returns to Onboarding |

---

## 14. THE GHOST DESIGN SYSTEM

### 14.1 Colour Palette

**CRITICAL: All colours specified as hex. Dark mode ONLY. No light mode.**

| Token | Hex | Usage |
|-------|-----|-------|
| `sgBg` | `#09090b` | App background (OLED-friendly near-black) |
| `sgBgRaised` | `#0e0e10` | Cards, list items, raised surfaces |
| `sgBgSurface` | `#111113` | Input backgrounds, card interiors |
| `sgBorder` | `#1a1a1e` | Default borders, dividers, button backgrounds |
| `sgBorderHover` | `#28282e` | Active borders, selected states, button borders |
| `sgTextGhost` | `#38383e` | Section labels, placeholders, lowest emphasis text |
| `sgTextFaint` | `#5a5a64` | Secondary text, descriptions |
| `sgTextMuted` | `#8a8a96` | Nav titles, body text, tab labels |
| `sgTextBody` | `#b8b8c4` | Primary content text, card titles (note: iOS spine says `#a8a8b4` but code uses `#b8b8c4` — use code value) |
| `sgTextBright` | `#d8d8e0` | Headings, emphasis, button text (note: iOS spine says `#cdcdd6` but code uses `#d8d8e0` — use code value) |
| `sgTextWhite` | `#e4e4ec` | Maximum emphasis (rare use) |
| `sgDanger` | `#c45555` | Wrong PIN, delete, warnings, danger buttons |
| `sgDangerBorder` | `#3d2020` | Warning box borders |
| `sgDangerBg` | `rgba(60, 30, 30, 0.15)` | Warning box backgrounds (translucent) |

### 14.2 Typography

**Primary font: Outfit** (Google Fonts, free, variable weight)

| Weight Name | CSS Weight | File |
|-------------|-----------|------|
| ExtraLight | 200 | Outfit-ExtraLight.ttf |
| Light | 300 | Outfit-Light.ttf |
| Regular | 400 | Outfit-Regular.ttf |
| Medium | 500 | Outfit-Medium.ttf |
| Bold | 700 | Outfit-Bold.ttf |

**Monospace:** System default monospaced font (for keys, pubkeys, hex values)

### 14.3 Text Styles

All text uses 6dp line spacing.

**Navigation/Chrome styles:**

| Style | Font | Size | Letter Spacing | Case | Colour |
|-------|------|------|---------------|------|--------|
| navTitle | Outfit-Regular | 11sp | +5sp | UPPERCASE | sgTextMuted |
| navButton | Outfit-Regular | 10sp | +2sp | UPPERCASE | sgTextMuted |
| sectionLabel | Outfit-Regular | 9sp | +3sp | UPPERCASE | sgTextMuted |
| tabBarLabel | Outfit-Regular | 8sp | +1sp | UPPERCASE | sgTextMuted |
| tagBadge | Outfit-Regular | 9sp | +1sp | UPPERCASE | sgTextMuted |
| counterText | Outfit-Regular | 9sp | +1.5sp | UPPERCASE | sgTextMuted |

**Body content styles:**

| Style | Font | Size | Letter Spacing | Colour |
|-------|------|------|---------------|--------|
| cardTitle | Outfit-Regular | 12sp | 0 | sgTextBody |
| body | Outfit-Light | 12sp | 0 | sgTextBody |
| subtitle | Outfit-Light | 12sp | 0 | sgTextBody |
| inputText | Outfit-Light | 13sp | 0 | sgTextBright |
| cellSmallTitle | Outfit-Light | 9sp | 0 | sgTextMuted |
| menuItemTitle | Outfit-Regular | 12sp | 0 | sgTextBody |

**Heading/emphasis styles:**

| Style | Font | Size | Letter Spacing | Case | Colour |
|-------|------|------|---------------|------|--------|
| onboardingHeading | Outfit-Light | 16sp | +0.5sp | Normal | sgTextBright |
| title / viewTitle | Outfit-Light | 16sp | +0.5sp | Normal | sgTextBright |
| slotTitle | Outfit-ExtraLight | 16sp | 0 | Normal | sgTextBright |
| buttonText | Outfit-Regular | 11sp | +4sp | UPPERCASE | sgTextBright |
| subtitleBold | Outfit-Regular | 11sp | +4sp | UPPERCASE | sgTextBright |

### 14.4 Spacing & Dimensions

| Token | Value | Usage |
|-------|-------|-------|
| lateralPadding | 16dp | Screen side margins |
| defaultSideMargin | 16dp | Card side margins |
| cardPadding | 16dp | Internal card padding |
| cardCornerRadius | 12dp | Card corner radius |
| buttonCornerRadius | 10dp | Button corner radius |
| inputCornerRadius | 8dp | Text input corner radius |
| logoHeight | 120dp | Logo/illustration height |
| verticalLogoSpacing | 32dp | Space below logo |
| subtitleSpacing | 12dp | Space below subtitle |
| verticalIllustrationSpacing | 20dp | Space below illustrations |
| defaultBottomMargin | 40dp | Bottom margin |
| firstButtonPadding | 50dp | Primary button top padding |
| secondButtonPadding | 30dp | Secondary button top padding |

### 14.5 Component Specifications

#### SKButton (Primary Action Button)

- Height: 46dp
- Corner radius: 10dp
- Font: Outfit-Regular 11sp, +4sp letter spacing, UPPERCASE
- Horizontal padding: 16dp (default)
- Styles:
  - **confirm/inform/regular**: Background sgBorder, text sgTextBright, border sgBorderHover
  - **danger**: Background transparent, text sgDanger, border sgDangerBorder

#### SKLabel (Info Card Row)

- Background: sgBgSurface
- Corner radius: 8dp
- Min height: 33dp
- Title: cardTitle style (Outfit-Regular 12sp, sgTextBody)
- Content: Outfit-Regular 16sp
- Spacing: 2dp between title and content

#### SKActionButtonSmall (Inline Action Button)

- Height: 40dp
- Corner radius: 10dp
- Background: sgBorder
- Font: Outfit-Regular 18sp
- Icon size: 20×20dp
- Horizontal padding: 10dp

#### HeaderView (App Header Bar)

- Height: 52dp
- Logo: "SIGNSTR" wordmark, Outfit-Regular 11sp, +5sp letter spacing
- Background: sgBg with bottom border overlay (sgBorder, 0.6 opacity)
- Horizontal padding: 16dp

#### NumericKeypad (PIN Entry)

- Layout: 3×4 grid (1-9, blank, 0, delete)
- Key height: 52dp
- Key background: sgBgRaised
- Key corner radius: 10dp
- Font: Outfit-Regular 20sp
- Grid spacing: 10dp
- Max width: 320dp
- Delete icon: "backspace" icon, 18dp
- Haptic feedback: light impact on press/delete

#### ClipboardToast (Confirmation Toast)

- Background: sgBgRaised with sgBorder stroke
- Corner radius: 8dp
- Font: Outfit-Light 11sp, sgTextFaint
- Padding: 16dp horizontal, 8dp vertical
- Position: bottom of screen, 24dp margin
- Animation: 0.5s fade in/out
- Auto-dismiss: 2 seconds

#### SecureTextInput (Password/Key Input)

- Background: sgBgSurface
- Border: sgBorder, 1dp stroke
- Corner radius: 8dp
- Eye icon: toggle reveal/hide, sgTextBright
- Padding: all sides

#### Identity Picker Chips

- Avatar size: 48×48dp, circular
- Horizontal scroll, no scroll indicators
- Chip spacing: 12dp
- Name font: Outfit-Regular 10sp
- Padding: 24dp horizontal, 8dp vertical
- Selected: sgTextBright
- Unselected: sgTextFaint
- Add button: circle with plus icon, sgTextMuted

#### Custom Toggle (Settings)

- Size: 50×30dp
- Corner radius: 16dp (fully rounded)
- Thumb: circle, 2dp inset, animated offset ±10dp
- Animation: 0.2s
- On colour: customisable (default sgBorderHover)
- Off colour: customisable (default sgBorder)

#### EmptyScanStateOverlay Card

- Card size: 260×164dp
- Corner radius: 12dp
- Background gradient: linear #121216 → #0e0e11 → #0b0b0e
- EMV chip: 36×28dp, gold gradient (#a89058 → #8a7440 → #685830)
- Pulse rings: 3 concentric circles, #7a6840 opacity 0.12, scale to 3.2x, 3s animation

---

## 15. THE SPLASH SCREEN

### 15.1 Design

- **Background:** `#09090b` (sgBg)
- **Animation:** A fountain pen writing "Signstr." in a flowing signature style
- **Ink colour:** `rgba(185, 185, 195, 0.92)`
- **Sheen effect:** `rgba(220, 220, 235, 0.02)` — subtle shine following the pen stroke
- **Tagline:** "your keys. your identity." — appears below the signature, same colour family
- **Font for tagline:** Outfit (loaded from bundled font file)
- **Duration:** Approximately 3 seconds total display time

### 15.2 Animation Timing

The iOS implementation uses an HTML5 Canvas animation embedded in a WebView. For Android, this can be replicated with a Canvas/CustomView or Lottie animation.

| Segment | Duration | Description |
|---------|----------|-------------|
| Forward stroke (writing "Signstr") | 1200ms | Pressure-sensitive stroke width, flowing from left to right |
| Gap | 280ms | Pause before i-dot |
| i-dot | 15ms | Quick dot above the i |
| Gap | 80ms | Pause before t-cross |
| t-cross | 55ms | Horizontal stroke crossing the t |
| Gap | 350ms | Pause before period |
| Full stop (period) | 15ms | Dot after "Signstr" |
| Hold | 600ms | Display complete signature |

**Total animation: ~2.6 seconds. Splash screen holds for approximately 3 seconds total before dismissing.**

### 15.3 Implementation Notes

The iOS version embeds an HTML Canvas animation in a WebView with no external network requests. For Android:
- **Option A:** Port the Canvas animation to Android Canvas/CustomView with PathMeasure and ValueAnimator
- **Option B:** Export as Lottie JSON animation and use Lottie library
- **Option C:** Use a WebView with the same HTML (simplest port, but WebView startup adds latency)

The pen stroke has variable width (pressure simulation) — thicker at the start of strokes, thinner at the end. The sheen effect is a subtle gradient highlight that follows the pen tip.

---

## 16. ANDROID-SPECIFIC IMPLEMENTATION NOTES

### 16.1 Suggested Architecture

| iOS | Android Equivalent |
|-----|-------------------|
| SwiftUI | Jetpack Compose |
| @StateObject / ObservableObject | ViewModel + StateFlow |
| @EnvironmentObject | Hilt/Dagger dependency injection |
| UserDefaults | SharedPreferences / DataStore |
| Keychain | Android Keystore + EncryptedSharedPreferences |
| Secure Enclave | Android Keystore (TEE/StrongBox) |
| CryptoKit | javax.crypto / Bouncy Castle |
| P256K (swift-secp256k1) | secp256k1-kmp or fr.acinq.secp256k1 |
| URLSessionWebSocketTask | OkHttp WebSocket |
| AVFoundation (camera) | CameraX + ML Kit barcode scanner |
| LocalAuthentication (Face ID) | BiometricPrompt |
| Core Data | Room (if needed) or DataStore |
| UNUserNotificationCenter | NotificationCompat / NotificationManager |
| NavigationStack | Compose Navigation |

### 16.2 Key Libraries Needed

- **secp256k1:** fr.acinq.secp256k1:secp256k1-kmp (Kotlin multiplatform secp256k1 bindings)
- **WebSocket:** OkHttp (built-in WebSocket support)
- **QR scanning:** ML Kit Barcode Scanning or ZXing
- **QR generation:** ZXing or QR-code-kotlin
- **Biometrics:** AndroidX Biometric (BiometricPrompt)
- **Secure storage:** AndroidX Security (EncryptedSharedPreferences)
- **JSON:** kotlinx.serialization or Moshi
- **Fonts:** Outfit font (bundle TTF files, or use Google Fonts dependency)
- **Compose:** Material3 with custom dark theme matching Ghost palette

### 16.3 Security Considerations

- Store nsec in EncryptedSharedPreferences backed by Android Keystore
- Use BiometricPrompt with BIOMETRIC_STRONG for signing approval
- Zero sensitive byte arrays after use (fill with 0x00)
- Clear clipboard after 30 seconds when sensitive data is copied
- Never log private keys or nsec values (even in debug)
- Use SecureRandom for all nonce/secret generation

### 16.4 Bech32 Encoding

nsec and npub use standard bech32 (BIP-173), NOT bech32m. The charset is:
```
qpzry9x8gf2tvdw0s3jn54khce6mua7l
```

Convert between 8-bit data and 5-bit groups with appropriate padding. Use a well-tested library.

### 16.5 Schnorr Signatures (BIP-340)

- Input: 32-byte private key, 32-byte message hash (event ID)
- Output: 64-byte signature
- Algorithm: BIP-340 x-only Schnorr (secp256k1)
- Public key: 32-byte x-only (even parity)
- Verify: against x-only pubkey, hash, and signature

### 16.6 NIP-01 Event ID Computation

```
Canonical JSON array: [0, "<pubkey>", <created_at>, <kind>, <tags>, "<content>"]

Rules:
- No whitespace
- Strings properly escaped per RFC 8259
- Tags as nested string arrays: [["p","hex"],["e","hex"]]
- Numbers as-is (no quotes)
- UTF-8 encoding

Event ID = lowercase hex of SHA-256(UTF-8 bytes of canonical JSON)
```

---

*This document is the complete implementation reference for building Signstr on Android. Every behaviour described here is implemented and working in the iOS codebase. When in doubt, the iOS source code is the authority.*
