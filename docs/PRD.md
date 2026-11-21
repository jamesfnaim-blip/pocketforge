# Product Requirements Document (PRD)

**Project Name:** Mobile Harness Next (Working Title: **PocketForge AI**)  
**Document Version:** 1.0.0  
**Target Platform:** Android (ARM64-v8a, Android 9.0+ / API 28–36)  
**Author / Maintainer:** Nayem Hossain  

---

## 1. Vision & Core Philosophy

**PocketForge AI** is the complete, autonomous AI software development workspace engineered directly for Android smartphones and tablets. It transforms modern Android phones into self-contained development machines capable of running an isolated **Linux operating system (Ubuntu ARM64)**, **autonomous AI coding agents (Claude Code, DeepSeek Harness, Antigravity CLI, OpenCode, Aider)**, a full **in-app VS Code editor (OpenVSCode Server)**, a **real interactive terminal**, a **live web preview engine**, and an **on-device Android compilation toolchain**.

### Core Pillars
1. **Zero-Friction Onboarding:** A non-coder should be able to install Linux and start building their first web app with an AI agent in under 2 minutes with fewer than 4 taps.
2. **Rock-Solid Background Longevity:** AI agents, long-running build tasks, and background servers must never die when the user minimizes the app, locks the screen, or receives a phone call.
3. **True Desktop Parity:** Real terminal emulation (with cursor matrix and alternate buffer for `vim`, `htop`, `tmux`), real VS Code environment via OpenVSCode Server, and direct on-device APK generation.
4. **Hardware-Backed Privacy:** User code and API keys never leave the phone's private storage without explicit user authorization; credentials remain encrypted with AES-256-GCM via the Android Keystore.

---

## 2. Target Personas

```
+───────────────────────────────────────────────────────────────────────────────+
|                               TARGET PERSONAS                                 |
+───────────────────────────────────────────────────────────────────────────────+
|  Persona 1: The Non-Coder / Curious Creator                                   |
|  - Skill: No code syntax knowledge; knows what they want to build.            |
|  - Goal: "Build me a recipe tracker app" -> One tap -> Working live app.      |
|  - Needs: Guided setup, plain language, auto-detected errors, visual preview. |
+───────────────────────────────────────────────────────────────────────────────+
|  Persona 2: The "Vibe Coder" / Indie Builder                                  |
|  - Skill: Understands prompting, frameworks (React, Python), and git basics.  |
|  - Goal: Rapidly prototype MVPs and SaaS ideas anywhere without a laptop.      |
|  - Needs: Fast agent switching, OpenRouter model flexibility, diff approvals. |
+───────────────────────────────────────────────────────────────────────────────+
|  Persona 3: The Professional Mobile Engineer                                  |
|  - Skill: Experienced with Kotlin, Gradle, Linux, bash, and git.              |
|  - Goal: Debug PRs, build and install Android APKs directly on their phone.    |
|  - Needs: Real terminal (tmux/vim), full VS Code editor, Logcat, background.  |
+───────────────────────────────────────────────────────────────────────────────+
```

---

## 3. User Stories

### Persona 1: Non-Coder / Beginner
- **US-1.1:** As a beginner, I want a 1-tap "Install Everything" setup so I don't have to configure Linux packages or terminal commands myself.
- **US-1.2:** As a beginner, I want to paste my DeepSeek or Claude API key once and have the app verify it works with a green checkmark before starting.
- **US-1.3:** As a beginner, I want to chat with an AI agent in plain language and immediately see my website running in a visual preview window.
- **US-1.4:** As a beginner, I want an "Undo" button that safely rolls back mistakes made by the AI agent without breaking my project.

### Persona 2: Vibe Coder / Indie Builder
- **US-2.1:** As a builder, I want to use any LLM provider (Anthropic, DeepSeek, OpenRouter, local endpoints) so I can optimize cost and capability.
- **US-2.2:** As a builder, I want my agent to keep writing code, generating assets, and installing npm packages in the background while I switch to other apps.
- **US-2.3:** As a builder, I want to inspect file diffs and review shell commands before the agent executes them so my files stay safe.

### Persona 3: Professional Mobile Engineer
- **US-3.1:** As an engineer, I want a full VS Code-style editor inside the app so I can edit code with syntax highlighting, extensions, and file tabs.
- **US-3.2:** As an engineer, I want a true interactive terminal that supports `vim`, `htop`, and `tmux` sessions that persist even if the app process restarts.
- **US-3.3:** As an Android developer, I want to build debug APKs on-device using Gradle and install them directly onto my phone with one click.

---

## 4. Functional Requirements (FR)

### Module 1: Onboarding & Subsystem Bootstrap
- **FR-001 (Language Selection):** On initial launch, provide a localized language picker (English, Bengali, Spanish, Chinese, Russian) applied dynamically to all UI contexts.
- **FR-002 (System Compatibility Audit):** Verify ARM64 architecture (`arm64-v8a`), minimum 4 GB RAM, and at least 3 GB free disk space. Block incompatible x86 emulators with a friendly explanation.
- **FR-003 (One-Tap Linux Setup):** Download, verify (SHA-256), and extract the Ubuntu 20.04 ARM64 rootfs in userspace via PRoot using streaming Zstandard decompression.
- **FR-004 (Modular Toolchains):** Allow optional installation of developer stacks: Web (Node.js 24 + npm), Python 3 (pip + venv), Android SDK (JDK 17 + SDK 36 + Gradle 8.14), C/C++ (GCC + Make), and PHP (Composer).
- **FR-005 (Atomic Rootfs Repair):** If a rootfs update or repair is needed, extract to `.staging` and atomically swap directories with automatic rollback on failure to safeguard user files in `/home` and `/workspace`.

### Module 2: AI Provider & Agent Integration
- **FR-010 (Multi-Provider Vault):** Securely store API credentials for Anthropic, DeepSeek, OpenRouter, Moonshot/Kimi, NVIDIA NIM, and custom OpenAI-compatible endpoints using Android Keystore AES-256-GCM.
- **FR-011 (Live Credential Validation):** Test provider credentials with an instant HTTPS ping before saving to eliminate confusing auth errors during agent execution.
- **FR-012 (Agent Fleet Management):** Provide on-demand installation and switching between **Claude Code**, **DeepSeek Harness (`dsh`)**, and **Google Antigravity CLI (`agy`)**.
- **FR-013 (Clean Runtime Download):** Download proprietary agent CLIs directly from official vendor distribution channels at runtime on user request, avoiding intellectual property infringement.
- **FR-014 (Local Format Translation Gateway):** Run an in-app loopback proxy on `127.0.0.1` translating Claude Code's Anthropic Messages requests into OpenAI Chat/Responses format for third-party providers.
- **FR-015 (OpenRouter Routing Injector):** Loopback proxy injecting custom provider ordering and fallback policies into agent API requests.
- **FR-016 (Live Streaming Chat UI):** Stream assistant text deltas, token counters, and collapsible chain-of-thought reasoning accordions in real time.
- **FR-017 (Interactive Tool Approval):** Intercept destructive agent actions (file edits, bash execution) and display an approval card with risk badge, command preview, and file diff.

### Module 3: In-App VS Code Editor (OpenVSCode Server)
- **FR-020 (OpenVSCode Subsystem Daemon):** Run OpenVSCode Server (or code-server) inside the PRoot Linux subsystem listening on a private loopback socket (`127.0.0.1:PORT`).
- **FR-021 (In-App WebView IDE):** Embed the full VS Code interface within an optimized, hardware-accelerated Compose `AndroidView(WebView)` with session state persistence.
- **FR-022 (Workspace Auto-Sync):** Keep files edited in VS Code instantly synchronized with the AI agent's workspace and Git checkpoint system.

### Module 4: True Interactive Terminal
- **FR-030 (Full VT100 / Xterm Emulation):** Replace static Compose Text scrollviews with a true terminal emulator (supporting cursor addressing, alternate screen buffers, line wrapping, and 256-color ANSI palettes).
- **FR-031 (Interactive TUI Support):** Provide full support for interactive console apps (`vim`, `nano`, `htop`, `tmux`, `git interactive rebase`).
- **FR-032 (Terminal Session Persistence):** Run terminal shells under a background socket daemon so active terminal sessions and running scripts survive app minimizes and orientation changes.
- **FR-033 (Mobile Keyboard Helper Row):** Persistent toolbar above soft keyboard offering essential terminal keys: <kbd>ESC</kbd>, <kbd>TAB</kbd>, <kbd>CTRL</kbd>, <kbd>ALT</kbd>, <kbd>PIPE</kbd>, <kbd>~</kbd>, <kbd>/</kbd>, <kbd>-</kbd>, <kbd>↑</kbd>, <kbd>↓</kbd>, <kbd>→</kbd>, <kbd>←</kbd>.

### Module 5: Workspace & File Operations
- **FR-040 (Project Manager):** Support creating named projects, instant zero-setup "Quick Chats", cloning repositories, and deleting projects.
- **FR-041 (Workspace File Explorer):** Interactive tree hierarchy showing folders, file sizes, creation timestamps, and breadcrumb navigation.
- **FR-042 (Media & Document Attachments):** Allow users to attach images from their photo gallery or take camera photos to provide visual context to AI agents.
- **FR-043 (Git Checkpoints & Rollback):** Automatically commit checkpoints before each agent turn; provide a 1-tap "Undo Changes" button that reverts project files via `git reset --hard`.

### Module 6: Live Web Preview & Server Discovery
- **FR-050 (Auto-Port Discovery):** Continuously scan local loopback ports (3000, 5173, 8000, 8080) for newly launched HTTP servers.
- **FR-051 (Embedded Preview Tab):** Automatically display the running web application in an in-app browser with back/forward navigation and refresh controls.

### Module 7: On-Device Android App Builder
- **FR-060 (Template Generator):** One-tap creation of modern Jetpack Compose or Kotlin+XML Android projects.
- **FR-061 (On-Device Gradle Build):** Run `./gradlew assembleDebug` inside PRoot using embedded Temurin OpenJDK 17 and Android SDK 36.
- **FR-062 (Smart Diagnostic Parser):** Parse compiler errors and XML issues into clickable diagnostic cards that link directly to the faulty file and line.
- **FR-063 (Direct Package Installation):** Install compiled APKs directly via Android's `PackageInstaller` API without requiring a computer.
- **FR-064 (Integrated Logcat):** Stream and filter device logs by application package name to debug runtime crashes immediately.

### Module 8: Background Execution & Power Management
- **FR-070 (Sticky Foreground Service):** Run active agent turns, builds, and servers inside a sticky foreground service with a persistent notification.
- **FR-071 (CPU WakeLock):** Acquire a partial CPU wake lock to ensure background processes run at full speed with the screen off.
- **FR-072 (Phantom Process Mitigation):** Enforce process concurrency limits (`--max-workers=2`) and provide an optional 1-tap Shizuku/ADB system fix.
- **FR-073 (OEM Optimization Wizard):** Built-in guided setup for OEM-specific battery killers (Xiaomi, Samsung, Oppo, Vivo, Huawei) linking to system settings.
- **FR-074 (System Health Watchdog):** Dashboard showing live process tree, CPU usage, RAM consumption, and thermal throttling state.

### Module 9: Developer Profile, Credits & Licensing
- **FR-080 (Developer Profile Screen):** Dedicated profile card displaying `Nayem Hossain`, biography, website (`https://nayemhsn.com`), LinkedIn (`https://linkedin.com/in/nayemdevbd`), and changelog.
- **FR-081 (Open-Source Attribution Screen):** Prominent attribution crediting the original Mobile Harness project, PRoot, talloc, libandroid-shmem, and all third-party libraries with links to original licenses.

---

## 5. Non-Functional Requirements (NFR)

| Category | Requirement Metric / Standard |
| :--- | :--- |
| **Performance** | - App cold launch to interactive dashboard in `< 1.2 seconds`.<br>- Terminal input latency `< 16 ms` (60 FPS fluid typing).<br>- Agent output streaming updates rendered at `>= 30 FPS` without UI jank. |
| **Memory (RAM)** | - Base app idle RAM footprint `< 120 MB`.<br>- Linux subsystem + agent idle RAM footprint `< 250 MB`.<br>- Proactive memory trimming via `onTrimMemory()` to prevent Android LMK kills. |
| **Battery & Thermal** | - Idle background consumption `< 1% per hour`.<br>- Active agent execution throttles concurrency if thermal status reaches `THERMAL_STATUS_SEVERE`. |
| **Storage Budget** | - Base APK size `< 25 MB` (Online flavor).<br>- Extracted core Ubuntu rootfs `< 450 MB`.<br>- Full Android build stack `< 1.2 GB`. |
| **Offline Reliability**| - Projects, file editing, terminal commands, and local git operations remain 100% functional without internet connectivity. |

---

## 6. Out-of-Scope (Version 1.0)

1. **Local LLM Inference (On-Device GGUF / Llama.cpp):** Running 7B/8B models locally consumes 6–8 GB RAM and causes severe thermal throttling on non-flagship phones. V1.0 will focus exclusively on API-based agent execution.
2. **iOS Support:** iOS does not permit `ptrace`, `fork()`, or dynamic binary execution in app sandboxes.
3. **Multi-Architecture Emulation (x86 on ARM):** Running x86 binaries via QEMU adds massive performance overhead. V1.0 strictly targets native `arm64-v8a`.
4. **Cloud Project Sync:** Initial release stores projects locally with password-protected encrypted file backups; cloud account sync is slated for V2.0.

---

## 7. Success Metrics & Key Performance Indicators (KPIs)

- **Onboarding Completion Rate:** `> 85%` of first-time users successfully install the rootfs and run their first prompt without encountering an unrecoverable error.
- **Background Task Survival:** `> 95%` of background agent sessions and builds running longer than 15 minutes complete without being killed by Android.
- **Crash-Free Sessions:** `> 99.2%` crash-free session rate on devices running Android 10 through Android 16.
- **App Rating & Polish:** Targeted user satisfaction score `> 4.7 / 5.0` based on beginner ease-of-use and visual aesthetics.
