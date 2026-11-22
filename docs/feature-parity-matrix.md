# PocketForge AI: Feature Parity & Migration Matrix

**Last Updated:** November 22, 2026  
**Objective:** Track every single screen, workflow, and capability from the original Mobile Harness codebase to ensure zero feature regression during the UI rebuild.

---

## Status Classification Legend
- **(a) Implemented in v2 UI:** Fully rebuilt in `com.jarves.mh.ui.v2` following the approved ForgeDesign system (Phosphor icons only, modular composables under 400 lines, Obsidian/AMOLED/Light themes, English + Bengali).
- **(b) Reachable in Legacy UI:** Fully functional and accessible right now in the app by tapping **"Switch to Classic UI"** at the top of the screen or in Pro mode.
- **(c) Missing:** Broken or dropped. (Target: **0 features**).

---

## Complete Feature Parity Matrix (40 Features)

| # | Feature Name | Original Source File / Composable | Current Status | Rebuild Milestone | Notes & Parity Details |
| :---: | :--- | :--- | :---: | :---: | :--- |
| **1** | First-Run Language Picker | `InitialLanguageSetupScreen.kt` | **(a) Implemented in v2** | UI-1 | Step 1 of `SetupWizardScreen.kt` with live locale switching. |
| **2** | Background Task / Battery Flow | `BackgroundTaskSetupScreen.kt` | **(b) Reachable in Legacy UI** | UI-7 | Battery optimization intent & OEM wizard. |
| **3** | Device Hardware Preflight | `RuntimeCompatibility.kt`, `ui/PocketDevApp.kt` | **(a) Implemented in v2** | UI-1 | Step 2 of `SetupWizardScreen.kt` (ARM64, RAM, Page size). |
| **4** | AI Provider Selection | `ProviderChoiceStep.kt` | **(a) Implemented in v2** | UI-1 | Step 4 of `SetupWizardScreen.kt` (Claude, OpenAI, DeepSeek, Antigravity). |
| **5** | Hardware-Backed Keystore Vault | `ApiKeyVault.kt`, `ProviderCredentialsStep.kt` | **(a) Implemented in v2** | UI-1 | AES-256 hardware enclave badge & live key validation. |
| **6** | Multi-Key Provider Pool | `ApiKeyVault.kt`, `ui/AgentScreen.kt` | **(b) Reachable in Legacy UI** | UI-6 | Available via Classic UI Agent Settings. |
| **7** | Runtime Rootfs Installation | `RuntimeInstaller.kt`, `RuntimeInstallationScreen.kt` | **(a) Implemented in v2** | UI-1 | Step 3 of `SetupWizardScreen.kt` (Ubuntu 20.04 extraction). |
| **8** | DevStack Toolchain Manager | `DevStackComponents.kt`, `DevStackVisuals.kt` | **(b) Reachable in Legacy UI** | UI-5 | Python, Android SDK, Clang management. |
| **9** | PRoot Subsystem Execution | `NativeSpawnProcess.kt`, `cpp/pocket_spawn.c` | **(a) Implemented in v2** | Engine (Retained) | Active in both v2 and legacy UI. |
| **10** | Monospaced PTY Scrollback | `TerminalScreen.kt` | **(b) Reachable in Legacy UI** | ENG-1 | Accessible in Pro Mode via Classic UI; upgraded in ENG-1. |
| **11** | Quick Command Keyboard Bar | `QuickTerminalSheet.kt` | **(a) Implemented in v2** | UI-0, UI-2 | Rebuilt in `ForgeKeyBar.kt` (ESC, TAB, CTRL, ALT, /, \|). |
| **12** | Project Creation & Templates | `ProjectsScreen.kt`, `FolderActionComponents.kt` | **(b) Reachable in Legacy UI** | UI-3, UI-5 | Full template picker in Classic UI. |
| **13** | ZIP Project Export & Import | `FolderActionComponents.kt`, `ProjectsScreen.kt` | **(b) Reachable in Legacy UI** | UI-3 | Import/export working in Classic UI. |
| **14** | Workspace File Tree Browser | `FilesTab.kt` | **(b) Reachable in Legacy UI** | UI-3 | Tree traversal working in Classic UI. |
| **15** | In-App Code Editor & Syntax | `FileViewerScreen.kt` | **(b) Reachable in Legacy UI** | UI-3 | Line-numbered viewer in Classic UI. |
| **16** | Local Web Server & Preview | `PreviewTab.kt` | **(b) Reachable in Legacy UI** | UI-4 | In-app WebView previewing port 3000/8080. |
| **17** | Android Gradle Builder | `AndroidDashboardScreen.kt`, `AndroidBuildText.kt` | **(b) Reachable in Legacy UI** | UI-5 | APK builder & assembly diagnostics. |
| **18** | APK Signature Diagnostics | `ProfessionalAndroidOverview.kt` | **(b) Reachable in Legacy UI** | UI-5 | v1/v2 signing inspector. |
| **19** | Package-Filtered Logcat Console | `ProfessionalAndroidOverview.kt` | **(b) Reachable in Legacy UI** | UI-7 | Real-time system logcat stream. |
| **20** | Git Status & Interactive Diffs | `ChangesTab.kt` | **(b) Reachable in Legacy UI** | UI-5 | Git diff inspection in Classic UI. |
| **21** | Undo & Accept Changes | `ChangesTab.kt`, `ui/PocketDevApp.kt` | **(a) Implemented in v2** | UI-2 | Rebuilt as `ForgeCheckpointCard.kt` in `ChatScreen.kt`. |
| **22** | Claude Code Bridge Adapter | `ClaudeRuntimeBridge.kt` | **(a) Implemented in v2** | UI-2 | Event dispatcher wired to `ChatScreen.kt`. |
| **23** | Interactive Tool Approval | `ClaudeActivityDisclosure.kt`, `RuntimeInstaller.kt` | **(a) Implemented in v2** | UI-2 | Rebuilt as `ForgeToolApprovalCard.kt` (Allow Once/Always/Deny). |
| **24** | Collapsible Reasoning Disclosure | `AnimatedThinkingDots.kt`, `ClaudeActivityDetail.kt` | **(a) Implemented in v2** | UI-2 | Rebuilt with Cyber Cyan sparkle in `ChatScreen.kt`. |
| **25** | Chat Context & Token Meter | `ChatTab.kt` | **(b) Reachable in Legacy UI** | UI-2 | Detailed token breakdown in Classic UI. |
| **26** | Multi-Chat Switcher Dialog | `ChatSwitcherDialog.kt` | **(b) Reachable in Legacy UI** | UI-3 | Multiple conversation branches per project. |
| **27** | Image & Camera Attachment | `ChatTab.kt`, `CommonUi.kt` | **(b) Reachable in Legacy UI** | UI-2 | Camera and gallery picker in Classic UI. |
| **28** | Antigravity OAuth & Bridge | `AntigravityRuntimeBridge.kt`, `AntigravityOnboardingScreen.kt` | **(b) Reachable in Legacy UI** | UI-6 | Google Auth bridge in Classic UI. |
| **29** | DeepSeek Harness Bridge | `DshRuntimeBridge.kt` | **(b) Reachable in Legacy UI** | UI-6 | Open-source DeepSeek CLI bridge. |
| **30** | GitHub OAuth & Repo Clone | `ProjectsScreen.kt` | **(b) Reachable in Legacy UI** | UI-5 | Device flow OAuth & remote git clone. |
| **31** | Read-Only Project Browser | `ReadOnlyProjectScreen.kt` | **(b) Reachable in Legacy UI** | UI-3 | Static inspector in Classic UI. |
| **32** | Hardened Background Service | `RuntimeExecutionService.kt` | **(a) Implemented in v2** | Engine (Retained) | Active in background for both UIs. |
| **33** | Backup & Full Restore (.tar.gz)| `BackupRestoreUi.kt` | **(b) Reachable in Legacy UI** | UI-6 | Full rootfs & workspace snapshot restore. |
| **34** | What's New & Release Notes | `WhatsNewDialog.kt` | **(b) Reachable in Legacy UI** | UI-6 | Changelog modal. |
| **35** | In-App Self-Updater | `AppUpdateChecker.kt` | **(b) Reachable in Legacy UI** | UI-6 | Manifest-based APK auto-downloader. |
| **36** | Developer Tribute & Credits | `DeveloperCreditsScreen.kt` | **(a) Implemented in v2** | UI-8 | Nayem Hossain profile & open-source licenses. |
| **37** | Atomic Rootfs Repair | `RootfsRepair.kt` | **(a) Implemented in v2** | Engine (Retained) | Active in background for both UIs. |
| **38** | 16 KiB Alignment Preflight | `RuntimeCompatibility.kt` | **(a) Implemented in v2** | UI-1 | Checked in `SetupWizardScreen.kt`. |
| **39** | Dynamic Model Localization | `ModelLabels.kt` | **(a) Implemented in v2** | UI-0 | English and Bengali string mapping. |
| **40** | Compose Markdown Parser | `MarkdownText.kt` | **(b) Reachable in Legacy UI** | UI-2 | Full syntax highlighter in Classic UI. |

---

## Summary of Parity

* **Total Capabilities:** 40 features
* **(a) Implemented in New v2 UI:** 14 features (35%)
* **(b) Reachable in Legacy UI:** 26 features (65%)
* **(c) Missing:** 0 features (0%)

> [!NOTE]
> By providing an instant, 1-tap bidirectional toggle between the new ForgeDesign UI and the Classic Mobile Harness UI, **100% of features remain completely accessible to the user** while the incremental milestone ports (UI-3 through UI-9) are developed and verified.
