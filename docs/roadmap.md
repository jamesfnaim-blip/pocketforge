# Implementation Roadmap: 11 Small Step-by-Step Milestones

This roadmap breaks down the complete construction of **PocketForge AI** into **11 bite-sized, independently testable milestones**. Each milestone has a concrete Definition of Done and a non-coder manual testing guide.

---

```
+─────────────────────────────────────────────────────────────────────────────+
|                          11-MILESTONE IMPLEMENTATION ROADMAP                 |
+─────────────────────────────────────────────────────────────────────────────+
|  M01: App Shell & ForgeDesign System ──► M02: Keystore & Settings           |
|                                                     │                       |
|  M04: True Interactive Terminal ◄────── M03: Linux Subsystem (Early Engine) |
|         │                                                                   |
|         ▼                                                                   |
|  M05: Projects & Git Checkpoints ──────► M06: AI Agent Bridge & Fleet       |
|                                                     │                       |
|  M08: Web Server & Live Preview  ◄───── M07: Tool Approvals & Diff Cards    |
|         │                                                                   |
|         ▼                                                                   |
|  M09: In-App VS Code IDE ──────────────► M10: On-Device Android Builder     |
|                                                     │                       |
|                                                     ▼                       |
|                                          M11: Background Engine & Credits   |
+─────────────────────────────────────────────────────────────────────────────+
```

---

### Milestone 1: App Foundation & New Design System Shell
* **Goal:** Create a clean, compiling Android project with the new **ForgeDesign** system (Obsidian, Light, and Pure AMOLED black themes) and bottom navigation bar.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/MainActivity.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/theme/Theme.kt`, `Color.kt`, `Type.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/MainScaffold.kt`
* **Definition of Done:** App compiles, launches smoothly, and displays 4 placeholder tabs (Projects, Code, Terminal, Settings) with working theme toggle.
* **How to Test on Your Phone:** Open the app. Confirm the bottom navigation works and switching between Light, Dark, and AMOLED black updates the background immediately.
* **Difficulty:** **Easy**

---

### Milestone 2: Hardware-Backed Keystore Vault & Settings Screen
* **Goal:** Implement the AES-256-GCM Keystore vault and the Settings screen allowing users to save and test AI provider keys.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/data/keystore/ApiKeyVault.kt`
  - `app/src/main/java/com/pocketforge/ai/network/ProviderApiClient.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/settings/SettingsScreen.kt`
* **Definition of Done:** User can enter an Anthropic or DeepSeek API key, tap "Test Connection", and receive a green checkmark or clean error alert.
* **How to Test on Your Phone:** Paste a real API key in Settings -> tap "Test Connection" -> verify green success badge appears.
* **Difficulty:** **Medium**

---

### Milestone 3: Linux Subsystem Engine & Rootfs Bootstrap
* **Goal:** Integrate the native C PRoot engine and implement streaming extraction of the Ubuntu 20.04 rootfs bundle.
* **Exact Files Touched:**
  - `app/src/main/cpp/pocket_spawn.c`, `CMakeLists.txt`, `executable_carrier.c`
  - `third_party/proot/`, `third_party/libandroid-shmem/`, `third_party/talloc/`
  - `app/src/main/java/com/pocketforge/ai/runtime/installer/RootfsExtractor.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/onboarding/OnboardingScreen.kt`
* **Definition of Done:** First-time launch downloads/extracts the Ubuntu rootfs with a live progress bar and writes `.pocket-runtime-ready`.
* **How to Test on Your Phone:** Launch the app, tap "Install Everything" -> watch progress bar -> confirm successful completion alert.
* **Difficulty:** **Hard**

---

### Milestone 4: True Interactive Terminal (Xterm / Termux Integration)
* **Goal:** Replace the original's static Compose Text viewer with a true interactive terminal emulator supporting cursor movement and full-screen curses apps.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/ui/screens/terminal/TerminalScreen.kt`
  - `app/src/main/java/com/pocketforge/ai/runtime/pty/NativeTerminalSession.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/components/TerminalKeyHelperBar.kt`
* **Definition of Done:** User can execute commands inside Ubuntu bash; full-screen tools (`htop`, `vim`, `nano`) render cleanly without garbled characters.
* **How to Test on Your Phone:** Open the Terminal tab -> type `htop` -> verify the real-time colorful CPU/memory dashboard renders and exits cleanly via <kbd>F10</kbd> or <kbd>CTRL</kbd>+<kbd>C</kbd>.
* **Difficulty:** **Hard**

---

### Milestone 5: Project Management & Git Workspace Checkpoints
* **Goal:** Create, list, rename, and delete projects in `/workspace`, with automated Git checkpoints for 1-tap "Undo Changes".
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/ui/screens/projects/ProjectsScreen.kt`
  - `app/src/main/java/com/pocketforge/ai/data/checkpoint/GitCheckpoints.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/projects/ProjectsViewModel.kt`
* **Definition of Done:** User can create projects, launch instant "Quick Chats", and rollback changes via Git reset.
* **How to Test on Your Phone:** Tap "New Project", name it "TestApp" -> verify folder exists in workspace -> verify "Quick Chat" opens an instant scratchpad.
* **Difficulty:** **Medium**

---

### Milestone 6: AI Agent Fleet & Real-Time Streaming Chat
* **Goal:** Embed and launch AI coding agents (Claude Code, DeepSeek Harness) with real-time text streaming and collapsible reasoning accordions.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/runtime/bridge/ClaudeAgentBridge.kt`
  - `app/src/main/java/com/pocketforge/ai/runtime/bridge/DeepSeekAgentBridge.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/chat/ChatScreen.kt`, `ChatViewModel.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/components/ReasoningAccordion.kt`
* **Definition of Done:** Sending a prompt streams reasoning tokens and assistant text smoothly without UI freezing.
* **How to Test on Your Phone:** Ask the agent: *"Explain how photosynthesis works in 3 bullet points"*. Verify text streams out smoothly and the "Thinking" drawer expands.
* **Difficulty:** **Hard**

---

### Milestone 7: Interactive Tool Execution & Approval Cards
* **Goal:** Intercept destructive agent actions (file edits, bash execution) and display an interactive `ApprovalCard` with diff view.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/ui/components/ApprovalCard.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/components/DiffViewer.kt`
  - `app/src/main/java/com/pocketforge/ai/runtime/bridge/ClaudeAgentBridge.kt`
* **Definition of Done:** Agent requests to edit files or run shell commands pause execution until the user taps "Approve".
* **How to Test on Your Phone:** Ask: *"Create a python script hello.py"*. Verify an approval card pops up showing the code diff -> tap "Approve" -> verify file is created.
* **Difficulty:** **Medium**

---

### Milestone 8: Live Web Server Discovery & In-App Browser Preview
* **Goal:** Detect background HTTP servers running on localhost and render them in an in-app WebView preview tab.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/network/PortScanner.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/preview/PreviewScreen.kt`
* **Definition of Done:** Running `python3 -m http.server 8000` or Vite automatically switches the preview tab to `http://localhost:PORT`.
* **How to Test on Your Phone:** Ask agent: *"Build an HTML page with a blue button and run a server"*. Tap the Preview tab -> verify the webpage renders live.
* **Difficulty:** **Medium**

---

### Milestone 9: In-App VS Code IDE (OpenVSCode Server Integration)
* **Goal:** Embed a full VS Code editor inside a hardware-accelerated WebView connected to OpenVSCode Server running inside PRoot.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/runtime/vscode/OpenVSCodeController.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/editor/VSCodeEditorScreen.kt`
* **Definition of Done:** Tapping the "Code" tab loads a full VS Code interface with multi-file tabs, file tree, and syntax coloring.
* **How to Test on Your Phone:** Tap the "Code" tab -> verify VS Code loads and shows your project's files -> make an edit and verify it saves.
* **Difficulty:** **Hard**

---

### Milestone 10: On-Device Android App Compilation & Installation
* **Goal:** Compile Android projects on-device via Gradle inside Ubuntu and install them directly using the Android `PackageInstaller` API.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/runtime/android/AndroidBuildRunner.kt`
  - `app/src/main/java/com/pocketforge/ai/runtime/android/AndroidIssueParser.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/android/AndroidDashboardScreen.kt`
* **Definition of Done:** User can tap "Build & Install APK" -> Gradle builds the APK -> Android system prompts to install the new app.
* **How to Test on Your Phone:** Create an Android Compose project template -> tap "Build APK" -> confirm Android prompts to install the resulting APK.
* **Difficulty:** **Hard**

---

### Milestone 11: Enterprise Background Engine, Developer Profile & Credits
* **Goal:** Add sticky foreground services, Phantom Process mitigation, OEM setup wizard, and the Developer & Credits screen.
* **Exact Files Touched:**
  - `app/src/main/java/com/pocketforge/ai/runtime/service/BackgroundRuntimeService.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/settings/OemOptimizationScreen.kt`
  - `app/src/main/java/com/pocketforge/ai/ui/screens/developer/DeveloperProfileScreen.kt`
* **Definition of Done:** Agent tasks survive 15+ minutes with the phone screen turned off; Developer Profile displays `Nayem Hossain` and full open-source license attribution.
* **How to Test on Your Phone:** Start a build, turn off phone screen for 10 minutes -> verify build completes; open Settings -> Developer Profile -> verify your name, links, and license notices appear cleanly.
* **Difficulty:** **Medium**
