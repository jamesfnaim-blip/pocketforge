# Implementation Roadmap: 11 Sequential Milestones

This roadmap breaks down the complete construction of **PocketForge AI** into **11 bite-sized, independently verifiable milestones**. PocketForge AI follows a **Fork-and-Evolve** engineering philosophy: the application remains fully bootable and testable at every single milestone.

---

```
+─────────────────────────────────────────────────────────────────────────────+
|                     11-MILESTONE FORK-AND-EVOLVE ROADMAP                    |
+─────────────────────────────────────────────────────────────────────────────+
|  M01: Rebrand & Developer Screen ──► M02: Design Shell & File Split          |
|                                                     │                       |
|  M04: Home, Project & Chat ◄──────── M03: Guided Setup Flow                 |
|  (PHONE CHECKPOINT 1)                                                       |
|         │                                                                   |
|         ▼                                                                   |
|  M05: Files, Editor & Preview ─────► M06: True PTY Terminal Emulator         |
|                                      (PHONE CHECKPOINT 2)                   |
|                                                     │                       |
|  M08: Android Build Dashboard ◄───── M07: Settings & Key Vault Suite        |
|         │                                                                   |
|         ▼                                                                   |
|  M09: Background Engine ───────────► M10: Multi-Agent Fleet Expansion       |
|  (PHONE CHECKPOINT 3)                               │                       |
|                                                     ▼                       |
|                                          M11: App ID Rename & Release       |
|                                          (FINAL CHECKPOINT)                 |
+─────────────────────────────────────────────────────────────────────────────+
```

---

### Milestone 1: Rebranding, Legal Credits & Developer Screen
- **Goal:** Rebrand application strings, launcher icons, README, NOTICE, and introduce the dedicated Developer & Open-Source Credits screen featuring **Nayem Hossain**. Retain `com.jarves.mh` package identifier for safe incremental evolution.
- **Key Deliverables:**
  - Update `strings.xml` with "PocketForge AI" branding and descriptions.
  - Replace mipmap launcher icons with the new SVG-generated brand marks.
  - Implement `DeveloperCreditsScreen.kt` featuring Nayem Hossain profile, links (`nayemhsn.com`, `linkedin.com/in/nayemdevbd`), and attribution for Mobile Harness (MIT), PRoot (GPL-2.0), talloc (LGPL-3.0), libandroid-shmem (BSD-3-Clause), and Phosphor Icons (MIT).
  - Update `README.md` and `NOTICE`.
- **CI Verification:** GitHub Actions builds debug APK successfully.
- **Rollback / Safety:** Low risk; isolated to resources and UI presentation.

---

### Milestone 2: ForgeDesign System, Shell & Monolith Decomposition
- **Goal:** Implement the new `ForgeDesign` Compose theme (Obsidian Dark, AMOLED Black, Clean Light), Phosphor Icons helper components, bottom navigation bar, and split the 7,485-line `PocketDevApp.kt` monolith into modular files under 400 lines each.
- **Key Deliverables:**
  - Modular theme system (`Theme.kt`, `Color.kt`, `Type.kt`).
  - `PhosphorIcon.kt` composable helper using VectorDrawables.
  - Decompose `PocketDevApp.kt` into dedicated packages (`ui/navigation/`, `ui/screens/home/`, `ui/screens/chat/`, etc.).
- **CI Verification:** `./gradlew assembleOnlineDebug` passes with zero regression.

---

### Milestone 3: Guided Onboarding & Setup Experience
- **Goal:** Replace scattered setup screens with a unified, thumb-friendly 1-flow setup experience with real-time hardware preflight, live progress bar, and API key verification.
- **Key Deliverables:**
  - Language selection step with English and Bengali.
  - Hardware preflight checklist (ARM64, 16 KiB page size, RAM, disk).
  - Resilient rootfs download/extract progress indicator.
  - Provider key verification with green checkmark / actionable error card.
  - Battery optimization exemption prompt.
- **CI Verification:** GitHub Actions debug build passes.

---

### Milestone 4: Home Dashboard, Project Workspace & Conversational Chat
- **Goal:** Implement the clean Home dashboard, project management cards, and the interactive AI agent chat stream with collapsible reasoning, activity chips, and tool approval cards.
- **Key Deliverables:**
  - Home screen with "Start New Autonomous Task" CTA, running status chip, and recent projects.
  - Chat screen with user/agent message bubbles, syntax-highlighted code blocks, and 1-tap copy/insert.
  - Collapsible thinking accordion displaying duration and token metrics.
  - Actionable tool approval card (Approve / Reject).
- **PHONE CHECKPOINT 1:** User installs APK on device, starts a chat with Claude Code/DeepSeek, tests tool approval card, and reports back.

---

### Milestone 5: File Explorer, Code Editor & Live Web Preview
- **Goal:** Implement the file tree manager, file operations, full-screen code editor with line numbers, and embedded live web preview.
- **Key Deliverables:**
  - Hierarchical file tree with file-type icons, search, and multi-selection.
  - File operations bottom sheet (New File, New Folder, Rename, Delete, Upload, Camera Photo).
  - Monospace code editor with dirty-state indicator and save action.
  - Embedded web preview with URL bar, reload, and external browser launch.
- **CI Verification:** GitHub Actions build passes.

---

### Milestone 6: True PTY Linux Terminal Emulation Engine
- **Goal:** Replace the static Compose text scrollback with a true interactive terminal emulation engine supporting in-place cursor addressing, alternate screen buffers, and raw keystroke streaming.
- **Key Deliverables:**
  - Integration of real terminal emulation matrix (Termux terminal-view or hardware-accelerated xterm.js).
  - Full support for interactive command-line tools: `vim`, `nano`, `htop`, `top`, `tmux`, and interactive agent TUIs.
  - Thumb-friendly sticky virtual key bar (<kbd>ESC</kbd>, <kbd>TAB</kbd>, <kbd>CTRL</kbd>, <kbd>ALT</kbd>, <kbd>↑</kbd>, <kbd>↓</kbd>, <kbd>|</kbd>, <kbd>/</kbd>, <kbd>~</kbd>).
- **PHONE CHECKPOINT 2:** User launches `htop` or `vim` in the terminal, tests sticky keys and window resizing, and reports back.

---

### Milestone 7: Settings Suite, Multi-Key Vault, Backup & Updates
- **Goal:** Build the complete settings suite with searchable categories, multi-key provider pool, password-encrypted backups, and in-app self-updater.
- **Key Deliverables:**
  - Grouped settings categories (AI Providers, Appearance, Language, Subsystem).
  - Multi-key pool per provider with nicknames and active key toggle.
  - Hardware Keystore AES-256 vault with biometric fingerprint unlock.
  - Encrypted `.mhbackup` export and restore.
  - In-app updater with release changelog dialog.
- **CI Verification:** GitHub Actions build passes.

---

### Milestone 8: Android Build Dashboard & On-Device APK Installer
- **Goal:** Build the specialized Android developer dashboard with build progress timeline, issue parser, direct APK installer, and Logcat streaming.
- **Key Deliverables:**
  - Build pipeline stage tracker (AAPT2, Kotlin, D8, Packaging).
  - Clickable error cards linking directly to source lines.
  - Direct on-device APK installer using `PackageInstaller` API.
  - Package-filtered Logcat streaming console.
- **CI Verification:** GitHub Actions build passes.

---

### Milestone 9: Hardened Background Execution Engine & OEM Battery Wizard
- **Goal:** Harden the background execution engine to guarantee long-running tasks, servers, and builds survive screen-off and app-kill, accompanied by a brand-aware battery wizard.
- **Key Deliverables:**
  - Hardened foreground service with persistent notification and stop action.
  - Partial wake-lock management preventing CPU sleep.
  - Android 12+ Phantom Process Killer mitigations.
  - Guided OEM setup wizard for Samsung, Xiaomi, Oppo, Vivo, OnePlus, and Huawei.
  - Background Tasks Center showing active processes, uptime, and memory impact.
- **PHONE CHECKPOINT 3:** User starts a long build or local dev server, turns off phone screen for 15 minutes, and verifies task is still running.

---

### Milestone 10: Multi-Agent Fleet Expansion & Advanced Features
- **Goal:** Add support for additional AI coding agents (Gemini CLI, Codex, OpenCode, Aider), global command palette (Ctrl+K), project starter templates, and git source control panel.
- **Key Deliverables:**
  - Gemini CLI, Codex, and OpenCode bridge adapters.
  - Global search / command palette across files, chats, and actions.
  - Starter templates picker (Next.js, Compose, FastAPI, Node).
  - Git panel (status, diffs, commits, branches, push/pull with GitHub).
- **CI Verification:** GitHub Actions build passes.

---

### Milestone 11: Application ID Migration, Release Hardening & Delivery
- **Goal:** Safely migrate `applicationId` to `com.pocketforge.ai`, perform JNI boundary checks, run final end-to-end regression tests, and package signed production release APKs.
- **Key Deliverables:**
  - Clean package namespace and `applicationId` migration with updated CMake bindings.
  - Complete automated test suite execution.
  - Cryptographically signed release APK artifact uploaded to GitHub Actions.
  - Publish documentation and user guide.
- **FINAL CHECKPOINT:** User installs final PocketForge AI release APK and verifies all features end-to-end.
