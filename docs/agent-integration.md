# AI Agent Integration Architecture

This document details how **Mobile Harness** embeds, configures, launches, and communicates with autonomous AI coding agents—specifically **Claude Code**, **DeepSeek Harness (`dsh`)**, and **Google Antigravity CLI (`agy`)**—directly on mobile devices.

---

## 1. High-Level Agent Architecture

Mobile Harness does not implement an LLM inference engine on device; instead, it runs **official developer CLI agent binaries** inside its rootless Linux subsystem. The Android UI communicates with these CLIs over bidirectional standard I/O streams (`stdin`, `stdout`, `stderr`), parsing structured JSON events in real time.

```
+---------------------------------------------------------------------------------+
|                       Jetpack Compose UI (ChatTab)                              |
|   - Message List        - Collapsible Reasoning Accordion   - Tool Approvals    |
+---------------------------------------------------------------------------------+
                                       ▲
                                       │ (Kotlin Flow<RuntimeEvent>)
+--------------------------------------┴------------------------------------------+
|                  MainViewModel & RuntimeBridge Implementation                   |
|                  (ClaudeRuntimeBridge / DshRuntimeBridge)                       |
+--------------------------------------┬------------------------------------------+
                                       │ (stdin writes / stdout line-buffered parser)
                                       ▼
+---------------------------------------------------------------------------------+
|               Linux Subsystem Process (PRoot Child via PTY/Pipes)               |
|                                                                                 |
|   +-----------------------+   +----------------------+   +------------------+   |
|   |   Claude Code CLI     |   | DeepSeek Harness CLI |   | Antigravity CLI  |   |
|   | (/usr/local/bin/claude|   | (/usr/local/bin/dsh) |   | (/root/.../agy)  |   |
|   +-----------------------+   +----------------------+   +------------------+   |
|               │                           │                                     |
|               ▼                           ▼                                     |
|   +--------------------------------------------------+                          |
|   | Local Loopback Compatibility Gateways            |                          |
|   | - LocalFormatGateway (Anthropic -> OpenAI SSE)   |                          |
|   | - OpenRouterRoutingGateway (Routing Policy)      |                          |
|   +--------------------------------------------------+                          |
|                                       │                                         |
|                                       ▼                                         |
|                      External LLM Provider API via HTTPS                        |
|              (Anthropic, DeepSeek, OpenRouter, NVIDIA, Custom)                  |
+---------------------------------------------------------------------------------+
```

---

## 2. Agent Execution Contracts & Command Line Arguments

Each agent is launched by its respective `RuntimeBridge` with strict command-line flags to force machine-readable, non-interactive streaming output:

### A. Claude Code (Anthropic)
* **Executable Path:** `/usr/local/bin/claude`
* **Source Reference:** `source/app/src/main/java/com/jarves/mh/runtime/RuntimeBridge.kt` (lines 113–117)
* **Exact Launch Command:**
  ```bash
  /usr/local/bin/claude \
      -p \
      --input-format stream-json \
      --output-format stream-json \
      --verbose
  ```
* **Why These Arguments?**
  - `-p` (Prompt mode): Prevents Claude Code from launching its full-screen interactive TUI (which fails on Mobile Harness's simple terminal).
  - `--input-format stream-json`: Instructs Claude to accept user turns and permission responses as single-line JSON objects piped to `stdin`.
  - `--output-format stream-json`: Forces Claude to emit all responses, thinking tokens, tool invocations, and file diffs as structured JSON streams on `stdout`.
  - `--verbose`: Ensures diagnostic progress and tool execution details are included in the event stream.

### B. DeepSeek Harness (`dsh`)
* **Executable Path:** `/usr/local/bin/dsh`
* **Source Reference:** `source/app/src/main/java/com/jarves/mh/runtime/DshRuntimeBridge.kt` (lines 30–37)
* **Exact Launch Command:**
  ```bash
  /usr/local/bin/dsh --profile headless "<prompt>"
  ```
* **Protocol Characteristics:**
  - One-shot headless mode: Output text prints to `stdout`.
  - Reasoning tokens stream under a distinct `dsh: reasoning:` prefix.
  - Failures emit `dsh: <CODE>: <message>`.
  - Exit code `0` signals completion.

### C. Google Antigravity CLI (`agy`)
* **Executable Path:** `/root/.local/bin/agy`
* **Source Reference:** `source/app/src/main/java/com/jarves/mh/runtime/AntigravityRuntimeBridge.kt`
* **Exact Launch Command:**
  ```bash
  /root/.local/bin/agy --mode stream --workspace /workspace "<prompt>"
  ```
* **Protocol Characteristics:**
  - Emits JSON events (`event: "step_update"`, `event: "tool_info"`).
  - Handles Google OAuth token refresh in the background via its internal token cache.

---

## 3. Passing Authentication & API Keys Securely

API keys and tokens are decrypted from the Android Keystore (`ApiKeyVault.kt`) only in memory immediately before process launch, and passed into the child process environment.

### Claude Code Environment Variables (`RuntimeLaunchConfigBuilder.kt`)

```kotlin
val environment = linkedMapOf(
    "DISABLE_AUTOUPDATER" to "1",
    "CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC" to "1",
    "DISABLE_TELEMETRY" to "1",
    "DISABLE_ERROR_REPORTING" to "1",
)
```

Depending on the active `ProviderProtocol`:

1. **Claude Subscription (OAuth):**
   ```text
   CLAUDE_CODE_OAUTH_TOKEN = <decrypted-oauth-token>
   ANTHROPIC_API_KEY = ""
   ANTHROPIC_AUTH_TOKEN = ""
   ```
   *Note:* `ANTHROPIC_API_KEY` is explicitly blanked so a previously used API key does not shadow the OAuth token.

2. **Official Anthropic API:**
   ```text
   ANTHROPIC_BASE_URL = https://api.anthropic.com
   ANTHROPIC_API_KEY = <decrypted-api-key>
   ANTHROPIC_MODEL = claude-sonnet-4-6
   ```

3. **OpenRouter:**
   ```text
   ANTHROPIC_BASE_URL = http://127.0.0.1:<gateway-port>
   OPENROUTER_API_KEY = <decrypted-key>
   ANTHROPIC_API_KEY = ""
   ANTHROPIC_MODEL = ~anthropic/claude-sonnet-latest
   ```

4. **DeepSeek / Third-Party Anthropic Gateways:**
   ```text
   ANTHROPIC_BASE_URL = https://api.deepseek.com/anthropic
   ANTHROPIC_API_KEY = <decrypted-deepseek-key>
   ANTHROPIC_MODEL = deepseek-v4-flash
   ```

---

## 4. Local Loopback Translation Gateways

Because Claude Code hardcodes the **Anthropic Messages Wire Protocol** (`/v1/messages`), it cannot natively query OpenAI-compatible APIs or apply custom routing headers. Mobile Harness solves this using two local background HTTP servers:

### A. `LocalFormatGateway.kt` (OpenAI Compatibility Gateway)
* **Port:** Ephemeral loopback port on `127.0.0.1` (`ServerSocket(0)`).
* **Role:** Intercepts Claude Code's requests to `http://127.0.0.1:PORT/v1/messages`.
* **Translation Flow:**
  1. Parses Claude's Anthropic JSON request: `{ "messages": [...], "system": "...", "tools": [...] }`.
  2. Converts message array into OpenAI Chat (`/v1/chat/completions`) or OpenAI Responses format.
  3. Forwards the translated request to the user's upstream endpoint (e.g. NVIDIA NIM, Moonshot, or self-hosted llama.cpp).
  4. Reads the upstream Server-Sent Events (SSE) stream (`data: {"choices": [{"delta": {"content": "..."}}]}`).
  5. Translates OpenAI SSE deltas back into Anthropic's expected format:
     ```json
     {"type":"content_block_delta","delta":{"type":"text_delta","text":"..."}}
     ```
  6. Sends the stream back to Claude Code.

### B. `OpenRouterRoutingGateway.kt` (Routing Policy Injector)
* **Port:** Ephemeral loopback port on `127.0.0.1`.
* **Role:** OpenRouter supports prioritizing specific model providers (e.g., Anthropic vs. AWS Bedrock vs. Google Vertex) via a `provider` body object. Because Claude Code's CLI does not expose custom body parameters, this proxy intercepts the request before it leaves the phone and injects:
  ```json
  "provider": {
      "order": ["Anthropic", "Together"],
      "allow_fallbacks": true
  }
  ```

---

## 5. Event Streaming & UI State Machine

`ClaudeRuntimeBridge` reads `stdout` line by line from the agent process and parses them into a Kotlin `Flow<RuntimeEvent>`:

```
[Agent stdout Line] ────► [JSON Parser] ────► [RuntimeEvent] ────► [StateFlow<AppUiState>]
```

### Event Mapping Lifecycle

| Agent JSON Output Type | Emitted `RuntimeEvent` | Chat UI Reaction |
| :--- | :--- | :--- |
| `{"type":"init"}` | `SessionStarted(sessionId)` | Clears stale turn buffers, activates foreground progress indicator. |
| `{"type":"text_delta","text":"..."}` | `AssistantDelta(text)` | Streams text directly into the active `ChatMessage` bubble. |
| `{"type":"thinking_delta","text":"..."}` | `ReasoningSummary(summary)` | Appends to collapsible reasoning accordion; updates token counter. |
| `{"type":"tool_use","name":"bash"}` | `ToolStarted(name, detail)` | Renders animated activity chip (`Bash: npm test`). |
| `{"type":"tool_result"}` | `ToolCompleted(name, summary)`| Replaces spinner with checkmark; displays execution timing. |
| `{"type":"permission_request"}` | `ToolRequested(request)` | **Halts execution.** Pops up interactive `ApprovalCard`. |
| `{"type":"error"}` | `SessionFailed(reason)` | Emits error banner with provider troubleshooting tips. |
| Process Exit 0 | `SessionCompleted` | Re-enables input field, saves chat to disk, updates git checkpoint. |

---

## 6. The Tool Approval Handshake Mechanism

To prevent an autonomous agent from accidentally destroying user data, Mobile Harness implements a synchronous approval loop:

1. **Trigger:** Claude Code decides to execute a potentially destructive tool (e.g. `Bash` command or `FileEdit`).
2. **Detection:** `ClaudeRuntimeBridge` intercepts a permission request line:
   ```json
   {"type":"permission_request","approval_id":"abc-123","tool":"bash","command":"rm -rf dist"}
   ```
3. **Card Display:** The bridge suspends the reading loop and emits `RuntimeEvent.ToolRequested`:
   - An `ApprovalCard` composable is displayed with a red **High Risk** badge and command preview.
4. **User Action:**
   - **Approved:** The user taps "Approve". `ClaudeRuntimeBridge.respondToApproval()` writes to Claude's `stdin`:
     ```json
     {"approval_id":"abc-123","approved":true}
     ```
   - **Rejected:** The user taps "Reject". The bridge writes:
     ```json
     {"approval_id":"abc-123","approved":false}
     ```
5. **Resume:** Claude Code reads the JSON approval on stdin and either executes the command or informs the user that the operation was canceled.
