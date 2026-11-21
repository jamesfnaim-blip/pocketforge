# Complete Feature Parity Matrix: Mobile Harness vs. PocketForge AI

This document maps **every functional capability, provider, agent, toolchain, setting, and permission** from the original Mobile Harness codebase to its exact architectural placement in **PocketForge AI**.

---

## 1. Core Feature Inventory Mapping (All 40 Features)

| # | Mobile Harness Feature | Original Location | New PocketForge UI Component | Mode Visibility | Status | Enhancement in PocketForge AI |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | First-Run Language Picker | `PocketDevApp.kt` (lines 590-764) | `SetupLanguageStep` / `SettingsLanguageScreen` | Simple & Pro | **Improved** | Added Bengali (`bn-BD`) with Noto Sans Bengali font; live instant re-rendering without activity restarts. |
| **2** | Background & Battery Exemption | `PocketDevApp.kt` (lines 819-1062) | `SetupBatteryStep` / `BatteryHelpScreen` | Simple & Pro | **Improved** | Automatic OEM detection (Samsung, Xiaomi, Oppo, Vivo, OnePlus) with visual step-by-step toggle guide. |
| **3** | Device Hardware Preflight | `PocketDevApp.kt` (lines 2910-2950) | `SetupHardwarePreflightStep` | Simple & Pro | **Improved** | Real-time checks for ARM64, free disk, RAM, and 16 KiB page size kernel compatibility with clear warning cards. |
| **4** | AI Provider Selection | `PocketDevApp.kt` (lines 2953-3050) | `SetupProviderStep` / `ProvidersScreen` | Simple & Pro | **Improved** | Filtered by selected agent; clear pricing/tier guidance; multi-key support per provider. |
| **5** | Hardware Keystore Vault | `ApiKeyVault.kt`, `ProviderApiClient.kt` | `KeyVaultManager` / `SettingsKeysScreen` | Simple & Pro | **Improved** | Added biometric fingerprint authentication before revealing or modifying stored credentials. |
| **6** | Multi-Key Provider Pool | `ApiKeyVault.kt`, `AgentScreen.kt` | `ProviderKeyPoolSheet` | Pro Mode | **Improved** | Visual key switcher with friendly nicknames, last-used timestamp, and quick connectivity test indicator. |
| **7** | Runtime Rootfs Install | `RuntimeInstaller.kt`, `RuntimeSetupService.kt` | `SetupInstallStep` / `TasksCenter` | Simple & Pro | **Improved** | Resilient chunked downloading with pause/resume, SHA-256 verification, and real-time unpack speed meter. |
| **8** | DevStack Toolchain Manager | `PocketDevApp.kt` (lines 1133-1603) | `DevStackScreen` / `SettingsDevStacks` | Pro Mode | **Improved** | On-demand 1-tap installation of Python, Android SDK, C/C++, and PHP toolchains without full reinstall. |
| **9** | Agent Engine Selection | `PocketDevApp.kt` (lines 1665-1750) | `AgentSwitchSheet` / `SettingsAgents` | Simple & Pro | **Improved** | Supports switching between Claude Code, DeepSeek Harness, Antigravity CLI, Gemini CLI, and OpenCode per project. |
| **10**| Antigravity Google Sign-In | `AntigravityAuthController.kt` | `AntigravityAuthModal` | Simple & Pro | **Improved** | Clean in-app OAuth sheet with 1-tap browser auth link and token paste field with auto-trimming. |
| **11**| Projects & Quick Chat | `PocketDevApp.kt` (lines 3592-4218) | `HomeScreen` / `ProjectsScreen` | Simple & Pro | **Improved** | Prominent "Start New Autonomous Task" CTA; auto-named quick chats; project cards with tags and git status. |
| **12**| Multi-Chat Project Threads | `PocketDevApp.kt` (lines 4889-4940) | `ChatHistoryDrawer` / `ChatSwitcherModal` | Simple & Pro | **Improved** | Dedicated sidebar drawer allowing multiple conversation threads within the same project directory. |
| **13**| Live Agent Chat Stream | `PocketDevApp.kt` (lines 6286-7080) | `ChatScreen` | Simple & Pro | **Improved** | Smoother streaming animation; syntax-highlighted code with 1-tap "Copy" and "Insert into Editor" actions. |
| **14**| Collapsible Thinking / Reasoning | `PocketDevApp.kt` (lines 6714-6840) | `ReasoningBlock` | Simple & Pro | **Improved** | Clean cyan badge with elapsed seconds counter; smoothly expands to inspect inner model chain-of-thought. |
| **15**| Real-Time Activity Tracking | `PocketDevApp.kt` (lines 6844-7000) | `ActivityTrackerCard` | Simple & Pro | **Improved** | Phosphor vector icons for file edits, bash runs, and reads; clean duration stopwatch and progress status. |
| **16**| Tool Approval System | `PocketDevApp.kt` (lines 7112-7129) | `ToolApprovalCard` | Simple & Pro | **Improved** | High-contrast amber card with command preview, unified file diff, and clear Approve / Reject buttons. |
| **17**| Workspace File Explorer | `PocketDevApp.kt` (lines 5912-6250) | `FilesTab` / `FileTreeComponent` | Simple & Pro | **Improved** | Hierarchical file tree with file type icons, search bar, and multi-selection support. |
| **18**| In-App Code Editor | `PocketDevApp.kt` (lines 5587-5900) | `EditorTab` / `CodeEditorComponent` | Simple & Pro | **Improved** | Monospace editor with line numbers, code syntax highlighting, dirty-state indicator, and search/replace. |
| **19**| File Operations (New, Upload, Camera) | `PocketDevApp.kt` (lines 6257-6285) | `FileActionMenu` / `ChatAttachSheet` | Simple & Pro | **Improved** | Bottom sheet offering New File, New Folder, Document Picker upload, and Camera photo capture for agent inspection. |
| **20**| Workspace Checkpoints & Undo | `WorkspaceCheckpoints.kt` | `CheckpointBar` / `GitHistorySheet` | Simple & Pro | **Improved** | Prominent "Undo Changes" bar after agent turns; unified side-by-side diff preview of modified files. |
| **21**| Pseudo-Terminal (PTY) | `TerminalScreen.kt`, `pocket_spawn.c` | `TerminalTab` / `TerminalScreen` | Pro Mode | **Improved** | Replaced static Compose text with real PTY emulator (Termux terminal-view / xterm.js); supports vim, htop, tmux! |
| **22**| Live In-App Web Preview | `PocketDevApp.kt` (lines 7291-7445) | `PreviewTab` / `WebPreviewScreen` | Simple & Pro | **Improved** | Auto-detects local dev servers (3000, 5173, 8080); URL bar, refresh button, external browser link, console logs drawer. |
| **23**| Android App Builder Dashboard | `PocketDevApp.kt` (lines 4965-5550) | `AndroidBuilderScreen` | Pro Mode | **Improved** | Dedicated build dashboard with stage indicators (AAPT2, Kotlin, D8, Sign), error breakdown, and logcat. |
| **24**| Build Diagnostics & Issue Parser| `AndroidBuildSupport.kt` | `BuildIssueCard` | Pro Mode | **Improved** | Parses Gradle errors into human-readable cards linking directly to the line of code in the editor. |
| **25**| On-Device APK Installer | `AndroidAppInstaller.kt` | `InstallApkAction` | Pro Mode | **Kept** | Directly commits APK install sessions using Android `PackageInstaller` API with user confirmation. |
| **26**| Android Logcat Viewer | `AndroidBuildSupport.kt` | `LogcatViewerTab` | Pro Mode | **Improved** | Monospace log console filtered by package name with log-level color coding and regex search bar. |
| **27**| Foreground Execution & WakeLock | `RuntimeExecutionService.kt` | `BackgroundEngineService` | Background | **Improved** | Hardened foreground service with persistent progress notification, partial wake lock, and Phantom Killer workarounds. |
| **28**| System Health & Resource Monitor| `PocketDevApp.kt` (line 5556) | `HealthMonitorScreen` | Pro Mode | **Improved** | Live RAM meter, CPU utilization, battery state, and running subsystem process listing. |
| **29**| Encrypted Backup & Restore | `BackupManager.kt`, `BackupCrypto.kt` | `BackupRestoreScreen` | Simple & Pro | **Improved** | AES-256 encrypted `.mhbackup` export and import, preserving project files and Keystore API keys. |
| **30**| In-App Self-Updater | `AppUpdater.kt` | `AppUpdateDialog` / `SettingsUpdates` | Simple & Pro | **Improved** | Background check against GitHub Releases with formatted markdown changelog and direct APK download. |
| **31**| Theme & UI Customization | `Theme.kt`, `SettingsScreenModern.kt` | `SettingsAppearanceScreen` | Simple & Pro | **Improved** | Switch between Obsidian Dark, AMOLED Pure Black, and Clean Light; custom accent colors and font sizes. |
| **32**| "What's New" Release Modal | `WhatsNewDialog.kt` | `WhatsNewModal` | Simple & Pro | **Improved** | Rich modal highlighting newly added features and bug fixes after each app upgrade. |
| **33**| LocalFormatGateway (OpenAI) | `LocalFormatGateway.kt` | `LocalFormatGatewayService` | Background | **Kept** | Local translation proxy converting Claude Anthropic format to OpenAI `/v1/chat/completions` transparently. |
| **34**| OpenRouter Routing Gateway | `OpenRouterRoutingGateway.kt` | `OpenRouterGatewayService` | Background | **Kept** | Loopback proxy injecting custom provider routing preferences and fallbacks for OpenRouter requests. |
| **35**| GitHub Sign-In & Repo Clone | `GitHubClient.kt` | `GitHubIntegrationScreen` | Simple & Pro | **Improved** | Device flow sign-in (`github.com/login/device`), user profile card, and repository browser for 1-tap clone. |
| **36**| Android Project Templates | `AndroidProjectTemplateGenerator.kt` | `TemplatePickerSheet` | Simple & Pro | **Improved** | Wizard creating Jetpack Compose or Kotlin+XML projects with preconfigured offline build settings. |
| **37**| Rootfs Crash-Resilient Repair | `RootfsRepair.kt` | `SubsystemRecoveryScreen` | Pro Mode | **Improved** | Atomic staging and swap mechanism protecting against broken installs with 1-tap "Repair Rootfs" action. |
| **38**| Runtime & 16 KiB Preflight | `RuntimeCompatibility.kt` | `CompatibilityEngine` | Background | **Kept** | Verifies `arm64-v8a` ABI and enforces flexible page-size binaries for Android 15/16 16 KiB kernels. |
| **39**| Dynamic Model & Provider Labels | `ModelLabels.kt` | `LocalizationRepository` | Background | **Improved** | Complete string resources in English and Bengali for all LLM providers, agents, and toolchains. |
| **40**| Compose Markdown Parser | `MarkdownText.kt` | `ForgeMarkdownView` | Simple & Pro | **Improved** | Rich native Markdown view with fenced code blocks, table formatting, and clickable internal file links. |

---

## 2. Exhaustive Enumeration & Settings Mapping

### 2.1 AI Provider Kinds (`model/Models.kt`)
- `CLAUDE` (Claude Subscription / OAuth): Mapped to `ProviderChoiceStep` and `ProvidersScreen`.
- `ANTHROPIC` (Anthropic API Console): Mapped to `ProviderChoiceStep` with model `claude-sonnet-4-6`.
- `LLM_ROUTER` (OpenRouter): Mapped to `ProviderChoiceStep` with model `~anthropic/claude-sonnet-latest`.
- `DEEPSEEK` (DeepSeek Official API): Mapped to `ProviderChoiceStep` with model `deepseek-v4-flash`.
- `KIMI` (Moonshot Kimi): Mapped to `ProviderChoiceStep` with model `kimi-k2.6`.
- `OPENCODE_ZEN` (OpenCode Zen Gateway): Mapped to `ProviderChoiceStep` with protocol `openai-responses`.
- `NVIDIA_NIM` (NVIDIA NIM Gateway): Mapped to `ProviderChoiceStep` with model `qwen/qwen2.5-coder-32b-instruct`.
- `CUSTOM` (Custom Anthropic-compatible Endpoint): Mapped with editable Base URL and model picker.

### 2.2 Agent Kinds (`model/Models.kt`)
- `CLAUDE_CODE` (`claude-code`, 71.8 MB): Fully mapped to `AgentSwitchSheet`.
- `DEEPSEEK_HARNESS` (`deepseek-harness`, 26.5 MB): Fully mapped to `AgentSwitchSheet`.
- `ANTIGRAVITY` (`antigravity`, 39.9 MB): Fully mapped with Google OAuth login dialog.
- **NEW (Planned):** `GEMINI_CLI`, `CODEX`, `OPENCODE`, `AIDER`.

### 2.3 DevStack Toolchains (`model/Models.kt`)
- `WEB` (Node.js & npm): Pre-installed core stack.
- `PYTHON` (Python 3, pip, venv, build tools): Mapped to `DevStackScreen`.
- `ANDROID` (JDK 17, SDK 36, Build Tools 35, Gradle 8.14.3): Mapped to `DevStackScreen`.
- `CPP` (gcc, g++, make, cmake, gdb): Mapped to `DevStackScreen`.
- `PHP` (php-cli, Composer, common extensions): Mapped to `DevStackScreen`.

### 2.4 Android Permissions (`AndroidManifest.xml`)
- `INTERNET`: Required for API requests, package downloads, and web preview.
- `ACCESS_NETWORK_STATE`: Monitors offline/online status.
- `POST_NOTIFICATIONS`: Android 13+ permission for foreground service progress notifications.
- `FOREGROUND_SERVICE`: Runs long tasks in background.
- `FOREGROUND_SERVICE_SPECIAL_USE`: Android 14+ subtype for user-started coding tasks.
- `FOREGROUND_SERVICE_DATA_SYNC`: Used by `BackupService` for file exports.
- `WAKE_LOCK`: Partial wake lock preventing CPU sleep during agent execution.
- `REQUEST_INSTALL_PACKAGES`: Allows installing compiled Android APKs.
- `UPDATE_PACKAGES_WITHOUT_USER_ACTION`: Streamlined self-updates.

### 2.5 All App Preferences (`data/AppPreferences.kt`)
All 24 preferences are retained and mapped to the new settings screens:
- `onboarding_complete`, `runtime_setup_complete`, `background_setup_complete`
- `initial_language_selected`, `language_code`
- `agent_kind`, `primary_agent_kind`
- `agent_antigravity_model`, `agent_antigravity_effort`, `agent_antigravity_signed_in`, `agent_antigravity_account_email`
- `whats_new_seen_code`, `backup_feature_seen`, `github_login`
- `dsh_version`, `theme_mode`
- `debug_update_manifest_url`, `selected_dev_stacks`
- `provider_kind`, `provider_base_url`, `provider_model`, `provider_dsh_api`, `provider_openrouter_order`, `provider_openrouter_allow_fallbacks`
- `agent_conversation_*` per project/chat.
