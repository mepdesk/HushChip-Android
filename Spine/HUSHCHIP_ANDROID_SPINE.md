# HUSHCHIP ANDROID APP — CLAUDE CODE SPINE DOCUMENT

**INCLUDE THIS DOCUMENT AT THE START OF EVERY CLAUDE CODE SESSION.**

This is the single source of truth for the HushChip Android app project. It tells you what this project is, what you can change, what you MUST NOT change, and the design system to follow.

---

## WHAT IS THIS PROJECT

HushChip is a rebranded fork of the open-source Seedkeeper-Android app (github.com/Toporin/Seedkeeper-Android). It is a companion app for a physical NFC smart card that stores secrets (seed phrases, passwords, etc.) on a PIN-protected secure element.

The app communicates with the card via NFC using APDU commands. The card runs an unmodified JavaCard applet. The app is a UI wrapper around the card's APDU protocol.

**Original repo:** github.com/Toporin/Seedkeeper-Android (Kotlin, AGPLv3)
**Our fork:** github.com/YOUR-ACCOUNT/HushChip-Android
**Local path:** ~/HushChip-Android
**Licence:** AGPLv3 (must remain AGPLv3, source must be public)

---

## THE GOLDEN RULE

```
┌─────────────────────────────────────────────────┐
│                                                 │
│   DO NOT MODIFY THE APDU / NFC / CARD           │
│   COMMUNICATION LAYER.                          │
│                                                 │
│   If a file sends bytes to the card or          │
│   parses bytes from the card, DO NOT TOUCH IT.  │
│                                                 │
│   If you are unsure whether something is part   │
│   of the APDU layer, ASK before changing it.    │
│                                                 │
└─────────────────────────────────────────────────┘
```

### CRITICAL: cardSelect("seedkeeper") — DO NOT CHANGE

The string `"seedkeeper"` in `cardSelect("seedkeeper")` is the **applet identifier on the physical card**. It appears 11 times in `NFCCardService.kt`. This is NOT branding — it is a protocol constant. Changing it will break all card communication.

### CRITICAL: JAR Library Imports — CANNOT CHANGE

All `import org.satochip.client.*` and `import org.satochip.io.*` statements reference classes inside pre-compiled JAR files. These package names are baked into the JARs. Do not attempt to rename or refactor them.

### What Counts as the APDU / Card Communication Layer

Any code that does ANY of the following is OFF LIMITS:

- Builds or sends APDU commands via `SatochipCommandSet` (`cmdSet.*` calls)
- Parses APDU responses from the card
- Manages the NFC reader mode lifecycle (`enableReaderMode` / `disableReaderMode`)
- Implements `CardListener` interface (`onConnected` / `onDisconnected`)
- Handles the secure channel (ECDH key exchange, encrypted backup protocol)
- Handles BIP39 wordlist validation or mnemonic-to-seed conversion
- Handles PIN verification protocol with the card (`cardVerifyPIN`, `setPin0`, `cardChangePin`)
- Manages card authentication (authentikey, PKI certificates)

### OFF-LIMITS Files (Do Not Modify Logic)

| File | Why |
|------|-----|
| `services/NFCCardService.kt` | All APDU commands and card state management (1057 lines). UI string changes OK, logic changes NOT OK. |
| `services/SeedkeeperCardListener.kt` | NFC `CardListener` callback implementation |
| `parsers/SecretDataParser.kt` | Parses raw card bytes into `SecretData` objects |
| `data/SecretData.kt` | Contains `getSecretBytes()` serialiser for card protocol |
| `data/NfcActionType.kt` | Enum of NFC action types used by service |
| `data/NfcResultCode.kt` | Enum of NFC result codes (UI strings can change, enum values cannot) |
| `libs/satochip-lib-0.2.3.jar` | Card command set library |
| `libs/satochip-android-0.0.2.jar` | Android NFC bridge library |

### What You CAN Freely Change

- All Composable functions (screens, components, layouts)
- All colour definitions (`Color.kt`, `colors.xml`)
- All string resources (`strings.xml`, hardcoded strings in composables)
- All theme definitions (`Theme.kt`, `Type.kt`, `themes.xml`)
- All drawable/mipmap assets (icons, backgrounds, illustrations)
- All font files in `res/font/`
- Navigation routes and screen flow (`Navigation.kt`)
- `AndroidManifest.xml` (app name, theme refs, package metadata)
- `build.gradle.kts` (applicationId, namespace, version, dependencies)
- `SharedViewModel.kt` (presentation logic, NOT card communication calls)
- `MainActivity.kt` (theme setup, orientation, Compose host)
- Adding entirely new screens that only call existing card functions
- Adding app-side-only features (clipboard timer, haptics, biometrics, local storage)

### The Architecture

```
HushChip Android App
│
├── UI Layer ← CHANGE EVERYTHING HERE
│   ├── ui/views/          (25 screen composables)
│   ├── ui/components/     (53 component composables)
│   ├── ui/theme/          (Color.kt, Theme.kt, Type.kt)
│   ├── res/drawable/      (56 assets)
│   ├── res/font/          (font files)
│   ├── res/values/        (strings.xml, colors.xml, themes.xml)
│   └── Navigation.kt     (route definitions)
│
├── ViewModel Layer ← CHANGE CAREFULLY
│   ├── SharedViewModel.kt    (bridges NFC service to Compose)
│   └── data/*.kt              (enums, data classes — UI strings OK)
│
├── APDU / NFC Layer ← DO NOT CHANGE
│   ├── services/NFCCardService.kt    (all card operations)
│   ├── services/SeedkeeperCardListener.kt
│   ├── parsers/SecretDataParser.kt
│   ├── data/SecretData.kt            (card serialisation)
│   └── libs/*.jar                    (Satochip libraries)
│
└── Utils ← CHANGE CAREFULLY
    ├── utils/Extensions.kt
    ├── utils/Utils.kt
    └── utils/ActivityIntents.kt
```

---

## IDENTIFYING APDU LAYER CODE

When exploring files, look for these patterns:

**APDU layer indicators (DO NOT MODIFY):**
- Any reference to `cmdSet.*` (SatochipCommandSet method calls)
- `cardSelect()`, `cardGetStatus()`, `cardVerifyPIN()`, `seedkeeperListSecretHeaders()`
- `seedkeeperImportSecret()`, `seedkeeperExportSecret()`, `seedkeeperResetSecret()`
- `NFCCardManager`, `CardChannel`, `CardListener`, `APDUException`
- `WrongPINException`, `BlockedPINException`, `ResetToFactoryException`
- Any byte array construction or parsing for card communication
- The string `cardSelect("seedkeeper")` — this is the applet AID, NOT branding

**UI layer indicators (SAFE TO MODIFY):**
- `@Composable` functions
- Layout composables: `Column`, `Row`, `Box`, `LazyColumn`, `Scaffold`
- `Color()`, `MaterialTheme`, font references
- `stringResource()`, `painterResource()`
- Navigation: `NavHost`, `NavController`, route objects
- `Modifier.*` chains

**When in doubt:** If it constructs byte arrays or calls `cmdSet.*`, leave it alone. If it shows things on screen or handles user input, it is safe to change.

---

## CODEBASE MAP (117 .kt FILES)

### Root (3 files)
| File | Description | Safe? |
|------|-------------|-------|
| `MainActivity.kt` | Launcher activity, Compose host, portrait lock | ✅ Change |
| `Navigation.kt` | NavHost with 18 routes, NFC result observers | ✅ Change |
| `WebviewActivity.kt` | External URL viewer | ✅ Remove/Replace |

### data/ (19 files)
| File | Safe? | Notes |
|------|-------|-------|
| `NfcActionType.kt` | ⚠️ Enum values NO, display strings YES |
| `NfcResultCode.kt` | ⚠️ Enum values NO, UI strings/images YES |
| `SecretData.kt` | ❌ Contains `getSecretBytes()` card serialiser |
| `PinCodeAction.kt` | ✅ UI enum only |
| `BackupStatus.kt` | ✅ UI enum only |
| `GenerateStatus.kt` | ✅ UI enum only |
| All others | ✅ UI enums and data classes |

### services/ (3 files) — ❌ OFF LIMITS (logic)
| File | Notes |
|------|-------|
| `NFCCardService.kt` | 1057 lines. ALL card ops. UI strings inside can change. |
| `SeedkeeperCardListener.kt` | CardListener implementation |
| `SatoLog.kt` | Debug logging — safe to modify |

### parsers/ (1 file) — ❌ OFF LIMITS
| File | Notes |
|------|-------|
| `SecretDataParser.kt` | Card byte parsing |

### viewmodels/ (1 file) — ⚠️ CAREFUL
| File | Notes |
|------|-------|
| `SharedViewModel.kt` | Bridge to Compose. Presentation logic OK. Card calls delegate to NFCCardService. |

### ui/views/ (25 screen files) — ✅ ALL SAFE TO CHANGE
### ui/components/ (53 component files) — ✅ ALL SAFE TO CHANGE
### ui/theme/ (3 files) — ✅ ALL SAFE TO CHANGE
### utils/ (5 files) — ✅ ALL SAFE TO CHANGE

---

## DESIGN SYSTEM: GHOST

The app uses the "Ghost" aesthetic. **Dark mode only. No light mode.** Greyscale with very slight cool blue-grey undertone. No accent colour except gold on app icon.

### Colour Palette

```kotlin
// HushChip Ghost Palette
object HushColors {
    val bg            = Color(0xFF09090B) // App background
    val bgRaised      = Color(0xFF0E0E10) // Cards, list items
    val bgSurface     = Color(0xFF111113) // Card interiors, input backgrounds
    val border        = Color(0xFF1A1A1E) // Default borders, dividers
    val borderHover   = Color(0xFF28282E) // Active borders, selected states
    val textGhost     = Color(0xFF38383E) // Section labels, placeholders
    val textFaint     = Color(0xFF5A5A64) // Secondary text, descriptions
    val textMuted     = Color(0xFF8A8A96) // Nav titles, body text
    val textBody      = Color(0xFFA8A8B4) // Secret labels, primary content
    val textBright    = Color(0xFFCDCDD6) // Headings, emphasis
    val textWhite     = Color(0xFFE4E4EC) // Maximum emphasis (rare)
    val danger        = Color(0xFFC45555) // Wrong PIN, delete, warnings
    val dangerBorder  = Color(0xFF3D2020) // Warning box borders
    val dangerBg      = Color(0x26C45555) // Warning box background (~15% opacity)
}
```

### Typography

**Font:** Outfit (bundled in `res/font/`, replacing Open Sans)
**Weights:** ExtraLight (200), Light (300), Regular (400), Medium (500)

```kotlin
// HushChip Typography
object HushType {
    private val outfitFamily = FontFamily(
        Font(R.font.outfit_extralight, FontWeight.ExtraLight),   // 200
        Font(R.font.outfit_light, FontWeight.Light),             // 300
        Font(R.font.outfit_regular, FontWeight.Normal),          // 400
        Font(R.font.outfit_medium, FontWeight.Medium),           // 500
    )

    val navTitle     = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 5.sp)    // uppercase
    val navButton    = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 2.sp)    // uppercase
    val sectionLabel = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 9.sp, letterSpacing = 3.sp)     // uppercase
    val cardTitle    = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp)
    val body         = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Light, fontSize = 12.sp)
    val heading      = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Light, fontSize = 16.sp, letterSpacing = 0.5.sp)
    val input        = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Light, fontSize = 13.sp)
    val button       = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 4.sp)    // uppercase
    val tag          = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 9.sp, letterSpacing = 1.sp)     // uppercase
}
```

**Usage by element:**

| Element | Weight | Size | Letter Spacing | Transform |
|---------|--------|------|---------------|-----------|
| Nav title | 400 | 11sp | 5sp | uppercase |
| Nav button | 400 | 10sp | 2sp | uppercase |
| Section label | 400 | 9sp | 3sp | uppercase |
| Card title / secret name | 400 | 12sp | 0 | none |
| Body text | 300 | 11-12sp | 0 | none |
| Onboarding heading | 300 | 16sp | 0.5sp | none |
| Input text | 300 | 13sp | 0 | none |
| Button text | 400 | 11sp | 4sp | uppercase |
| Tab bar label | 400 | 8sp | 1sp | uppercase |
| Tag badge | 400 | 9sp | 1sp | uppercase |
| Counter text | 400 | 9sp | 1-2sp | uppercase |
| Monospace (fingerprints) | FontFamily.Monospace | 10sp | 0 | none |

### Component Specs

**Cards:** bg `bgRaised`, border 1dp `border`, corner radius 12dp, padding 16dp
**Buttons (primary):** bg `border`, border `borderHover`, text `textBright`, corner radius 10dp, padding 14dp, full width, uppercase, 4sp letter-spacing
**Buttons (secondary):** bg transparent, border `borderHover`, text `textBody`
**Buttons (danger):** bg transparent, border `dangerBorder`, text `danger`
**Input fields:** bg `0xFF0C0C0E`, border `border`, corner radius 8dp, text `textBright`, placeholder `textGhost`
**Warning boxes:** border `dangerBorder`, bg `dangerBg`, text `danger`, corner radius 8dp

### Navigation

**Top nav bar:** Sticky, background `bg` with 85% alpha blur, border-bottom `border` at 60% alpha, ~62dp height
**Bottom nav (if used):** 3 tabs — Card, Generate, Settings. Active icon `textMuted`. Inactive opacity 0.35.

---

## WHAT TO REMOVE FROM THE SEEDKEEPER APP

1. **Card authenticity check** — HushChip cards will FAIL this (no Satochip PKI certificate). Remove the feature entirely from CardInformation and any automatic check on card connection. Remove `CardAuthenticity` screen.
2. **All "SeedKeeper" text** — Replace with "HushChip" everywhere in user-facing strings.
3. **All "Satochip" text** — Remove from user-facing UI. Keep ONLY in About/Legal screen as attribution.
4. **All Satochip links** — Remove all 12 URLs pointing to satochip.io. Replace with HushChip equivalents.
5. **"Buy your SeedKeeper" prompts** — Remove all purchase CTAs for Satochip products (noSeedkeeper string, buySeedkeeper, buySeedkeeperUrl, oldSeedkeeper).
6. **French language** — Remove `values-fr/strings.xml` and `assets/password-replacement-fr.txt` for MVP. English only.
7. **Debug mode in main settings** — Move to hidden gesture (tap version number 7 times).
8. **Firebase references** — Already removed from gradle plugins. Also clean up `libs.versions.toml` entries and `google-services sample.json`.
9. **WebviewActivity** — Replace with browser intent (`Intent.ACTION_VIEW`). No in-app webview needed.
10. **Dead JAR files** — Remove 5 unused JARs from `app/libs/`: `satochip-lib-0.0.4.jar`, `0.1.0.jar`, `0.2.0.jar`, `0.2.1.jar`, `0.2.2.jar`.
11. **Unused font files** — Remove 9 unused Open Sans variants. Replace all 12 with 4 Outfit files.
12. **Satochip background images** — Remove all 5 `seedkeeper_background*.png` files and `ic_sato_small.xml`.
13. **Purple colour theme** — Replace all 21 `Sato*` colour values with Ghost palette.
14. **Dynamic colour (Material You)** — Remove. Force Ghost dark theme always.

## WHAT TO ADD

1. **HushChip branding** — Ghost palette, Outfit font, app icon with gold EMV chip
2. **3-screen onboarding** — "Your secrets. On a chip." / "Tap. Store. Done." / "Your PIN is everything."
3. **Prominent PIN warning** — Red warning box during setup requiring user confirmation
4. **Clipboard auto-clear** — 30 second timer after copying secrets, with toast notification
5. **Haptic feedback** — On NFC detect, PIN entry, success, error
6. **About/Legal screen** — AGPLv3 notice, credit to Toporin/Satochip, link to github.com/hushchip
7. **Better error messages** — Human-readable NFC errors, PIN attempt warnings
8. **Card health screen** — Memory donut chart + stats (secrets count, memory, PIN tries, applet version)
9. **`<uses-feature android:name="android.hardware.nfc" android:required="true" />`** — Add to manifest

### Haptic Feedback Map

| Action | Android Implementation |
|--------|----------------------|
| Card detected (NFC) | `VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)` |
| PIN dot entered | `VibrationEffect.createOneShot(20, 80)` |
| Wrong PIN | `VibrationEffect.createOneShot(200, 255)` |
| PIN accepted | `VibrationEffect.createOneShot(50, 128)` |
| Secret saved | `VibrationEffect.createOneShot(50, 128)` |
| Secret copied | `VibrationEffect.createOneShot(20, 80)` |
| Delete confirm | `VibrationEffect.createOneShot(100, 200)` |
| Factory reset | `VibrationEffect.createOneShot(300, 255)` |

---

## APP IDENTITY

- **App name:** HushChip
- **Application ID:** `uk.co.hushchip.app`
- **Namespace:** `uk.co.hushchip.app` (replaces `org.satochip.seedkeeper`)
- **App icon:** Gold EMV chip centred on dark card surface, "HUSH" embossed in centre contact pad
- **Splash screen:** Gold chip + "HUSH" + "HUSHCHIP" wordmark + pulsing dots
- **About:** "HushChip is a trading name of Gridmark Technologies Ltd"

---

## SECRET TYPE ICONS

Text-based icons in rounded squares (28dp, bg `bgSurface`, border `border`, radius 6dp):

| Type | Icon Text | Colour |
|------|-----------|--------|
| BIP39 Mnemonic | Aa | `textFaint` |
| Password | ● (filled circle) | `textFaint` |
| Wallet Descriptor | { } | `textFaint` |
| Free Text | T | `textFaint` |
| 2FA Secret | 2F | `textFaint` |
| Master Seed | S | `textFaint` |

---

## BRANDING AUDIT SUMMARY

### Must Rename (UI-facing)
- `strings.xml`: ~30 strings mentioning "Seedkeeper" → "HushChip"
- `strings.xml`: ~4 strings mentioning "Satochip S.R.L." → Remove or move to About
- `strings.xml`: 12 URLs pointing to `satochip.io` → HushChip URLs
- `themes.xml`: `Theme.Seedkeeper` → `Theme.HushChip`
- `AndroidManifest.xml`: 3 references to `Theme.Seedkeeper`
- `build.gradle.kts`: `applicationId` and `namespace`
- `settings.gradle.kts`: project name
- Drawable filenames: 5 `seedkeeper_background*.png`, 1 `ic_sato_small.xml`
- Mipmap launcher icons: 22 files across 6 density buckets

### Must Rename (Code-internal, cosmetic)
- 21 `Sato*`-prefixed colour names in `Color.kt`
- ~10 `Sato*`-prefixed component function names
- `SeedkeeperTheme()` composable function name
- `SeedkeeperPreferences` enum name

### DO NOT RENAME
- `cardSelect("seedkeeper")` — applet identifier (11 occurrences in NFCCardService.kt)
- `import org.satochip.*` — JAR library package names
- `getSharedPreferences("seedkeeper")` — only rename with migration code
- Enum values in `NfcActionType`, `NfcResultCode` — internal identifiers

### Package Rename Strategy
The Java/Kotlin package `org.satochip.seedkeeper` needs renaming to `uk.co.hushchip.app`. This requires:
1. Update `namespace` and `applicationId` in `build.gradle.kts`
2. Move all files from `java/org/satochip/seedkeeper/` to `java/uk/co/hushchip/app/`
3. Update every `package` declaration in every .kt file (~90 files)
4. Update every internal import across all files
5. DO NOT touch `import org.satochip.client.*` or `import org.satochip.io.*` (JAR imports)
6. Update `AndroidManifest.xml` activity references
7. Update `settings.gradle.kts` project name

**Recommendation:** Do this as a single dedicated step using Android Studio's refactor tool or a batch sed command. Verify build immediately after.

---

## NFC IMPLEMENTATION NOTES

- **Mode:** Reader Mode (not Tag Dispatch)
- **Flags:** `NfcAdapter.FLAG_READER_NFC_A or FLAG_READER_NFC_B or FLAG_READER_SKIP_NDEF_CHECK`
- **NFC is passive on Android** — no user-triggered scan button needed. Card is detected automatically when app is in foreground.
- **The NFC "scan" dialog** in the current app is a prompt telling the user to hold the card. Android does not have iOS's system NFC sheet.
- **Manifest:** Only `<uses-permission android:name="android.permission.NFC" />`. No intent filters or tech-list. Add `<uses-feature>` for NFC.

---

## CURRENT SDK & BUILD VERSIONS

| Setting | Current Value | Target |
|---------|--------------|--------|
| minSdk | 24 (Android 7.0) | Keep |
| targetSdk | 34 (Android 14) | Keep or bump to 35 |
| compileSdk | 34 | Keep or bump to 35 |
| Kotlin | 1.9.0 | Keep |
| Compose BOM | 2024.05.00 | Keep |
| AGP | 8.13.2 | Keep |

---

## DISTRIBUTION

### Google Play
- Application ID: `uk.co.hushchip.app`
- Signed AAB (Android App Bundle)
- $25 one-time developer fee
- Standard review process

### Zapstore (Bitcoin community)
- Signed APK (not AAB)
- Requires Nostr keypair (brand key, not personal)
- Pubkey needs whitelisting by Zapstore team
- Config: `zapstore.yaml` with identifier, name, repo, icon, licence
- CLI or web publisher at publisher.zapstore.dev

---

## LICENCE COMPLIANCE

Every new or substantially modified Kotlin file must include this header:

```kotlin
// Copyright (c) 2026 Gridmark Technologies Ltd (HushChip)
// https://github.com/hushchip/HushChip-Android
//
// Based on Seedkeeper-Android by Toporin / Satochip S.R.L.
// https://github.com/Toporin/Seedkeeper-Android
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
```

Do NOT remove or modify existing copyright headers in files from the original repo. Add the HushChip header above the original one.

**Note:** Licence is AGPLv3 (not GPL-3.0 like iOS). The AGPL additionally requires that users interacting over a network can access the source. Since this is a local app, the practical difference is minimal, but use the correct licence text.

---

## SCREEN INVENTORY (MVP)

| # | Screen | Description |
|---|--------|-------------|
| 0 | Splash | Gold chip, "HUSH", wordmark, loading dots. Glisten on first launch. |
| 1 | Onboarding (3 pages) | Swipeable. "Your secrets. On a chip." / "Tap. Store. Done." / "Your PIN is everything." |
| 2 | Home: Tap Your Card | NFC ripple pulse, "Tap your card" text |
| 3a | PIN Entry: Unlock | Lock icon, dot row, tries counter, system keyboard |
| 3b | PIN Entry: Wrong | Red state, shake dots, warning at <=3 tries |
| 3c | PIN Entry: Setup | New PIN + confirm + card name + red warning box |
| 4 | Secret List | Search, health bar, secret rows with type icons |
| 5 | Secret Detail | Metadata card, reveal/QR/copy buttons |
| 6 | Secret Revealed | Word grid (mnemonic) or monospace (password), red warning, auto-hide 60s |
| 7a | Add Secret: Type Selector | Secret type cards |
| 7b | Add Secret: Mnemonic | Word grid, BIP39 autocomplete, export rights toggle |
| 7c | Add Secret: Password | Label + password + login + URL |
| 7d | Add Secret: Free Text | Label + multiline text |
| 8a | Generate: Password | Display, length slider, character toggles |
| 8b | Generate: Mnemonic | 12/24 toggle, word grid |
| 9 | Backup Wizard | Multi-step progress, NFC prompt, per-secret progress |
| 10 | Settings | Grouped rows, danger zone with factory reset |
| 11 | Card Health | Memory donut, stats table |
| 12 | About / Legal | Logo, version, licence, credits, source link |

---

## CURRENT TASK

(Update this section at the start of each Claude Code session to reflect the current task.)

**Current phase:** Step 1 — Codebase Reconnaissance (COMPLETE)
**Current task:** [NEXT STEP]
**Files being modified:** [LIST]
**Files NOT to touch:** [LIST]

---

*This document is the law. When in doubt, refer back here.*
