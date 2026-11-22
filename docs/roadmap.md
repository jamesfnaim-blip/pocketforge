# PocketForge AI Implementation Roadmap: Complete UI Rebuild & Engine Evolution

This roadmap defines the complete, uncompromising overhaul of **PocketForge AI** from the approved mockup (`docs/mockups/index.html`). 

**Core Strategy:**
1. **Freeze Legacy UI:** Discontinue all incremental retouching of the legacy `ui/PocketDevApp.kt` monolith and `MainViewModel.kt`.
2. **Keep Engine Intact:** Retain the working Linux/PRoot subsystem, native PTY spawn (`pocket_spawn.c`), rootfs installer, agent bridges (`ClaudeRuntimeBridge`, `AntigravityRuntimeBridge`, `DshRuntimeBridge`), Keystore vault, and data layer.
3. **Rebuild New UI Layer in `ui/v2`:** Rebuild the entire UI layer in package `com.jarves.mh.ui.v2` directly from the approved mockup with ForgeDesign theme, single-responsibility composables (< 400 lines each), modular feature ViewModels, and real Simple vs. Pro modes.
4. **Automated UI Review Gallery (`ui-gallery`):** Render every screen and state to PNGs via JVM screenshot testing in Obsidian, AMOLED, and Light modes across English and Bengali, uploaded as a CI artifact for review before code lands on a phone.

---

```
+─────────────────────────────────────────────────────────────────────────────+
|               POCKETFORGE AI UI REBUILD & EVOLUTION ROADMAP                 |
+─────────────────────────────────────────────────────────────────────────────+
|  UI-0: Foundation, Theme, Components, Shell & Gallery Pipeline              |
|         │                                                                   |
|         ▼                                                                   |
|  UI-1: Onboarding / Setup Flow + Home Dashboard                             |
|         │                                                                   |
|         ▼                                                                   |
|  UI-2: Chat & Conversational Agent Screen                                   |
|  >>> MANDATORY PHONE CHECKPOINT: UI-0..UI-2 Gallery & Visual Approval <<<    |
|         │                                                                   |
|         ▼                                                                   |
|  UI-3: Project Screen, File Explorer, Code Editor & Live Preview            |
|         │                                                                   |
|         ▼                                                                   |
|  UI-4: Terminal Screen (PTY UI Shell)                                      |
|         │                                                                   |
|         ▼                                                                   |
|  UI-5: Android Build Dashboard, Git/GitHub & Project Templates              |
|         │                                                                   |
|         ▼                                                                   |
|  UI-6: Settings, Providers & Key Pool, DevStacks, Backup, Updates, Language |
|         │                                                                   |
|         ▼                                                                   |
|  UI-7: Background Tasks Center, Health Monitor & Battery Help Wizard        |
|         │                                                                   |
|         ▼                                                                   |
|  UI-8: Developer & Legal Credits, Licenses Viewer, In-App Bug Reporter      |
|         │                                                                   |
|         ▼                                                                   |
|  UI-9: Legacy UI Decommissioning, Tablet/Foldables & Polish                 |
|         │                                                                   |
|         ▼                                                                   |
|  ENG-1: True PTY Terminal Emulator Matrix (Termux / xterm.js)                |
|  ENG-2: Background Reliability & Phantom Process Killer Workarounds         |
|  ENG-3: Multi-Agent Fleet Expansion (Gemini CLI, Codex, OpenCode, Aider)    |
|  ENG-4: Application ID Migration (com.pocketforge.ai), Release Signing      |
+─────────────────────────────────────────────────────────────────────────────+
```

---

## Milestone Breakdown

### Milestone UI-0: Foundation, Design Tokens, Components & Gallery Pipeline
- **Goal:** Establish the `com.jarves.mh.ui.v2` foundation: ForgeDesign theme, 100% Phosphor icon system, typography fallback stack (Inter, JetBrains Mono, Noto Sans Bengali), modular component library (< 400 lines per file), navigation shell, Simple/Pro mode controller, stable signing configuration, and the automated JVM screenshot gallery pipeline (`ui-gallery`).
- **Key Deliverables:**
  - `ui/v2/theme/`: `ForgeTheme.kt`, `ForgeColors.kt` (Obsidian `#0D0F12`, AMOLED `#000000`, Light `#F8F9FA`, Electric Indigo `#6366F1`, Cyber Cyan `#06B6D4`, Emerald `#10B981`, Amber `#F59E0B`, Rose `#EF4444`), `ForgeTypography.kt`, `ForgeShapes.kt`.
  - `ui/v2/components/`: Modular component primitives:
    - `ForgeButton.kt`: Primary, Secondary, Ghost, Danger, Icon-only buttons with 48dp touch targets.
    - `ForgeCard.kt`: High-contrast bordered and elevated cards.
    - `ForgeChip.kt`: FilterChip, AssistChip, BadgeChip with explicit active states.
    - `ForgeInput.kt`: High-contrast `OutlinedTextField` with visible borders, focus ring, and subtle container fill.
    - `ForgeBottomSheet.kt`: Modal sheet with drag handle and thumb-friendly layout.
    - `ForgeStatusCards.kt`: High-contrast Tool Approval Card (amber), Error Card (rose), Empty State Card.
    - `ForgeModeSwitch.kt`: Real Simple vs. Pro toggle controlling visible controls.
    - `ForgeKeyBar.kt`: Virtual sticky key bar for terminal and code navigation.
  - `ui/v2/navigation/`: App shell, bottom navigation bar, top bar with project switcher, and routing.
  - **Stable CI Signing:** Configure stable project keystore so CI debug APKs can be installed over previous builds without uninstalling or losing data.
  - **Automated Screenshot Gallery Pipeline:** Roborazzi / LayoutLib JVM screenshot test suite rendering screens in Obsidian, AMOLED, and Light themes, English and Bengali, generating the `ui-gallery` CI artifact.
- **Verification:** CI builds passing; `ui-gallery` artifact generated; Roborazzi / test suite verified.

---

### Milestone UI-1: Setup / Onboarding Flow & Home Dashboard
- **Goal:** Build the complete onboarding flow and Home dashboard in `ui/v2` matching `screen-setup-lang`, `screen-setup-hardware`, `screen-setup-install`, `screen-setup-providers`, and `screen-home` from `docs/mockups/index.html`.
- **Key Deliverables:**
  - Guided setup wizard with 4 distinct steps:
    1. Language & Theme Selection (`SetupLanguageStep.kt`)
    2. Hardware Preflight Check (`SetupHardwareStep.kt`: ARM64, 16 KiB page size, RAM, Storage)
    3. Linux Runtime Download & Extraction (`SetupInstallStep.kt`: live progress, speed meter)
    4. Provider Key Configuration (`SetupProvidersStep.kt`: live test, success/error cards)
  - Home Dashboard (`HomeScreen.kt`):
    - Hero banner with "Start New Autonomous Task" CTA.
    - Running agent status chip with live pulse indicator.
    - Recent project cards with tag indicators, git branch, and quick action menus.
    - Import project modal (ZIP, Git URL, GitHub clone).
    - Mode-aware visibility: Simple mode shows 1 primary action; Pro mode shows recent terminal sessions and quick stats.
- **Verification:** CI build passes; screenshots rendered in `ui-gallery`.

---

### Milestone UI-2: Conversational Chat & Agent Execution Screen
- **Goal:** Implement the full conversational agent interface in `ui/v2` matching `screen-chat` from the approved mockup, complete with streaming messages, collapsible thinking accordion, activity tracking chips, bidirectional Tool Approval cards, and checkpoint undo/accept.
- **Key Deliverables:**
  - Message bubble list (`ChatScreen.kt`, `MessageBubble.kt`) with crisp separation between user and agent.
  - Collapsible reasoning block (`ReasoningBlock.kt`) displaying duration stopwatch and estimated token count.
  - Real-time activity chips (`ActivityTracker.kt`) showing file edits, shell runs, and reads with Phosphor icons.
  - Elevated Tool Approval card (`ToolApprovalCard.kt`): amber accent, monospace file path preview, explicit "Allow Once" and "Reject" actions wired to the bidirectional PRoot bridge.
  - Checkpoint bar (`CheckpointBar.kt`): "Undo Changes" and "Accept Changes" with unified diff preview.
  - Attachment bottom sheet (`ChatAttachSheet.kt`): Camera photo capture, document picker upload.
  - Model & Agent switcher bottom sheet (`AgentPickerSheet.kt`).
- **MANDATORY PHONE CHECKPOINT (UI-0..UI-2):**
  - Generate and inspect `ui-gallery` screenshots.
  - Publish APK to GitHub Actions.
  - **STOP AND WAIT FOR USER APPROVAL** before proceeding to UI-3.

---

### Milestone UI-3: Project Workspace, File Explorer, Editor & Live Preview
- **Goal:** Implement the project workspace tabs in `ui/v2` matching `screen-project`, `screen-editor`, and `screen-preview`.
- **Key Deliverables:**
  - Hierarchical file tree (`FilesTab.kt`, `FileTree.kt`) with Phosphor file/folder icons, search filter, and file action bottom sheet.
  - Full-screen code editor (`EditorTab.kt`, `CodeEditor.kt`) with line numbers, monospace JetBrains Mono font, dirty-state indicator, save action, and undo/redo.
  - Live in-app web preview (`PreviewTab.kt`, `WebPreview.kt`) auto-detecting local ports (3000, 5173, 8080), URL navigation bar, reload button, external browser launcher, and console drawer.
- **Verification:** CI build passes; `ui-gallery` screenshots updated.

---

### Milestone UI-4: PTY Terminal Screen (UI Shell)
- **Goal:** Build the dedicated terminal screen UI shell in `ui/v2` matching `screen-terminal`, with thumb-friendly sticky virtual key bar, quick command chips, session switcher, and output clear actions.
- **Key Deliverables:**
  - Dedicated Terminal screen (`TerminalScreen.kt`) with dark terminal surface (`#07080A`).
  - Virtual sticky key bar (`ForgeKeyBar.kt`): <kbd>ESC</kbd>, <kbd>TAB</kbd>, <kbd>CTRL</kbd>, <kbd>ALT</kbd>, <kbd>↑</kbd>, <kbd>↓</kbd>, <kbd>|</kbd>, <kbd>/</kbd>, <kbd>~</kbd>.
  - Quick action chips (`git status`, `npm run dev`, `ls -la`, `top`).
  - Terminal tab integrated into the main workspace.
- **Verification:** CI build passes; `ui-gallery` screenshots updated.

---

### Milestone UI-5: Android Build Dashboard, Git/GitHub & Templates
- **Goal:** Build the developer tools suite in `ui/v2` matching `screen-builder`, `screen-git`, and `screen-templates`.
- **Key Deliverables:**
  - Android Build Dashboard (`AndroidBuilderScreen.kt`): stage pipeline tracker (AAPT2, Kotlin, D8, Packaging), human-readable error cards linking to source lines, direct APK install trigger.
  - Git & GitHub Screen (`GitIntegrationScreen.kt`): device-code login flow, user profile card, clone repo modal, git status, diffs, branch switcher, and push/pull controls.
  - Project Templates Picker (`TemplatePickerScreen.kt`): Compose, Kotlin+XML, Next.js, FastAPI starter templates.
- **Verification:** CI build passes; `ui-gallery` screenshots updated.

---

### Milestone UI-6: Settings Suite, Key Vault, DevStacks & Backup
- **Goal:** Build the comprehensive settings suite in `ui/v2` matching `screen-settings`.
- **Key Deliverables:**
  - Grouped settings categories: AI Providers, Appearance & Theme, Language, Subsystems, Backup & Restore, About.
  - Multi-key provider pool (`ProviderKeyPoolSheet.kt`) with nicknames, active toggle, and API connection tester.
  - DevStack Toolchain Manager (`DevStackScreen.kt`): 1-tap installation/removal of Python, Android SDK, C/C++, and PHP toolchains.
  - Encrypted Backup & Restore (`BackupRestoreScreen.kt`): password-protected `.mhbackup` export and import.
  - In-app updater (`AppUpdateScreen.kt`) pointing to `jamesfnaim-blip/pocketforge` releases with release notes.
  - What's New modal (`WhatsNewModal.kt`).
- **Verification:** CI build passes; `ui-gallery` screenshots updated.

---

### Milestone UI-7: Background Tasks Center, Health Monitor & Battery Wizard
- **Goal:** Build the background management and diagnostic screens in `ui/v2` matching `screen-tasks` and `screen-battery`.
- **Key Deliverables:**
  - Background Tasks Center (`TasksCenterScreen.kt`): active agent processes, dev servers, builds, uptime, and memory impact.
  - Health & Resource Monitor (`HealthMonitorScreen.kt`): live RAM usage gauge, CPU load, internal storage meter, and battery state.
  - OEM Battery Optimization Wizard (`BatteryHelpScreen.kt`): brand-specific visual step-by-step guides for Samsung, Xiaomi, Oppo, Vivo, OnePlus, Huawei.
- **Verification:** CI build passes; `ui-gallery` screenshots updated.

---

### Milestone UI-8: Developer Credits, Licenses Viewer & In-App Bug Reporter
- **Goal:** Build the developer tribute, license compliance viewer, and privacy-respecting bug reporter matching `screen-developer` and `screen-bug-report`.
- **Key Deliverables:**
  - Developer & Credits Screen (`DeveloperCreditsScreen.kt`): Nayem Hossain profile, portfolio and LinkedIn links, Open Source attribution (Mobile Harness MIT, PRoot GPL-2.0, talloc LGPL-3.0, Phosphor MIT).
  - Open Source Licenses Viewer (`LicensesScreen.kt`) displaying full texts of all bundled libraries.
  - In-App Bug Report System (`BugReportScreen.kt`):
    - Collects environment diagnostics (app version, Android version, device model, ABI, mode, active agent/provider).
    - Secrets auto-redaction engine (scrubs API keys, tokens, OAuth codes, emails, file paths) backed by unit tests.
    - Transparent preview before submission; 1-tap GitHub Issue creation or file sharing.
- **Verification:** CI build passes; `ui-gallery` screenshots updated.

---

### Milestone UI-9: Legacy UI Decommissioning, Foldable/Tablet Layouts & Polish
- **Goal:** Remove legacy `ui/` files once every feature in `docs/feature-parity-matrix.md` is verified in `ui/v2`. Implement dual-pane tablet and foldable layouts, accessibility labels, and haptic feedback.
- **Key Deliverables:**
  - Safely delete decommissioned legacy UI files (`PocketDevApp.kt` and orphaned helpers).
  - Adaptive dual-pane layout for tablets and unfolded foldables (split editor on left, chat on right).
  - Complete TalkBack accessibility labeling and theme polish.
- **Verification:** CI build passes; zero references to legacy UI remain.

---

### Subsequent Engine Milestones (ENG-1 to ENG-4)
- **ENG-1: True PTY Terminal Emulator Matrix:** Replace Compose text terminal with full interactive VT100/xterm matrix supporting `vim`, `nano`, `htop`, `tmux`, and interactive TUIs.
- **ENG-2: Background Reliability Engine:** Harden ForegroundService, partial wake-lock, and Android 12+ Phantom Process Killer mitigations.
- **ENG-3: Multi-Agent Fleet Expansion:** Add bridge adapters for Gemini CLI, Codex, OpenCode, and Aider.
- **ENG-4: Application ID Migration & Release:** Migrate package namespace and `applicationId` to `com.pocketforge.ai`, finalize JNI bindings, and publish signed release APKs.
