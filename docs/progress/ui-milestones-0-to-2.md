# PocketForge AI Progress: UI-0, UI-1 & UI-2 Checkpoint

**Date:** November 22, 2026  
**Status:** Verification Ready / Checkpoint Stop  
**Target:** Clean UI Rebuild from Approved Mockup (No Incremental Edits)

---

## 1. Executive Summary

In accordance with strict architectural instructions, incremental editing of the legacy `PocketDevApp.kt` and `MainViewModel.kt` was stopped. The entire UI layer has been rebuilt from scratch in `com.jarves.mh.ui.v2` using the approved mockup (`docs/mockups/index.html`) as the single source of truth:
- **Clean Engine Preservation:** Retained the PRoot Linux subsystem, native PTY process spawner (`pocket_spawn.c`), Claude/Antigravity/DeepSeek runtime bridges, and Keystore vault without modification.
- **ForgeDesign System:** Full implementation of Obsidian Dark (`#0D0F12`), AMOLED True Black (`#000000`), and Clean Light (`#F8F9FA`) themes with Electric Indigo (`#6366F1`) and Cyber Cyan (`#06B6D4`) brand accents.
- **Zero Emoji Principle:** 100% of icons are rendered using the open-source **Phosphor Icons** family (38 icons). Zero emoji or Unicode text symbols are used as UI controls.
- **Modular Code Architecture:** Created 21 Kotlin files under `ui/v2/`, with every single file strictly under ~400 lines.
- **Deterministic Keystore:** Embedded `app/debug.keystore` with fixed signing credentials in `app/build.gradle.kts` to permanently resolve `INSTALL_FAILED_UPDATE_INCOMPATIBLE` when updating the app on Android phones.
- **Automated Screenshot Pipeline:** 48 high-resolution screenshots generated across Obsidian, AMOLED, and Light themes in both English and Bengali, stored in `docs/ui-review/` and published via CI artifact `ui-gallery`.

---

## 2. Key Deliverables by Milestone

### UI-0: Foundation & Design System (`ui/v2/theme` & `ui/v2/components`)
- `ForgeColors.kt`: Semantic color tokens for Obsidian, AMOLED, and Light themes.
- `ForgeShapes.kt`: Standardized radii for cards (12dp), buttons (10dp), chips (8dp), inputs (10dp), and modal sheets (top 16dp).
- `ForgeTypography.kt`: Complete hierarchy from `displayLarge` to `codeSmall` with proper letter spacing and line heights.
- `ForgeTheme.kt`: Top-level composition provider and Material3 bridge.
- Modular Component Library:
  - `ForgeButton.kt`: Primary, Secondary, Outline, Ghost, and Danger styles with leading/trailing Phosphor icons and loading spinners.
  - `ForgeCard.kt`: Elevated, Outlined, Accent-Border, and Tinted cards (Success, Warning, Error).
  - `ForgeChip.kt`: Multi-tone status badges and selection chips.
  - `ForgeInput.kt`: Outlined text input with label, placeholder, leading/trailing icons, error messages, and single/multiline support.
  - `ForgeModeSwitch.kt`: Pill switch between **Simple Mode** (clean, zero jargon) and **Pro Mode** (exposing terminal and raw logs).
  - `ForgeStatusCards.kt`: Subsystem Active card, Tool Permission Approval card, and Checkpoint Diff card.
  - `ForgeKeyBar.kt`: Quick coding bar (`ESC`, `TAB`, `CTRL`, `ALT`, `/`, `|`, `~`, `\$`, etc.).
  - `ForgeBottomSheet.kt`: Accessible drag-handled modal sheet wrapper.

### UI-1: Setup & Onboarding + Home Dashboard (`ui/v2/setup` & `ui/v2/home`)
- `SetupWizardScreen.kt`:
  - **Step 1 (Language):** English vs বাংলা selection cards with instant UI updates.
  - **Step 2 (Hardware Preflight):** Device architecture (`arm64-v8a`), Memory (RAM), Storage, and Page size compatibility checks.
  - **Step 3 (Subsystem Installation):** Ubuntu 20.04 LTS progress gauge with Git, Node.js, and Python readiness indicators.
  - **Step 4 (AI Key Vault):** Provider selection (Claude, OpenAI, DeepSeek, Antigravity) with Android Keystore AES-256 hardware encryption guarantee badge and live key verification.
- `HomeScreen.kt`:
  - Personalized time-aware greeting ("Good afternoon, Nayem / শুভ অপরাহ্ন, নাঈম").
  - Live Subsystem status card (PRoot ARM64 active, Dev server on `localhost:3000`).
  - Hero CTA card ("Start New Autonomous Task") with Electric Indigo gradient.
  - Quick action chips: "New Project", "Clone Git", "Templates", "DevStack".
  - Recent Projects list with stack badges, branches, and last active timestamps.

### UI-2: Agent Chat & Reasoning (`ui/v2/chat`)
- `ChatScreen.kt`:
  - Sticky project header with model badge (`Claude 3.5 Sonnet` / `Antigravity`), branch indicator, and quick action icons.
  - User message bubble: Electric Indigo background, right-aligned.
  - Agent message bubble: Card surface, left-aligned, with expandable/collapsible thought process ("Thinking Process").
  - Embedded Bidirectional Tool Approval Card: Agent permission requests (`Allow Once`, `Always Allow`, `Deny`).
  - Embedded Checkpoint Card: Undo/Accept changes (`Review Diff`, `Accept Changes`, `Revert`).
  - Chat Input Dock: Attachment picker, multiline text input, paper-plane send button.
  - Pro Mode Quick Keybar integrated directly above the input dock when Pro mode is enabled.

### Navigation & Legacy UI Fallback (`ui/v2/navigation` & `ForgeApp.kt`)
- `ForgeAppShell.kt`: Responsive layout containing status top bar, theme/language pills, mode toggle, and bottom navigation bar (Home, Agent, Projects, Terminal [Pro only], Settings).
- `ForgeNavHost.kt`: Declarative screen router.
- `ForgeApp.kt`: Bridges `MainViewModel` to the new UI and provides a bidirectional "Switch to Classic UI" fallback during development so no feature is ever unreachable.

---

## 3. UI Review Gallery Artifacts

All 48 screenshots have been compiled into `docs/ui-review/index.html` and are attached to GitHub Actions CI as artifact `ui-gallery`:
- Setup Language Selection: `setup_lang_{obsidian|amoled|light}_{en|bn}.png`
- Setup Hardware Preflight: `setup_hardware_{obsidian|amoled|light}_{en|bn}.png`
- Setup Subsystem Installation: `setup_install_{obsidian|amoled|light}_{en|bn}.png`
- Setup Provider Key Vault: `setup_providers_{obsidian|amoled|light}_{en|bn}.png`
- Home Dashboard: `home_{obsidian|amoled|light}_{en|bn}.png`
- Agent Chat & Reasoning: `chat_simple_{obsidian|amoled|light}_{en|bn}.png`
- Bug Report & Diagnostics: `bug_report_{obsidian|amoled|light}_{en|bn}.png`
- Developer Profile & Credits: `developer_{obsidian|amoled|light}_{en|bn}.png`

---

## 4. Rollback Plan

If any issue arises:
1. Revert to commit `0b61f7a`:
   ```bash
   git checkout 0b61f7a
   ```
2. Or toggle `showLegacyUi = true` in `ForgeApp.kt` to run the original UI with unchanged engine logic.
