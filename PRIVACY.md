# PocketForge AI Privacy Policy

> [!IMPORTANT]
> **Owner Review Required:** This privacy policy must be reviewed and approved by the project owner (Nayem Hossain) prior to public release.

**Effective Date:** November 22, 2026  
**Repository:** [https://github.com/jamesfnaim-blip/pocketforge](https://github.com/jamesfnaim-blip/pocketforge)

PocketForge AI is an open-source, local-first mobile development environment. Your code, projects, conversations, and credentials belong to you. This policy explains in plain language what data the app handles, how it is secured, and when data leaves your phone.

---

## 1. Core Principles

- **Local-First:** All your projects, code, chat histories, git commits, and terminal outputs reside exclusively on your physical phone in private app storage.
- **No Background Telemetry:** PocketForge AI contains **zero** third-party ad networks, tracking trackers, behavioral analytics, or silent telemetry SDKs.
- **Hardware-Encrypted Keys:** Your AI provider API keys and tokens are encrypted with AES-256 using keys stored in your device's hardware **Android Keystore**.
- **User-Controlled Network Requests:** Data is sent over the internet only when you explicitly take an action (e.g., sending a prompt to an AI provider you chose, cloning a Git repo, or downloading runtime packages).

---

## 2. Information Handled by PocketForge AI

When you use the app, it handles:
- **AI Provider Credentials:** API keys, subscription tokens, or OAuth authorization codes that you configure.
- **Development Content:** Project source files, conversations, attachments (photos/documents), git logs, and terminal outputs.
- **Hardware Diagnostics:** Device architecture (`arm64-v8a`), memory (RAM), storage space, and kernel page size (4 KiB vs. 16 KiB) strictly to ensure local runtime compatibility.

---

## 3. When Information Leaves Your Device

Information only leaves your device in the following specific circumstances:

### A. Communicating with AI Providers
When you send a message in a project chat, the prompt, conversation context, and any files or tool results necessary for the agent to complete your task are transmitted directly to the AI provider endpoint you configured (e.g., Anthropic, DeepSeek, OpenRouter, Google). That transmission is subject to your provider's privacy policy.

### B. Downloading Runtime Environments
During initial setup or when installing optional stacks (Python, Android SDK, C++, PHP), PocketForge AI downloads verified open-source runtime archives via HTTPS. These requests send standard HTTP headers (IP address, user-agent) to the download hosts.

### C. In-App Bug Reporting (Opt-In Only)
If you choose to submit a bug report via **Settings → Help → Report a Problem** or after an unexpected crash:
1. **Automated Secret Redaction:** PocketForge AI automatically scrubs all API keys, bearer tokens, OAuth codes, Authorization headers, email addresses, and private filesystem paths from diagnostics.
2. **Full User Preview:** You are shown an exact preview of every piece of data collected before anything is sent.
3. **Explicit Action:** Data is never sent automatically in the background. The app either opens a pre-filled GitHub Issue in your browser or allows you to share the diagnostic text via Android's system share sheet.

---

## 4. Android Device Permissions

PocketForge AI requests only the permissions strictly required to provide a local development environment:
- **Internet (`android.permission.INTERNET`):** Used to communicate with user-selected AI APIs and download development packages.
- **Foreground Service & Wake Lock (`android.permission.FOREGROUND_SERVICE`, `WAKE_LOCK`):** Keeps local dev servers, compilers, and coding agents running reliably when the screen turns off.
- **Notifications (`android.permission.POST_NOTIFICATIONS`):** Informs you when a long-running background task, build, or agent finishes or needs your input.
- **Camera (`android.permission.CAMERA`):** Optional, used only if you tap the camera icon to photograph an error screen or whiteboard to show the agent.
- **Install Packages (`android.permission.REQUEST_INSTALL_PACKAGES`):** Allows triggering the standard Android Package Installer when you choose to install an APK built by PocketForge AI on your phone.

---

## 5. Data Retention & Deletion

- You can delete any project, file, chat, or saved API key at any time directly within the app.
- Uninstalling PocketForge AI completely wipes its private data directory, removing all local files, databases, and Keystore keys according to standard Android OS behavior.

---

## 6. Contact & Source Code

PocketForge AI is open-source software maintained by **Nayem Hossain**.  
- **Source Code & Issue Tracker:** [https://github.com/jamesfnaim-blip/pocketforge](https://github.com/jamesfnaim-blip/pocketforge)  
- **Website:** [https://nayemhsn.com](https://nayemhsn.com)
