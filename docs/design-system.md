# PocketForge AI Design System (`ForgeDesign`)

## 1. Design Philosophy & Architectural Principles
The **ForgeDesign** system is engineered specifically for autonomous AI development on mobile devices. It transforms Mobile Harness from an engineering prototype with a crowded, orange-accented interface into a consumer-grade, distraction-free IDE experience.

### Core Pillars
1. **Simplicity First (Dual Modes):**
   - **Simple Mode (Default):** Natural language interface, single primary action per screen, no Linux/PRoot jargon, guided setup, 1-tap quick actions.
   - **Pro Mode:** Exposes full PTY terminal emulator, raw runtime logs, process monitors, and low-level subsystem configurations.
2. **True AMOLED & Deep Obsidian Focus:**
   - 100% true black (`#000000`) for OLED power savings during multi-hour background tasks.
   - Deep Obsidian (`#0D0F12`) for high-contrast dark mode.
   - Pristine Light (`#F8F9FA`) with slate neutrals.
3. **Confident, Restrained Color Palette:**
   - Moving away from muddy orange to an **Electric Indigo (`#6366F1`)** and **Cyber Cyan (`#06B6D4`)** accent system.
4. **Thumb-Driven Ergonomics:**
   - 48dp minimum touch targets.
   - Bottom navigation bars, modal bottom sheets for selections, and essential inputs situated in the thumb comfort zone.
5. **Adaptive Across Form Factors:**
   - Responsive layouts optimized for standard phones, foldables (unfolded 8:7 ratio), and tablets (split dual-pane editor/chat).
6. **Zero Emoji in User Interface:**
   - All visual controls and status markers use the open-source **Phosphor Icons** (MIT) family exclusively. Zero emoji or plain Unicode symbols are used as icons.

---

## 2. Color Palette & Semantic Tokens

### 2.1 Theme Swatches

| Semantic Role | Obsidian Dark (`dark`) | AMOLED Black (`amoled`) | Clean Light (`light`) |
| :--- | :--- | :--- | :--- |
| **App Background** | `#0D0F12` | `#000000` | `#F8F9FA` |
| **Surface (Cards, Bars)**| `#16191E` | `#0A0A0A` | `#FFFFFF` |
| **Surface Elev. (Modals)**| `#1E232B` | `#141414` | `#FFFFFF` |
| **Border / Divider** | `#262B35` | `#222222` | `#E2E8F0` |
| **Text Primary** | `#F1F5F9` | `#FFFFFF` | `#0F172A` |
| **Text Secondary** | `#94A3B8` | `#A1A1AA` | `#64748B` |
| **Text Tertiary / Muted**| `#64748B` | `#71717A` | `#94A3B8` |

### 2.2 Brand & Status Tokens

| Token Name | Hex Code | Purpose / Usage |
| :--- | :--- | :--- |
| `primary` (Electric Indigo) | `#6366F1` | Primary CTA buttons, active tabs, focused inputs |
| `primaryHover` | `#4F46E5` | Pressed/active interactive state |
| `accent` (Cyber Cyan) | `#06B6D4` | Agent thinking badges, streaming indicators, live stats |
| `success` (Emerald) | `#10B981` | Completed builds, verified API keys, running servers |
| `warning` (Amber) | `#F59E0B` | Rate limits, battery saver alerts, pending approvals |
| `error` (Rose) | `#EF4444` | Build errors, failed connections, crash cards |
| `terminalBg` | `#07080A` | Dedicated high-contrast monospace terminal background |

---

## 3. Typography & Multi-Language Support (English + Bengali)
To ensure pristine legibility across English, technical code, and Bengali, ForgeDesign enforces a multi-font fallback stack:
- **Latin Prose:** **Inter** (or Android system Roboto)
- **Bengali Prose:** **Noto Sans Bengali** (open-source by Google)
- **Code & Monospace:** **JetBrains Mono**

### Font Fallback Stack
```css
/* UI Prose Fallback */
font-family: "Inter", "Noto Sans Bengali", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;

/* Monospace & Terminal Fallback */
font-family: "JetBrains Mono", Consolas, "Courier New", monospace;
```

### Typography Scale Table

| Scale Token | Font Stack | Size | Weight | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `Display` | Inter / Noto Sans Bengali | 24sp | Bold (700) | 32sp | Hero greetings, welcome screen |
| `TitleLarge` | Inter / Noto Sans Bengali | 18sp | SemiBold (600) | 24sp | Screen headers, modal titles |
| `TitleMedium`| Inter / Noto Sans Bengali | 16sp | SemiBold (600) | 22sp | Card titles, project names |
| `BodyLarge` | Inter / Noto Sans Bengali | 15sp | Regular (400) | 22sp | Chat messages, markdown prose |
| `BodyMedium` | Inter / Noto Sans Bengali | 13sp | Regular (400) | 18sp | Subtitles, helper text |
| `LabelSmall` | Inter / Noto Sans Bengali | 11sp | Medium (500) | 14sp | Badges, timestamps, tags |
| `CodeEditor` | JetBrains Mono | 13sp | Regular (400) | 20sp | Code editor text, file viewer |
| `Terminal` | JetBrains Mono | 12sp | Medium (500) | 16sp | PTY terminal, logs, diffs |

---

## 4. Brand Logo & Launcher Icon System
PocketForge AI features a custom geometric monogram combining an outer hexagonal code-bracket frame with an interlocking "P" and "F" anvil motif and an autonomous cyber-cyan spark node.

### Vector Assets Created (`docs/mockups/assets/`)
1. `logo.svg`: Full-color brand emblem with Electric Indigo and Cyber Cyan gradients.
2. `logo-dark.svg`: High-contrast version optimized for Obsidian and AMOLED black backgrounds.
3. `logo-light.svg`: Slate and deep indigo version optimized for Clean Light mode.
4. `logo-monochrome.svg`: Single-color flat vector mark suitable for status bar notifications.
5. `adaptive-icon.svg`: Android Adaptive Icon format (108x108 viewport with 72x72 safe circle).

---

## 5. Premium Icon Standard: Phosphor Icons (MIT)
All interface icons are sourced strictly from the **Phosphor Icons** library.
- Base touch target: 48dp
- Icon display size: 24dp (primary controls), 20dp (compact tags/chips)
- Default style: `regular` (stroke)
- Active/selected style: `fill`
- Conversion to Android: Phosphor SVGs are converted to Android `VectorDrawable` XML files located in `app/src/main/res/drawable/` and consumed via standard Jetpack Compose `Icon()` elements.
- Full catalog documented in `docs/icon-map.md`.

---

## 6. Proposed New Features (Post-Parity Roadmap)

| Feature | Description | Difficulty | Priority | On-Device Feasibility & Limitations |
| :--- | :--- | :--- | :--- | :--- |
| **Command Palette & Global Search** | Quick modal (Ctrl+K / Cmd+K or tap search bar) to jump across files, chats, settings, and commands. | **Medium** | **High** | Fully feasible on-device using in-memory file index and Kotlin coroutines. |
| **Project Templates Picker** | Starter wizards for Static Web, Vite/React, Node API, Python, Flask, and Android Compose. | **Easy** | **High** | Fully feasible; reuses existing `AndroidProjectTemplateGenerator` and offline zip templates. |
| **Git Management Panel** | Visual panel showing git status, diff viewer, commit form, branch switcher, and push/pull. | **Medium** | **High** | Fully feasible; uses standard `git` CLI installed in Ubuntu subsystem. |
| **Prompt Library & Rules Editor** | Saved prompt snippets and a dedicated editor for project-specific agent instruction files (`CLAUDE.md`, rules). | **Easy** | **Medium** | Fully feasible; stored in local SQLite / DataStore and project root. |
| **Agent Switcher & Token/Cost Tracker** | Switch agent per conversation thread; track token consumption and estimated API costs per provider. | **Medium** | **High** | Fully feasible; streams token usage from agent deltas and computes cost locally. |
| **Voice Input for Prompts** | Voice dictation button in chat input bar powered by Android's built-in `SpeechRecognizer` API. | **Easy** | **Medium** | Fully feasible; uses native offline/online Android speech recognition engine. |
| **Background Tasks Center** | Dedicated drawer showing all running agents, web dev servers, and Gradle builds with pause/kill actions. | **Medium** | **High** | Fully feasible; queries `RuntimeExecutionService` task registry. |
| **Port Manager & 1-Tap Preview** | Scans and lists all active TCP listening ports in PRoot (`netstat -tuln`) with 1-tap "Open in WebView". | **Easy** | **High** | Fully feasible; reuses `LocalNetwork.scanActivePorts()`. |
| **Project ZIP Import/Export** | Export entire project folder to a compressed `.zip` file; import external zip into workspace. | **Easy** | **High** | Fully feasible; uses standard `java.util.zip` and Android Storage Access Framework (SAF). |
| **Biometric Key Vault Lock** | Hardware biometric prompt (Fingerprint / Face Unlock via `BiometricPrompt`) to view/edit Keystore API keys. | **Medium** | **High** | Fully feasible; standard Android `BiometricManager` API. |
| **MCP Servers Manager** | UI to inspect and configure Model Context Protocol (MCP) servers for agents that support MCP. | **Hard** | **Medium** | *UNVERIFIED per agent:* Claude Code and Claude Desktop support MCP natively via JSON config (`~/.claude/config.json`), while DeepSeek Harness requires verification. Can be tested by spawning a sample local MCP stdio server in PRoot. |
| **Hardware Keyboard & Tablet Two-Pane** | Physical keyboard shortcuts (Ctrl+S save, Ctrl+P search, Ctrl+` terminal) and adaptive side-by-side tablet layout. | **Medium** | **High** | Fully feasible; Jetpack Compose `BoxWithConstraints` and `onKeyEvent`. |
| **Appearance & Haptics Settings** | Accent color customization (Indigo, Cyan, Emerald, Rose), font size scaling, haptic feedback toggle, reduced motion. | **Easy** | **Medium** | Fully feasible; Compose theme tokens and `Vibrator` service. |
| **App Shortcuts & Quick Settings Tile** | Android Home-screen app shortcut and System Quick Settings pull-down tile to launch "New Chat" instantly. | **Easy** | **Medium** | Fully feasible; standard Android `TileService` and dynamic `ShortcutManager`. |
| **Crash & Diagnostic Report Export** | 1-tap export of sanitized system diagnostics (CPU, RAM, OS, logcat, no API keys) for bug reports. | **Easy** | **Medium** | Fully feasible; packages sanitized logs into a shareable text file. |
