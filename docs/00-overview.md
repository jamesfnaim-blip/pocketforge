# 00 - Product Overview: Mobile Harness & PocketDev

## 1. Executive Summary

**Mobile Harness** (internally identified by its engine codename **PocketDev**) is an autonomous AI-driven software development environment engineered to run completely on **ARM64 Android devices**.

Unlike lightweight cloud code-editor apps or simple SSH frontends, Mobile Harness bundles and executes a **full, isolated Linux operating system (Ubuntu 20.04.5 LTS ARM64)** directly inside Android's application sandbox without requiring root access (`su`). It uses a patched version of **PRoot** to intercept and translate Linux system calls into user-space Android system calls.

Inside this private Linux environment, the app embeds real developer toolchains—including **Node.js 24**, **Python 3**, **Git**, and optionally **OpenJDK 17 + Android SDK 36 + Gradle**—and runs state-of-the-art autonomous AI coding agents directly on the phone, including **Claude Code**, **DeepSeek Harness (`dsh`)**, and **Google Antigravity CLI (`agy`)**.

---

## 2. Target Audience & Personas

Mobile Harness bridges the gap between desktop AI workflows and handheld mobile devices. Its target user base comprises three core personas:

### A. The "Vibe Coder" & Curious Non-Coder
* **Characteristics:** Has little to no traditional syntax programming experience, but has clear product ideas and knows how to prompt AI models.
* **Needs:** Wants to say *"Build me a currency converter app in HTML/CSS"* or *"Create a task manager"*, have the agent create all the files, install dependencies, run the test server, and show a working preview with a single tap.
* **Pain Point with Mobile:** Until now, building software required a PC with terminal tools, Node, Git, and complex setups. Mobile Harness brings that full capability to their pocket phone.

### B. The Mobile Developer & Commuter
* **Characteristics:** Professional or indie software developer who spends hours commuting or traveling without a laptop.
* **Needs:** Wants to triage GitHub issues, test quick bug fixes, run automated tests, inspect git diffs, or build and install debug APKs directly on their physical device on the subway or bus.
* **Pain Point with Mobile:** Termux is powerful but barebones and manual; cloud editors like GitHub Codespaces require continuous high-speed internet and disconnect on bad mobile signals.

### C. The AI Experimenter & Privacy-Conscious Engineer
* **Characteristics:** Likes exploring the latest AI coding CLIs (Claude Code, DeepSeek, Antigravity) and wants direct API key control.
* **Needs:** Multiple AI provider backends (Anthropic, DeepSeek, OpenRouter, self-hosted Ollama/llama.cpp endpoints), hardware-backed credential encryption, and offline or private project storage that never leaks source code to untrusted cloud servers.

---

## 3. Core Capabilities & Architecture Pillars

1. **Embedded Linux Subsystem (Rootless PRoot):**
   - Boots a minimal Ubuntu 20.04 rootfs in userspace using `ptrace` system call interception.
   - Provides standard Linux paths (`/usr/bin`, `/bin`, `/home`, `/workspace`, `/tmp`) with simulated root privileges (`fake_id0`).
   - Requires zero root permissions and runs within Android's private app data directory (`/data/data/com.jarves.mh/files/runtime/ubuntu`).

2. **Native Process Spawning & PTY Engine (`libpocketspawn.so`):**
   - Custom C native library loaded via JNI (`Java_com_jarves_mh_runtime_NativeSpawn_spawn`).
   - Creates real pseudo-terminal (PTY) master/slave pairs using POSIX terminal APIs (`posix_openpt`, `grantpt`, `unlockpt`, `ptsname`, `ioctl(TIOCSCTTY)`).
   - Allows interactive terminal applications (shells, vim, htop, agent prompts) to run with full ANSI color support and proper terminal line discipline.

3. **Autonomous AI Coding Agents On-Device:**
   - **Claude Code (Anthropic):** Runs Anthropic's official CLI with stream-JSON protocol over standard I/O pipes.
   - **DeepSeek Harness (`dsh`):** Executes DeepSeek's headless agent, streaming reasoning deltas and tool execution steps.
   - **Antigravity CLI (`agy`):** Integrates Google's developer agent CLI with Google account OAuth flow.
   - **Multi-Provider API Router:** Connects directly to Anthropic, DeepSeek, OpenRouter, Moonshot/Kimi, NVIDIA NIM, and custom OpenAI-compatible gateways.

4. **Live In-App Web Preview:**
   - Built-in Android `WebView` that detects when an agent or terminal command starts a local HTTP development server (e.g., Vite on port 5173, Python `http.server` on port 8000).
   - Supports auto-reloading and port scanning (`127.0.0.1:PORT`).

5. **On-Device Android App Compilation:**
   - Bundles Temurin OpenJDK 17 and Android SDK build-tools inside the Linux subsystem.
   - Runs `./gradlew assembleDebug` directly on ARM64 phone hardware.
   - Directly triggers Android's native `PackageInstaller` session to install the compiled `.apk` onto the phone without needing a computer.

6. **Hardware-Backed Security:**
   - All API keys and secrets are encrypted with **AES-256-GCM** via the **Android Keystore** (`pocket-provider-key`), preventing unauthorized extraction even on rooted devices.

---

## 4. End-to-End User Experience: How It Feels to Use

### 1. First-Time Setup & Onboarding
- **Language Picker:** On first launch, the user is greeted with a clean language selection screen (supporting English, Russian, Simplified Chinese, etc.).
- **Battery & Background Permission:** Explains clearly why background execution is needed and opens Android's system settings to exempt the app from battery optimization ("Unrestricted" battery).
- **Device Hardware Verification:** The app checks that the device has an `arm64-v8a` CPU, at least 4 GB of RAM, and sufficient free storage (at least 2.5 GB for core runtime).
- **Provider Choice & API Key Entry:** The user picks their preferred AI service (e.g., Anthropic, DeepSeek, OpenRouter) and enters their API key. The key is validated in real-time with a live ping test before moving forward.
- **Runtime Installation:**
  - In the **Online** build, the app downloads a compressed rootfs bundle (`pocketdev-core-arm64.tar.zst`, ~72 MB download, ~369 MB uncompressed) from GitHub Releases and decompresses it with a live progress bar.
  - In the **Offline** build, the runtime is extracted directly from the APK assets.
  - The user can optionally select additional dev stacks to install: **Python**, **Android (Java/Kotlin)**, **C/C++**, or **PHP**.

### 2. The Main Workspace Experience
- **Projects Screen:** Shows a list of existing projects, recent activity timestamps, and a primary action button: **"New Project"** or **"Quick Chat"**.
- **Chat Tab (AI Interaction):**
  - The user types a natural-language prompt (e.g., *"Make a responsive portfolio website with a dark theme"*).
  - The AI agent streams its thinking process in collapsible reasoning blocks.
  - As the agent edits files or runs shell commands, **Activity Chips** show what is happening in real-time (*"Editing index.html"*, *"Running npm install"*).
  - Dangerous commands (file deletion, git reset) trigger an interactive **Approval Card** where the user can inspect diffs and tap **"Approve"** or **"Reject"**.
- **Files Tab:**
  - A responsive file tree explorer showing project folders and files.
  - Tapping a file opens an in-app viewer/editor with syntax highlighting, line numbers, and search.
  - Users can create new files, create folders, upload images, or take camera photos to attach as context for the AI agent.
- **Terminal Tab:**
  - A full-featured terminal emulator with virtual keys (<kbd>ESC</kbd>, <kbd>TAB</kbd>, <kbd>CTRL</kbd>, <kbd>ALT</kbd>, <kbd>↑</kbd>, <kbd>↓</kbd>).
  - Runs standard bash commands inside Ubuntu: `ls`, `git status`, `python3 test.py`, `npm start`.
- **Preview Tab:**
  - An embedded browser preview that instantly renders the project's web page as soon as a local dev server starts.
- **Android Dashboard (for Android Projects):**
  - If the project is an Android app, a dedicated dashboard shows Gradle build stages, compilation status, APK size, error diagnostics with exact file/line links, and a **"Build & Install APK"** button.

### 3. Background Persistence
- When the user switches to WhatsApp or locks the screen, an ongoing foreground notification (*"Mobile Harness: Claude Code working in MyProject..."*) ensures Android's Low Memory Killer (LMK) does not terminate the process.
- When the task completes, the notification updates with a success alert, allowing the user to tap and return directly to the result.
