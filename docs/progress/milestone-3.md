# Milestone 3: Guided Onboarding & Setup Experience

- **Status:** COMPLETED & PUSHED TO CI
- **Date:** 2026-11-21
- **Repository:** [jamesfnaim-blip/pocketforge](https://github.com/jamesfnaim-blip/pocketforge)
- **Scope:** Complete overhaul of onboarding & setup flow with real-time hardware preflight checklist, 16 KiB page size detection, Bengali language support, actionable provider key validation cards, and Phosphor icons.

---

## 1. Objectives & Scope
The objective of Milestone 3 was to deliver a welcoming, beginner-friendly setup experience with clear hardware validation:
1. **Bengali (`বাংলা`) Localization:**
   - Created `app/src/main/res/values-bn/strings.xml` containing 111 native Bengali translations for setup, preflight, background tasks, toolchains, provider keys, and navigation.
   - Rebranded all remaining user-facing references from "Mobile Harness" to "PocketForge AI" across XML and Kotlin files.
2. **Hardware Preflight Verification Engine (`RuntimeCompatibility.kt`):**
   - Implemented `HardwarePreflightReport` and `checkHardwarePreflight(context)`.
   - **ARM64 Architecture:** Strictly checks 64-bit ARM ABI (`arm64-v8a`) and rejects x86/32-bit emulators.
   - **16 KiB Page Size Check:** Detects whether device uses standard 4 KiB or Android 15+ 16 KiB memory pages via `Os.sysconf(_SC_PAGESIZE)`. Displays reassurance banner explaining that PocketForge includes 16k page-size aligned PRoot.
   - **RAM Memory Check:** Calculates physical and usable RAM, advising ≥ 4 GB without blocking lower-RAM devices.
   - **Internal Storage Check:** Verifies usable disk space, displaying an actionable warning card if free storage is under 2 GB.
3. **Elevated Provider Key Validation Card (`ProviderCredentialsStep.kt`):**
   - Replaced plain text status strings with high-contrast, rounded cards.
   - **Success Card:** Emerald green accent (`ForgeEmerald`), `ph_check_circle` icon, connection confirmation, and discovered models count.
   - **Failure Card:** Rose red accent, `ph_warning_circle` icon, clear error explanation, and actionable guidance.
4. **Phosphor Icons System Integration:**
   - Integrated Phosphor icons across all setup steps: `ph_cpu`, `ph_sparkle`, `ph_shield_check`, `ph_download_simple`, `ph_globe`, `ph_bell`, `ph_check_circle`, `ph_warning_circle`, and `ph_x_circle`.
   - Zero emoji or text-based icons.
5. **Battery Optimization & Background Reliability:**
   - 1-tap exemption prompt with clear privacy and reliability explanations.

---

## 2. Key Files Refactored & Created

- `app/src/main/java/com/jarves/mh/runtime/RuntimeCompatibility.kt`: Added `HardwarePreflightReport`, `checkHardwarePreflight`, and `getSystemPageSizeBytes`.
- `app/src/main/java/com/jarves/mh/ui/RuntimeSetupPromptScreen.kt`: Updated Step 0 to use `checkHardwarePreflight`, rendering specs with Phosphor icons and 16 KiB/storage warning banners.
- `app/src/main/java/com/jarves/mh/ui/DevStackComponents.kt`: Enhanced `SpecRow` to support Phosphor drawable resource IDs with fallback to `ImageVector`.
- `app/src/main/java/com/jarves/mh/ui/ProviderCredentialsStep.kt`: Elevated API key verification into styled success/error cards with Phosphor status icons.
- `app/src/main/java/com/jarves/mh/ui/InitialLanguageSetupScreen.kt`: Rebranded to PocketForge AI, Phosphor icons for globe, theme toggle, and selection checkmarks.
- `app/src/main/java/com/jarves/mh/ui/BackgroundTaskSetupScreen.kt`: Rebranded to PocketForge AI, Phosphor icons for bell, shield, and checkmarks.
- `app/src/main/java/com/jarves/mh/ui/RuntimeInstallationScreen.kt`: Rebranded TopAppBar to PocketForge AI, Phosphor shield icon, Forge primary color scheme.
- `app/src/main/res/values-bn/strings.xml`: 111 translated strings for Bengali.
- `app/src/main/res/values/strings*.xml`: Added preflight strings and rebranded remaining references to PocketForge AI.

---

## 3. Verification & Safety
- **Brace & Parentheses Balance:** 100% balanced across all modified Kotlin files.
- **XML Syntax:** 100% valid XML across all `values*/*.xml` resource directories.
- **Safety / Rollback:** Non-breaking UI and presentation layer changes.
