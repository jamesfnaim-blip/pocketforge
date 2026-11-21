# PocketForge AI Design System (`ForgeDesign`)

## 1. Design Philosophy & Goals
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

## 3. Typography Scale
ForgeDesign utilizes **Inter** (or Android system Roboto) for UI prose and **JetBrains Mono** for code, terminal, diffs, and logs.

| Scale Token | Font | Size | Weight | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `Display` | Inter | 24sp | Bold (700) | 32sp | Hero greetings, welcome screen |
| `TitleLarge` | Inter | 18sp | SemiBold (600) | 24sp | Screen headers, modal titles |
| `TitleMedium`| Inter | 16sp | SemiBold (600) | 22sp | Card titles, project names |
| `BodyLarge` | Inter | 15sp | Regular (400) | 22sp | Chat messages, markdown prose |
| `BodyMedium` | Inter | 13sp | Regular (400) | 18sp | Subtitles, helper text |
| `LabelSmall` | Inter | 11sp | Medium (500) | 14sp | Badges, timestamps, tags |
| `CodeEditor` | JetBrains Mono | 13sp | Regular (400) | 20sp | Code editor text, file viewer |
| `Terminal` | JetBrains Mono | 12sp | Medium (500) | 16sp | PTY terminal, logs, diffs |

---

## 4. Spacing, Radii & Elevation Grid
- **4dp Baseline Grid:** All margins, paddings, and component dimensions are multiples of 4:
  - `space-xxs`: 4dp
  - `space-xs`: 8dp
  - `space-sm`: 12dp
  - `space-md`: 16dp (standard screen padding)
  - `space-lg`: 24dp
  - `space-xl`: 32dp
- **Corner Radii:**
  - `radius-sm`: 6dp (chips, badges, code blocks)
  - `radius-md`: 12dp (buttons, cards, text fields)
  - `radius-lg`: 18dp (dialogs, bottom sheets)
  - `radius-full`: 999dp (pills, avatars, status dots)
- **Elevation / Outlines:**
  - Subtle 1dp solid borders (`border-subtle`: `#262B35` / `#222222`) preferred over heavy drop-shadows for sharp mobile readability.

---

## 5. Core Component Library

### 5.1 Primary & Secondary Buttons
- **Primary Button:** 52dp height, full-width or pill, `primary` background (`#6366F1`), bold white text, subtle press-scale animation.
- **Secondary / Ghost Button:** 44dp height, outline border, surface background, subtle ripple.

### 5.2 Chat Bubbles & Reasoning Cards
- **User Bubble:** Right-aligned, dark indigo tint (`#1E1B4B`), clear padding, rounded with bottom-right notch.
- **Agent Bubble:** Left-aligned, surface card (`#16191E`), Markdown rendering, syntax-highlighted code blocks with 1-tap "Copy" and "Insert into Editor".
- **Collapsible Reasoning ("Thinking"):** Cyan border (`#06B6D4`), pulsing dot, collapsed by default into a 1-line chip ("Thinking for 4s..."), expandable to reveal step-by-step agent thoughts.

### 5.3 Tool Approval Cards
- Dedicated card appearing inline when the agent wants to execute a command or modify files:
  - Header: Tool name (`execute_command`, `write_file`) + risk level badge.
  - Body: Command string or expandable unified diff.
  - Actions: 2 large buttons: **Approve (`#10B981`)** vs **Reject (`#EF4444`)**.

### 5.4 Terminal Key Bar
- Fixed horizontal scroll bar sitting right above the mobile keyboard:
  - Sticky modifier keys: `ESC`, `TAB`, `CTRL`, `ALT`.
  - Directional arrows: `▲`, `▼`, `◀`, `▶`.
  - Common shortcuts: `~`, `/`, `-`, `|`, `PIPE`, `^C`, `^D`.

### 5.5 Actionable Error Cards
- Never throw raw stack traces or silent failures to users:
  - Top icon + clear plain-language title (e.g., *"Cannot connect to Anthropic"*).
  - One-line explanation (e.g., *"Your API key is missing or invalid"*).
  - Action button: *"Fix in Settings"* or *"Retry"*.

---

## 6. The 11 Screen Specifications
1. **Welcome + One-Flow Setup:** Language selector, device architecture check, 1-tap workspace download, API key entry with live test button, battery exemption wizard.
2. **Home Screen:** Greeting, prominent "Start New Task" button, Quick Actions carousel, running services status widget, recent projects list.
3. **Chat / Agent Screen:** Interactive conversation, collapsible tool reasoning, active tool chips, input bar with attachment button, voice/image integration.
4. **Project Screen:** Tabs for Chat, Files, Live Preview, and Terminal (Pro mode).
5. **Files & Editor:** File hierarchy tree with icons, multi-file tabs, breadcrumbs, search, syntax highlighting, line numbers.
6. **Live Preview:** Built-in web browser viewing local server (e.g., `localhost:3000`), URL bar, reload, console logs drawer.
7. **Interactive Terminal:** True PTY Linux session, custom monospace font, ANSI color support, thumb-friendly sticky key bar.
8. **Android Build Dashboard:** Target project, one-click "Build APK", stage timeline (Clean -> AAPT2 -> D8/R8 -> Sign), readable error breakdown, direct APK installer.
9. **Settings Screen:** Grouped categories (AI Providers, Agents, Appearance, Languages, Backup, Subsystem).
10. **Developer & Credits Screen:** Developer card for **Nayem Hossain** with biography and links, legal attribution to Mobile Harness (MIT), PRoot, talloc, libandroid-shmem, and bundled licenses viewer.
11. **Background & Battery Help Screen:** Automatic OEM detection (Samsung OneUI, Xiaomi MIUI/HyperOS, etc.) with step-by-step visual toggle guides to disable battery killing.
