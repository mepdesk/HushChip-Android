# SIGNSTR iOS APP -- CLAUDE CODE SPINE DOCUMENT (v2)

**INCLUDE THIS DOCUMENT AT THE START OF EVERY CLAUDE CODE SESSION.**

This is the single source of truth for the Signstr iOS app project. It tells you what this project is, what the architecture looks like, and the design system to follow.

---

## WHAT IS THIS PROJECT

Signstr is a **Nostr identity vault and remote signer** for iOS. It is NOT a Nostr client. Users do not compose posts, browse feeds, or read messages in Signstr. They do all of that in their preferred Nostr client (Damus, Primal, Amethyst, etc.).

Signstr does two things:

1. **Holds the nsec** -- encrypted on device (Secure Enclave) or on a NostrKey NFC card
2. **Signs things when other apps ask** -- via NIP-46 (Nostr Connect) remote signing protocol

The user's Nostr client sends signing requests to Signstr over Nostr relays. Signstr shows the user what's being signed, the user approves (Face ID or card tap), Signstr signs and sends the signature back. The nsec NEVER touches the client app.

### The Two Tiers

**Tier 1 -- Software Vault (v1.0, free):**
nsec encrypted in iOS Secure Enclave. Face ID approves each signing request. Key never stored in plaintext. Already more secure than every Nostr client.

**Tier 2 -- Card Vault (v2.0, NostrKey card at GBP 14.99):**
nsec stored on NFC card secure element. Signing requires physical card tap. Key never touches the phone at all. Fully air-gapped.

### Why This Matters

There is NO standalone Nostr remote signer on iOS. Amber does this on Android. Nothing exists for iOS. Every iOS Nostr user currently pastes their nsec directly into clients. Signstr is first to market.

**Fork source:** github.com/hushchip/HushChip-iOS
**Upstream:** github.com/Toporin/Seedkeeper-iOS (Swift, GPL-3.0)
**Card applet (v2.0):** github.com/Toporin/SatochipApplet v0.14-0.2 (AGPLv3)
**Licence:** GPL-3.0 (must remain GPL-3.0, source must be public)
**Product name:** Signstr (app), NostrKey (physical card)
**Bundle ID:** uk.co.hushchip.signstr
**Website:** signstr.com

---

## NIP-46: NOSTR CONNECT (THE CORE PROTOCOL)

NIP-46 defines how a remote signer communicates with Nostr clients. Signstr IS the remote signer (called a "bunker" in NIP-46 terminology).

### How It Works

```
1. User has Signstr with their nsec stored securely
2. User opens Damus and chooses "Log in with Nostr Connect"
3. Damus shows a nostrconnect:// URI (as QR code or text)
4. User scans/pastes that URI in Signstr
5. Signstr connects to Damus via Nostr relays (kind 24133 events)
6. When Damus needs to sign something, it sends a request to Signstr
7. Signstr shows: "Damus wants to sign: 'Hello world' (kind 1)"
8. User approves (Face ID or card tap)
9. Signstr signs the event hash, sends signature back to Damus
10. Damus publishes the signed event
11. The nsec NEVER touches Damus
```

### NIP-46 Protocol Details

**Communication:** Encrypted kind 24133 events over Nostr relays, using NIP-44 encryption.

**Connection flows:**

Flow A -- Client initiates (most common):
```
Client generates: nostrconnect://<client-pubkey>?relay=wss://...&secret=<random>&name=Damus
User scans QR / pastes URI in Signstr
Signstr sends connect response to client-pubkey via specified relays
Session established
```

Flow B -- Signer initiates:
```
Signstr generates: bunker://<signer-pubkey>?relay=wss://...&secret=<random>
User pastes bunker URI into client
Client sends connect request
Session established
```

**Request format (client -> signer):**
```json
{
  "kind": 24133,
  "pubkey": "<client-pubkey>",
  "content": "<NIP-44 encrypted JSON-RPC>",
  "tags": [["p", "<signer-pubkey>"]]
}
```

Decrypted content:
```json
{
  "id": "<random-request-id>",
  "method": "<method-name>",
  "params": ["<array>", "<of>", "<strings>"]
}
```

**Response format (signer -> client):**
```json
{
  "id": "<same-request-id>",
  "result": "<result-string>",
  "error": "<optional-error>"
}
```

### NIP-46 Methods (Signstr Must Implement)

| Method | Params | Returns | Description |
|--------|--------|---------|-------------|
| `connect` | [pubkey, secret?, perms?] | "ack" | Establish connection |
| `get_public_key` | [] | hex pubkey | Return user's pubkey |
| `sign_event` | [unsigned_event_json] | signed_event_json | Sign an event |
| `get_relays` | [] | JSON relay map | Return user's relay preferences |
| `nip44_encrypt` | [third_party_pubkey, plaintext] | ciphertext | Encrypt for DMs |
| `nip44_decrypt` | [third_party_pubkey, ciphertext] | plaintext | Decrypt DMs |

MVP: `connect`, `get_public_key`, `sign_event`. The rest can follow.

### Approval Policies

The user configures how Signstr handles incoming signing requests:

| Policy | Behaviour |
|--------|-----------|
| **Always ask** | Every signing request shows approval UI. Most secure. Default. |
| **Trust for session** | After first approval, auto-approve for this app until Signstr is closed |
| **Trust for duration** | Auto-approve for N minutes/hours/days, then require approval again |
| **Trust by kind** | Auto-approve specific event kinds (e.g. kind 1 posts) but ask for others (e.g. kind 4 DMs) |
| **Always trust** | Auto-approve everything from this app. Least secure. Not recommended. |

With the card (Tier 2), the user can configure:
- **Tap per sign:** Every signature requires a physical card tap
- **Session tap:** Tap once to unlock a signing session, auto-approve for N minutes
- **PIN per sign:** Require card PIN for every signature (most secure)
- **PIN per session:** Enter PIN once, then tap-only for the session duration

---

## ARCHITECTURE

```
Signstr iOS App
|
+-- UI Layer (FORK FROM HUSHCHIP, REBRAND)
|   +-- Views / Screens (SwiftUI)
|   +-- Theme (Ghost palette, Outfit font)
|   +-- Assets
|   +-- Navigation / Routing
|
+-- NIP-46 Remote Signer Layer (NEW -- CORE FEATURE)
|   +-- NIP46Service.swift            Listens for signing requests from connected clients
|   +-- NIP46Session.swift            Manages a single client connection
|   +-- NIP46SessionManager.swift     Manages multiple connected clients
|   +-- NIP46MessageHandler.swift     Parses JSON-RPC requests, dispatches to signer
|   +-- NIP46ConnectionParser.swift   Parses nostrconnect:// and bunker:// URIs
|   +-- ApprovalPolicy.swift          Trust settings per connected app
|   +-- ApprovalManager.swift         Evaluates whether to prompt or auto-approve
|
+-- Nostr Logic Layer (BUILT -- Phase 1 complete)
|   +-- NostrEvent.swift              Event construction (NIP-01)
|   +-- NostrEventSerializer.swift    Event JSON serialisation for hashing
|   +-- NostrKeyUtils.swift           nsec/npub bech32 encoding/decoding
|   +-- NostrRelay.swift              WebSocket relay connection
|   +-- NostrRelayPool.swift          Multi-relay management
|   +-- NostrSigner.swift             Protocol: getPublicKey(), signHash()
|   +-- NIP44.swift                   NIP-44 encryption/decryption (for NIP-46 communication)
|
+-- Key Storage Layer (BUILT -- Phase 2 complete)
|   +-- SecureEnclaveKeyStore.swift   Encrypt/decrypt nsec via Secure Enclave
|   +-- KeyManager.swift              Key lifecycle (generate, import, delete)
|   +-- SoftwareSigner.swift          Implements NostrSigner using in-memory key + Face ID
|   +-- SchnorrSigner.swift           secp256k1 Schnorr signing (BIP-340)
|
+-- Card Communication Layer (DORMANT -- v2.0)
|   +-- SatochipCardService.swift     Satochip APDU commands
|   +-- CardSigner.swift              Implements NostrSigner via NFC card
|   +-- NFCSessionManager.swift       NFC session lifecycle (inherited from HushChip)
|   +-- SecureChannel.swift           ECDH + AES (inherited)
|   +-- PINManager.swift              Card PIN (inherited)
|
+-- Platform Layer
    +-- CoreNFC (dormant until v2.0)
    +-- Haptics
    +-- Biometrics (Face ID / Touch ID)
    +-- Persistent storage (connected apps, approval policies, event log)
```

### The Signer Protocol (Unchanged)

```swift
protocol NostrSigner {
    var isCardBacked: Bool { get }
    func getPublicKey() async throws -> Data          // 32-byte x-only pubkey
    func signHash(_ hash: Data) async throws -> Data  // 64-byte Schnorr signature
}

class SoftwareSigner: NostrSigner { ... }  // v1.0: Secure Enclave + Face ID
class CardSigner: NostrSigner { ... }      // v2.0: NFC card tap
class MockSigner: NostrSigner { ... }      // Dev only
```

---

## THE GOLDEN RULE (v1.0)

```
DO NOT MODIFY OR DELETE THE INHERITED APDU / NFC / CARD COMMUNICATION FILES.
They are DORMANT, not dead. They will be activated in v2.0.
Leave them in the project. Don't import them in v1.0 screens.
```

---

## SCREEN INVENTORY (v1.0 MVP -- VAULT + REMOTE SIGNER)

| # | Screen | Description |
|---|--------|-------------|
| 0 | Splash | "SIGNSTR" wordmark, loading dots |
| 1 | Onboarding (3 pages) | "Your Nostr identity. Secured." / "Connect your favourite clients. Never paste your nsec." / "Face ID approves every signature." |
| 2 | Key Setup | Two paths: "Create new identity" OR "Import existing nsec" |
| 3a | Import nsec | Text field for nsec1..., paste button, QR scanner |
| 3b | Generate Key | Generate button, shows new npub, confirms save |
| 3c | Back Up Your Key | Shown after generate OR import. See KEY BACKUP FLOW below. |
| 4 | Home / Connections (main tab) | List of connected apps (Damus, Primal, etc.) with status. "Add connection" button. Empty state: "Scan a Nostr Connect QR to get started." |
| 5 | Add Connection | QR scanner for nostrconnect:// URIs. Also paste field for bunker:// or nostrconnect:// strings. Shows app name, requested permissions. Approve/reject. |
| 6 | Signing Request | "Damus wants to sign:" + event preview (kind, content). Approve (Face ID) / Reject buttons. Timer countdown if auto-approve is active. |
| 7 | Identity tab | npub display (truncated + full), npub QR, copy button, bunker:// URI for sharing. "Go Air-Gapped" upsell card. |
| 8 | Event Log | History of signed events: timestamp, which app requested it, event kind, content preview, approved/rejected |
| 9 | App Settings (per connection) | Approval policy for this app: always ask, trust for session, trust for duration, trust by kind. Disconnect button. |
| 10 | Settings | Biometrics toggle, default approval policy, relay config for NIP-46 communication, delete key (danger), emergency export (danger), about |
| 11 | Go Air-Gapped | NostrKey card explanation, purchase link (signstr.com/card, GBP 14.99) |
| 12 | Emergency Export | Face ID + red warning screen. Reveals raw nsec for backup. See EMERGENCY ACCESS below. |
| 13 | About / Legal | Version, GPL-3.0 notice, credits, GitHub link |

### Tab Bar

3 tabs: **Connections** (link icon) / **Identity** (person icon) / **Settings** (gear icon)

---

## KEY BACKUP FLOW

Shown immediately after generating a new key OR importing an nsec. The user MUST acknowledge this before proceeding to the main app.

### v1.0 (Software Only)

```
Screen: "Back up your key"

Warning (red box):
  "Your Nostr identity depends on this key. If you lose this device
   and have no backup, your identity is gone forever. There is no
   recovery. No password reset. No support ticket."

Option A: "Copy nsec to password manager"
  --> Face ID to reveal nsec
  --> nsec displayed (masked, tap to reveal)
  --> Copy button (clipboard auto-clears after 30 seconds)
  --> "I've saved it somewhere safe" confirmation

Option B: "I'll do this later"
  --> Warning: "You can export your nsec from Settings at any time.
      But if you lose this device first, your identity is gone."
  --> "I understand the risk" button

Then --> proceed to Connections tab (main app)
```

### v2.0 (Card Support Added)

Same screen but with additional options:

```
Option B: "Write to NostrKey card" (if card detected)
  --> NFC session: imports nsec to NostrKey card
  --> nsec now lives on card AND on device
  --> User can later delete device copy from Settings
  --> "Your key is now on your NostrKey card."

Option C: "Write to HushChip card" (if card detected)
  --> NFC session: stores nsec on HushChip SeedKeeper card
  --> This is a cold backup -- stored in a drawer, not used daily
  --> "Your key is backed up on your HushChip card."

Note: HushChip stores secrets (SeedKeeper applet). NostrKey signs
events (Satochip applet). Ideally a serious user has both:
NostrKey for daily signing, HushChip locked away for recovery.
```

---

## EMERGENCY ACCESS

Available from Settings at any time. This is the "break glass" option.

```
Settings --> "Export nsec" (in danger zone)

Screen: "Emergency key export"

Red warning box:
  "This will display your raw private key. Anyone who sees this
   screen controls your Nostr identity. Only do this if you need
   to recover your key or move it to another device."

Face ID required to proceed.

Then:
  --> nsec displayed (nsec1... format), masked by default
  --> Tap to reveal
  --> Copy button (clipboard auto-clears 30 seconds)
  --> QR code of nsec (for scanning into another app)
  --> "Write to HushChip" button (v2.0, NFC backup)
  --> "Write to NostrKey" button (v2.0, NFC migration)

No confirmation needed to leave -- just navigate back.
```

The nsec is ALWAYS recoverable by the owner. Signstr is a vault, not a prison.

### Signing Request UX Flow (v1.0 -- Software)

```
Damus sends sign_event request via NIP-46
  --> Signstr receives via relay subscription
  --> ApprovalManager checks policy for this app
  
If policy = "always ask":
  --> Push notification: "Damus wants to sign a note"
  --> User opens Signstr (or it's already open)
  --> Signing Request screen shows event preview
  --> User taps Approve --> Face ID
  --> SoftwareSigner decrypts nsec, signs hash, zeros key
  --> Signstr sends signed event back via relay
  --> Damus publishes

If policy = "trust for duration" and within window:
  --> SoftwareSigner auto-signs (biometric cached)
  --> Signstr sends signature back immediately
  --> Entry added to Event Log
  --> User sees nothing (background operation)
```

### Signing Request UX Flow (v2.0 -- Card)

```
Same as above, but instead of Face ID:
  --> NFC sheet appears: "Tap your NostrKey card"
  --> User taps card to phone
  --> Card signs hash on secure element
  --> Signstr sends signature back via relay

If policy = "session tap":
  --> First request: tap card + enter PIN
  --> Subsequent requests within session: auto-approve (no tap needed)
  --> Session expires after configured duration
```

---

## NOSTR PROTOCOL REFERENCE

### NIP-01: Event Format

```json
{
  "id": "<32-byte lowercase hex SHA-256>",
  "pubkey": "<32-byte lowercase hex x-only pubkey>",
  "created_at": <unix timestamp seconds>,
  "kind": <integer>,
  "tags": [["tag", "value"], ...],
  "content": "<string>",
  "sig": "<64-byte lowercase hex Schnorr signature>"
}
```

### Event ID Computation

SHA-256 of UTF-8 encoded JSON array:
```
[0, <pubkey>, <created_at>, <kind>, <tags>, <content>]
```

This hash is both the event `id` AND the hash that gets signed (Schnorr, BIP-340).

### nsec / npub Bech32

```
nsec + <32-byte private key>  = nsec1...
npub + <32-byte x-only pubkey> = npub1...
```

Standard bech32 (NOT bech32m). Use a library.

### NIP-44 Encryption (Required for NIP-46)

NIP-46 messages are encrypted with NIP-44 (versioned, padded, authenticated encryption using secp256k1 ECDH + ChaCha20 + HMAC-SHA256). This replaces the older NIP-04 encryption.

Signstr must implement NIP-44 encrypt/decrypt to communicate with connected clients.

---

## SATOCHIP APDU COMMANDS (v2.0 REFERENCE -- DORMANT)

Documented in full in the previous spine version. Key commands:

- SELECT applet: AID `5361746F43686970`
- INS_VERIFY_PIN (0x22)
- INS_IMPORT_KEY (0x32) -- import nsec to keyslot
- INS_GET_PUBKEY (0x33) -- retrieve npub
- INS_SIGN_SCHNORR_HASH (0x74) -- sign event hash on card

Full signing flow: SELECT -> secure channel -> PIN -> sign hash -> return signature.

---

## DESIGN SYSTEM: GHOST

Identical to HushChip. Dark mode only. Outfit font. sg-prefixed colour tokens.

### Colour Palette

```swift
extension Color {
    static let sgBg          = Color(hex: "#09090b")
    static let sgBgRaised    = Color(hex: "#0e0e10")
    static let sgBgSurface   = Color(hex: "#111113")
    static let sgBorder       = Color(hex: "#1a1a1e")
    static let sgBorderHover  = Color(hex: "#28282e")
    static let sgTextGhost    = Color(hex: "#38383e")
    static let sgTextFaint    = Color(hex: "#5a5a64")
    static let sgTextMuted    = Color(hex: "#8a8a96")
    static let sgTextBody     = Color(hex: "#a8a8b4")
    static let sgTextBright   = Color(hex: "#cdcdd6")
    static let sgTextWhite    = Color(hex: "#e4e4ec")
    static let sgDanger       = Color(hex: "#c45555")
    static let sgDangerBorder = Color(hex: "#3d2020")
}
```

### Typography, Components, App Identity

Same as previous spine version. Outfit font, same weights, same component specs.

- **App name:** Signstr
- **Tab bar:** Connections (link icon) / Identity (person icon) / Settings (gear icon)
- **About:** "Signstr is a product of Gridmark Technologies Ltd"

---

## WHAT SIGNSTR IS NOT

- **Not a Nostr client.** Users do not compose posts, browse feeds, or read messages in Signstr.
- **Not a note composer.** There is no "write a post" screen. The compose screen from earlier development should be removed.
- **Not a relay browser.** Relay config exists only for NIP-46 communication, not for reading events.
- **Not a prison.** The nsec is always exportable by the owner (behind Face ID + warning). Signstr is a vault, not a lockbox. The user can always get their key back if they need to.

---

## DEVELOPMENT PHASES

### Phase 1: Nostr Logic Layer -- COMPLETE
NostrEvent, bech32, Schnorr signing, relay WebSocket, MockSigner. All tested.

### Phase 2: Key Storage + Software Signing -- COMPLETE
Secure Enclave encryption, KeyManager, SoftwareSigner with Face ID. All tested.

### Phase 3: NIP-46 Remote Signer (CURRENT PRIORITY)
The core feature. Build the bunker:
- NIP-44 encryption/decryption
- NIP-46 connection parser (nostrconnect:// and bunker:// URIs)
- NIP-46 service (listen for kind 24133 events on relay)
- Session manager (track connected clients)
- Message handler (parse JSON-RPC, dispatch to signer)
- Approval manager (prompt or auto-approve based on policy)
- Unit tests: mock client sends sign_event, Signstr signs and responds

### Phase 4: UI
Build all screens:
- Onboarding + key setup (PARTIALLY BUILT)
- Connections tab (list connected apps, add new connection via QR/paste)
- Signing request approval screen
- Identity tab (npub, QR, bunker URI, upsell)
- Event log
- Per-app settings (approval policies)
- Global settings

### Phase 5: Polish + App Store
- Push notifications for signing requests (when app is backgrounded)
- Error handling, edge cases
- Haptic feedback
- App Store submission

### Phase 6: Card Support (v2.0)
- Activate dormant NFC/APDU code
- CardSigner implements NostrSigner via NFC
- Key migration (device -> card)
- Card-specific approval policies (tap per sign, session tap, PIN per sign)

---

## LICENCE COMPLIANCE

```swift
// Copyright (c) 2026 Gridmark Technologies Ltd (Signstr)
// https://github.com/hushchip/Signstr-iOS
//
// Based on Seedkeeper-iOS by Toporin / Satochip S.R.L.
// https://github.com/Toporin/Seedkeeper-iOS
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
```

---

## CURRENT TASK

(Update this section at the start of each Claude Code session.)

**Current phase:** Phase 3 -- NIP-46 Remote Signer
**Current task:** Build NIP-44 encryption + NIP-46 service layer
**Files being modified:** New files in Signstr/NIP46/ directory
**Files NOT to touch:** All inherited APDU/NFC files (dormant), all Phase 1+2 Nostr files (complete)

---

*This document is the law. When in doubt, refer back here.*
