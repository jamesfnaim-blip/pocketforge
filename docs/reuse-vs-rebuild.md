# Reuse vs. Rebuild Matrix: Component-by-Component Strategy

**Target Parity:** ~92–95% functional feature parity with Mobile Harness, combined with a 10x improvement in UI/UX polish, stability, and background longevity.

---

## 1. Executive Strategy

| Decision Tier | Strategy | Target Components |
| :---: | :--- | :--- |
| **REUSE AS-IS** | Keep code verbatim with original copyright notices. | Low-level C/C++ PRoot engine, JNI PTY spawn, POSIX carriers, talloc, libandroid-shmem. |
| **REUSE WITH MODIFICATION** | Keep core algorithmic logic, but refactor into clean, maintainable modular classes. | Rootfs extractor, agent stream parsers, loopback gateways, Keystore vault, Android build runner. |
| **REBUILD BETTER** | Completely rewrite from scratch to eliminate critical flaws in the original. | Terminal screen, code editor, monolithic UI (`PocketDevApp.kt`), monolithic ViewModel, background supervisor. |

---

## 2. Complete Component Decision Table

| Subsystem / Component | Original File(s) | Decision | Action & Justification | Attribution Requirement |
| :--- | :--- | :---: | :--- | :--- |
| **PRoot System Call Tracer** | `third_party/proot/*` | **REUSE AS-IS** | Proven user-space system call interceptor with Termux Android compatibility patches. | Retain GPL-2.0 license notice in `third_party/proot/`. |
| **Native PTY Spawn JNI** | `app/src/main/cpp/pocket_spawn.c` | **REUSE AS-IS** | Robust POSIX PTY allocation (`posix_openpt`, `ptsname`, `ioctl(TIOCSCTTY)`, `ioctl(TIOCSWINSZ)`). | Retain MIT notice and Mobile Harness header. |
| **ELF Executable Carrier** | `app/src/main/cpp/executable_carrier.c` | **REUSE AS-IS** | Clever packaging stub that tricks AGP into shipping ELF binaries inside APK native libraries. | Retain Mobile Harness header. |
| **Launcher Dumpable Wrapper** | `app/src/main/cpp/pocket_launcher.c` | **REUSE AS-IS** | Correctly sets `prctl(PR_SET_DUMPABLE, 1)` and unsets Android `LD_PRELOAD`. | Retain Mobile Harness header. |
| **Hierarchical Memory Allocator** | `third_party/talloc/*` | **REUSE AS-IS** | Samba talloc library required by PRoot. | Retain LGPL-3.0 notice. |
| **Shared Memory IPC Shim** | `third_party/libandroid-shmem/*`| **REUSE AS-IS** | Translates SysV shared memory to Android `/dev/ashmem`. | Retain BSD-3-Clause notice. |
| **Zstandard Streaming Extractor**| `runtime/RuntimeInstaller.kt` | **REUSE W/ MOD** | The streaming decompression logic (`ZstdCompressorInputStream` -> `TarArchiveInputStream`) is excellent, but must be decoupled from UI code into a dedicated `RootfsExtractor.kt`. | MIT Attribution in header. |
| **Crash-Resilient Rootfs Swap** | `runtime/RootfsRepair.kt` | **REUSE W/ MOD** | Atomic `.staging` -> `ubuntu/` swap with rollback protection is preserved and refactored into the runtime installer. | MIT Attribution in header. |
| **Hardware Keystore Vault** | `data/ApiKeyVault.kt` | **REUSE W/ MOD** | AES-256-GCM Keystore encryption is rock-solid. Refactor to use modern AndroidX Security Crypto or clean `ByteArray` sanitization. | MIT Attribution in header. |
| **Claude Code Stream Bridge** | `runtime/ClaudeRuntimeBridge.kt`| **REUSE W/ MOD** | Keep stream-JSON parsing and tool approval logic, but decouple from monolithic ViewModel into `ClaudeAgentBridge.kt`. | MIT Attribution in header. |
| **DeepSeek Harness Bridge** | `runtime/DshRuntimeBridge.kt` | **REUSE W/ MOD** | Headless one-shot execution logic and reasoning extraction are retained with clean coroutine error handling. | MIT Attribution in header. |
| **Antigravity CLI Bridge** | `runtime/AntigravityRuntimeBridge.kt`| **REUSE W/ MOD** | OAuth extraction and streaming event parser are retained in `AntigravityAgentBridge.kt`. | MIT Attribution in header. |
| **OpenAI Format Gateway** | `runtime/LocalFormatGateway.kt` | **REUSE W/ MOD** | Retain Anthropic-to-OpenAI loopback HTTP translation; improve error handling and socket timeout management. | MIT Attribution in header. |
| **OpenRouter Routing Gateway** | `runtime/OpenRouterRoutingGateway.kt`| **REUSE W/ MOD** | Retain JSON body injection for provider routing preferences. | MIT Attribution in header. |
| **Git Workspace Checkpoints** | `runtime/WorkspaceCheckpoints.kt`| **REUSE W/ MOD** | Keep automatic git commit snapshots before turns for 1-tap "Undo Changes". | MIT Attribution in header. |
| **On-Device Android Builder** | `runtime/AndroidBuildSupport.kt`| **REUSE W/ MOD** | Retain optimized Gradle invocation flags (`--no-daemon --max-workers=2 -Pandroid.aapt2FromMavenOverride=...`) and error parser regex. | MIT Attribution in header. |
| **On-Device APK Installer** | `runtime/AndroidAppInstaller.kt`| **REUSE W/ MOD** | Retain Android `PackageInstaller` session creation logic. | MIT Attribution in header. |
| **Encrypted Backup & Restore** | `backup/BackupManager.kt`, `BackupCrypto.kt` | **REUSE W/ MOD** | Retain `.mhbackup` ZIP AES-GCM encryption format; upgrade password derivation to Argon2id. | MIT Attribution in header. |
| **In-App Self-Updater** | `update/AppUpdater.kt` | **REUSE W/ MOD** | Retain GitHub Releases manifest checking and APK downloader. | MIT Attribution in header. |
| **Terminal UI Screen** | `ui/TerminalScreen.kt` | **REBUILD BETTER** | **Replace completely.** Original is a static Compose Text view that scrambles `vim`, `htop`, `tmux`. Rebuild using **Xterm.js** or **Termux TerminalView** with real VT100 character matrix. | New Component. |
| **Code Viewer & Editor** | `ui/PocketDevApp.kt` (`FileViewerScreen`) | **REBUILD BETTER** | **Replace completely.** Replace primitive text box with an embedded **OpenVSCode Server** inside a hardware-accelerated WebView. | New Component. |
| **Monolithic UI Host** | `ui/PocketDevApp.kt` (7,297 lines) | **REBUILD BETTER** | **Break down completely.** Decompose into individual modular screen files (`OnboardingScreen.kt`, `ProjectsScreen.kt`, `ChatScreen.kt`, etc.). | New Architecture. |
| **Monolithic ViewModel** | `ui/MainViewModel.kt` (5,177 lines) | **REBUILD BETTER** | **Break down completely.** Split into scoped ViewModels (`ProjectsVM`, `ChatVM`, `TerminalVM`, `AndroidBuildVM`). | New Architecture. |
| **Background Execution Engine** | `runtime/RuntimeExecutionService.kt`| **REBUILD BETTER** | **Supercharge.** Upgrade from basic wake lock to an enterprise engine with Phantom Process mitigation, session persistence daemon, and OEM battery killer guides. | Enhanced Architecture. |
| **Developer Profile & Credits** | *(None in original)* | **NEW BUILD** | Beautiful developer card with `Nayem Hossain`, links, and comprehensive open-source license attribution. | New Component. |
