# Comprehensive Testing Plan: Non-Coder Guide & Automated Test Suite

This document provides a dual testing strategy for **PocketForge AI**:
1. **The Non-Coder Manual Verification Guide:** Plain-language physical tests you can perform directly on your phone after each milestone, without writing a single line of code.
2. **Automated Test Specifications:** Automated unit, integration, and UI tests that the coding agent must author and execute.

---

## Part 1: How You (A Non-Coder) Test on a Real Phone

Whenever a new milestone APK is built, copy it to your phone and follow these simple step-by-step verification flows:

```
+─────────────────────────────────────────────────────────────────────────────+
|                          NON-CODER TESTING FLOW                             |
+─────────────────────────────────────────────────────────────────────────────+
|  Step 1: Install APK -> Tap app icon -> Confirm clean splash screen         |
|  Step 2: Onboarding Flow -> Pick Language -> Tap "Install Everything"       |
|  Step 3: Verification -> Progress bar completes without red error banner    |
|  Step 4: AI Test -> Enter API Key -> Ask "Create hello world" -> Check diff |
|  Step 5: Background Test -> Start build -> Lock screen 5 mins -> Verify OK  |
+─────────────────────────────────────────────────────────────────────────────+
```

### Milestone-by-Milestone Manual Acceptance Checklist

#### Test 1: First-Run Onboarding & System Setup
- **What to do:** Open the freshly installed app.
- **What you see:** A clean welcome screen asking for your language, showing your phone's processor (Snapdragon/MediaTek), and a large blue button: **`[ INSTALL EVERYTHING ]`**.
- **Pass Criteria:**
  - Tapping the button shows a smooth progress bar.
  - The progress bar completes to 100% within 1–2 minutes on Wi-Fi without any red error popup.
- **Fail Signs:** The app crashes to the Android home screen or shows *"Architecture not supported"*.

#### Test 2: AI Provider Connection & Key Validation
- **What to do:** In the onboarding or Settings, select **DeepSeek** (or Anthropic/OpenRouter) and paste your API key.
- **What you see:** The input field shows masked asterisks `****`. Tap the **"Test Connection"** button.
- **Pass Criteria:** A green checkmark appears: *"Key Verified: Connected to DeepSeek-V3"*.
- **Fail Signs:** A red warning: *"Invalid API Key or HTTP 401"*.

#### Test 3: The "Hello World" Project Test
- **What to do:** Tap **"Quick Chat"** on the home screen. Type in plain English: *"Create an HTML file that shows a big blue button saying 'Click Me' and plays a chime sound."*
- **What you see:**
  1. An accordion box appears: *"Thinking (1,200 tokens)..."*.
  2. Text streams out explaining what it is building.
  3. An activity chip appears: *"Edit index.html"*.
- **Pass Criteria:** The agent finishes, and a green badge appears: *"Preview ready on port 5173 / local"*. Tapping the **Preview** tab renders the working button!

#### Test 4: The 10-Minute Background Survival Test
- **What to do:** Ask the agent a larger task (e.g. *"Build an interactive tic-tac-toe game in HTML/JS"*). While the agent is actively working, **press the physical power button on your phone to turn the screen completely off**. Leave the phone on your desk for 5 minutes.
- **What you see in notification drawer:** A persistent notification: *"PocketForge: Agent working on tic-tac-toe (elapsed 2m 45s)..."*.
- **Pass Criteria:** Turn your screen back on and unlock your phone. The agent should have finished the task without getting killed by Android!
- **Fail Signs:** The app restarts from the splash screen, losing your conversation.

#### Test 5: The Interactive Terminal Test
- **What to do:** Tap the **Terminal** tab. Type `htop` and press Enter.
- **Pass Criteria:** A colorful real-time dashboard of your phone's CPU and memory appears. Press <kbd>F10</kbd> or <kbd>CTRL</kbd> + <kbd>C</kbd> using the mobile helper keys to exit cleanly.
- **Fail Signs:** Garbled characters appear (`^[[1;2H`) or the screen freezes.

---

## Part 2: Automated Testing Suite for Coding Agents

The AI build agent must implement and run automated tests for every module:

### 1. Unit Tests (`app/src/test/java/`)
- **`ApiKeyVaultTest.kt`:**
  - Test encryption and decryption with mock Keystore.
  - Test multi-key pool storage (adding, renaming, deleting, switching active key).
- **`LocalFormatGatewayTest.kt`:**
  - Spin up mock loopback server.
  - Send Anthropic `/v1/messages` JSON payload; verify it correctly transforms into OpenAI `/v1/chat/completions` payload.
- **`AndroidBuildIssueParserTest.kt`:**
  - Feed sample Gradle compilation errors (AAPT2 syntax error, Kotlin compiler unresolved reference).
  - Assert that file path, line number, and error suggestion are accurately parsed.
- **`GitCheckpointsTest.kt`:**
  - Create dummy project files, trigger checkpoint, alter file, invoke rollback; verify file returns to exact original byte contents.

### 2. Native JNI & Subsystem Tests
- **`PocketSpawnNativeTest.cpp`:**
  - Verify `posix_openpt()` allocates valid master/slave file descriptors.
  - Verify `prctl(PR_SET_DUMPABLE, 1)` succeeds.
  - Verify child processes inherit distinct process groups (`setpgid`).

### 3. Compose UI Component Tests (`app/src/androidTest/`)
- **`ApprovalCardTest.kt`:**
  - Verify clicking "Approve" emits `ToolApproved` event to the stream bridge.
  - Verify clicking "Reject" emits `ToolRejected`.
- **`TerminalHelperRowTest.kt`:**
  - Verify pressing virtual <kbd>CTRL</kbd> toggles sticky visual checkmark state.
