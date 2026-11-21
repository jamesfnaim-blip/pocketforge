# Design System & UI/UX Architecture

**Product Name:** PocketForge AI  
**Design Paradigm:** Material 3 Adaptive Expressive  
**Target Form Factors:** Smartphones, Foldables, Tablets (portrait & landscape)  

---

## 1. Honest Critique of the Original UI/UX

While Mobile Harness established a brilliant architectural proof-of-concept, an objective design and ergonomics audit reveals severe usability flaws:

1. **Monolithic UI Anti-Pattern:**
   - Over 7,000 lines of Compose code bundled in a single file (`PocketDevApp.kt`). This resulted in state spaghetti, janky recompilations, and inconsistent padding and elevation across different dialogs.
2. **Jargon-Heavy Onboarding for Beginners:**
   - The first-run flow bombarded non-coders with terms like *"PRoot Subsystem"*, *"Rootfs Tarball"*, *"ABI Check"*, and *"AAPT2 Override"*. A beginner has no idea what these mean and immediately feels intimidated.
3. **The "Pseudo-Terminal" Frustration:**
   - The terminal appeared to be a real command line, but was actually a static text view. When users ran `vim`, `nano`, or `htop`, the screen scrambled or stopped responding, creating deep confusion and broken expectations.
4. **Cramped Mobile Touch Targets:**
   - Virtual keyboard helper keys (<kbd>ALT</kbd>, <kbd>CTRL</kbd>, <kbd>ESC</kbd>) had insufficient padding and were tightly squeezed against the bottom system gesture bar, causing frequent accidental exits to the Android home screen.
5. **No Visual Code Editor:**
   - The built-in file viewer was an unstyled text box. Users had no tabs, no multi-file editing, no code folding, and no syntax autocomplete.
6. **Orange-Dominant Theme without True AMOLED:**
   - The dark mode used a muddy dark-slate background (`#090D14`) heavily tinted with aggressive orange accents, which burned power on OLED screens compared to true `#000000` black.

---

## 2. The New Design System (ForgeDesign)

### A. Color Palette Matrix

The new design system adopts a sophisticated, developer-focused aesthetic: **Deep Obsidian** with vibrant, high-contrast neon accents for status indicators.

```
+───────────────────────────────────────────────────────────────────────────────+
|                           POCKETFORGE COLOR PALETTE                           |
+───────────────────┬───────────────────┬───────────────────┬───────────────────+
| Role              | Light Mode        | Dark Mode (Slate) | AMOLED Mode       |
+───────────────────┼───────────────────┼───────────────────┼───────────────────+
| Background        | #F8FAFC (Slate50) | #0B0F17 (DarkBlue)| #000000 (PureBlack|
| Surface Container | #FFFFFF (White)   | #151D2A (CardBlue)| #111111 (OLED Card|
| Surface Higher    | #F1F5F9 (Slate100)| #1E293B (Hover)   | #1C1C1C (Elevated)|
| Primary Accent    | #3B82F6 (Blue 500)| #60A5FA (Blue 400)| #38BDF8 (Sky 400) |
| Secondary Accent  | #8B5CF6 (Violet)  | #A78BFA (Violet)  | #A78BFA (Violet)  |
| Success / Prompt  | #10B981 (Emerald) | #34D399 (Emerald) | #34D399 (Emerald) |
| Warning / Risk    | #F59E0B (Amber)   | #FBBF24 (Amber)   | #FBBF24 (Amber)   |
| Error / Destruct  | #EF4444 (Red 500) | #F87171 (Red 400) | #F87171 (Red 400) |
| Text Primary      | #0F172A (Slate900)| #F8FAFC (Slate50) | #FFFFFF (PureWhite|
| Text Secondary    | #64748B (Slate500)| #94A3B8 (Slate400)| #A1A1AA (Zinc 400)|
| Outline / Border  | #E2E8F0 (Slate200)| #334155 (Slate700)| #27272A (Zinc 800)|
+───────────────────┴───────────────────┴───────────────────┴───────────────────+
```

### B. Typography Scale (8-Point Modular Grid)
- **Primary Font Family:** Google Inter / Roboto (clean, ultra-legible humanist sans-serif).
- **Code & Terminal Font:** JetBrains Mono or Fira Code with full programming ligature support.

| Style Name | Font Size | Line Height | Font Weight | Usage |
| :--- | :--- | :--- | :--- | :--- |
| **Headline Large** | 28 sp | 34 sp | Bold (700) | Main screen hero titles |
| **Title Medium** | 18 sp | 24 sp | Semi-Bold (600) | Project cards, section headers |
| **Body Large** | 15 sp | 22 sp | Regular (400) | Chat bubbles, explanations |
| **Body Code** | 13 sp | 19 sp | Medium (500) | Terminal lines, code snippets |
| **Label Small** | 11 sp | 14 sp | Bold (700) | Badges, chip tags, timers |

### C. Spacing Scale & Corner Radii
- **Spacing Grid:** Based strictly on multiples of `4 dp`:
  `4dp (micro)`, `8dp (compact)`, `12dp (standard)`, `16dp (card padding)`, `24dp (section)`, `32dp (screen edges)`.
- **Corner Radii:**
  - Small chips & buttons: `8 dp`
  - Cards & modals: `16 dp`
  - Bottom sheets & major surfaces: `24 dp`
  - Avatars & status pills: `999 dp (Full Circle)`

### D. Motion & Animation Physics
- **Spring Physics for Sheets:** Bottom sheets and modals use damped spring animations (`stiffness = Spring.StiffnessMediumLow`, `dampingRatio = Spring.DampingRatioLowBouncy`) for an organic, tactile mobile feel.
- **Accordion Reasoning:** Expanding AI "Thinking" boxes smoothly animates content height with `tween(durationMillis = 240, easing = FastOutSlowInEasing)`.
- **Live Pulsing Dot:** The "Agent Thinking" indicator gently pulses scale (`0.85f -> 1.15f`) and alpha to signal active reasoning without annoying flickers.

---

## 3. Screen-by-Screen Layouts & ASCII Wireframes

### Screen 1: Zero-Friction Beginner Onboarding
**Goal:** Guide a non-coder from clean slate to running their first project in under 2 minutes.

```text
+-------------------------------------------------------------+
|  [ 1/3: Welcome to PocketForge ]                            |
|                                                             |
|           ⚡ POCKETFORGE AI                                  |
|     Your Personal AI Software Lab                           |
|                                                             |
|  Build websites, mobile apps, and scripts on your phone.    |
|                                                             |
|  Choose your language:                                      |
|  [ (•) English       ( ) বাংলা        ( ) Español ]          |
|                                                             |
|  ---------------------------------------------------------  |
|  Step 1: One-Tap Linux Environment                          |
|  [✓] 64-bit Phone Detected (Snapdragon 8 Gen 2 / 12 GB RAM) |
|  [✓] Background Battery Exemption (Granted)                 |
|                                                             |
|  Ready to set up your private workspace (approx. 400 MB):   |
|                                                             |
|  +-------------------------------------------------------+  |
|  |             [ INSTALL EVERYTHING (1-TAP) ]            |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  Step 2: Connect Your AI Brain                              |
|  Select AI Provider:                                        |
|  [ Claude Code ]   [ DeepSeek (Recommended) ]   [ Free Trial ]
|                                                             |
|  Enter API Key: [ ************************************ ] [📋]|
|  [✓ Key Verified: DeepSeek-V3 Active]                       |
|                                                             |
|                      [ START BUILDING -> ]                  |
+-------------------------------------------------------------+
```

---

### Screen 2: Projects Dashboard & Quick Chat

```text
+-------------------------------------------------------------+
|  ⚡ PocketForge           [🔋 92%] [● Background Active] [⚙] |
+-------------------------------------------------------------+
|  🔍 Search projects or commands...                          |
|                                                             |
|  +-------------------------------------------------------+  |
|  |  🚀 QUICK CHAT                                        |  |
|  |  Instant scratchpad. No setup required.               |  |
|  |  [ Start Instant AI Session ]                         |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  YOUR PROJECTS (3)                       [ + New Project ]  |
|                                                             |
|  +-------------------------------------------------------+  |
|  |  📁 Portfolio-Website                  [🌐 Server: 5173]|  |
|  |  Responsive dark-mode developer portfolio.            |  |
|  |  Stack: HTML/Vite · Updated 12m ago                   |  |
|  +-------------------------------------------------------+  |
|  |  📱 ExpenseTracker                     [🔨 APK Ready ]  |  |
|  |  Android Jetpack Compose personal finance app.         |  |
|  |  Stack: Android/Kotlin · Updated 2h ago               |  |
|  +-------------------------------------------------------+  |
|                                                             |
+-------------------------------------------------------------+
|  [📁 Projects]    [💬 Active Chat]    [💻 Terminal]   [⚙ Settings]|
+-------------------------------------------------------------+
```

---

### Screen 3: The Main Workspace (Chat Tab)

```text
+-------------------------------------------------------------+
|  <- Portfolio-Website               [🌐 Preview (5173)] [⋮] |
+-------------------------------------------------------------+
|  [👤 User]                                                  |
|  Add a pricing table with 3 tiers (Free, Pro, Enterprise)   |
|  and make the Pro tier highlighted in neon blue.            |
|                                                             |
|  [🤖 Claude Code · DeepSeek-V3]                             |
|  +-------------------------------------------------------+  |
|  |  💡 Thought Process (2,450 tokens)               [ v ]|  |
|  |  1. Inspecting index.html structure...                |  |
|  |  2. Designing CSS grid for 3 pricing cards...          |  |
|  +-------------------------------------------------------+  |
|  |  ⚙️ Activity: [Edit index.html]  [Edit styles.css]     |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  I have generated the pricing table and styled the "Pro"    |
|  card with a vibrant neon blue border. Check preview!       |
|                                                             |
|  +-------------------------------------------------------+  |
|  | ⚠️ APPROVAL REQUIRED: Shell Command                      |  |
|  | Command: `npm run build`                                |  |
|  | Risk: SAFE                                            |  |
|  |          [ Reject ]         [ Approve & Run ]         |  |
|  +-------------------------------------------------------+  |
|                                                             |
|  +-------------------------------------------------------+  |
|  | Ask agent to build, edit, or fix...            [📎] [>]|  |
|  +-------------------------------------------------------+  |
+-------------------------------------------------------------+
|  [💬 Chat]  [📝 Code (VS Code)]  [💻 Terminal]  [🌐 Preview]  |
+-------------------------------------------------------------+
```

---

### Screen 4: In-App VS Code Editor (OpenVSCode Server WebView)

```text
+-------------------------------------------------------------+
|  <- Portfolio-Website       [index.html*] [styles.css]  [▶] |
+-------------------------------------------------------------+
| 1  | <!DOCTYPE html>                                        |
| 2  | <html lang="en">                                       |
| 3  | <head>                                                 |
| 4  |   <meta charset="UTF-8">                               |
| 5  |   <title>My Portfolio</title>                          |
| 6  |   <link rel="stylesheet" href="styles.css">            |
| 7  | </head>                                                |
| 8  | <body>                                                 |
| 9  |   <div class="pricing-grid">                           |
| 10 |     <div class="card pro-tier">                        |
| 11 |       <h3>Professional Plan</h3>                       |
| 12 |       <span class="price">$19/mo</span>                |
| 13 |     </div>                                             |
| 14 |   </div>                                               |
| 15 | </body>                                                |
| 16 | </html>                                                |
|                                                             |
+-------------------------------------------------------------+
| [TAB] [CTRL] [ALT] [ { ] [ } ] [ ( ] [ ) ] [ < ] [ > ] [ / ]|
+-------------------------------------------------------------+
|  [💬 Chat]  [📝 Code (VS Code)]  [💻 Terminal]  [🌐 Preview]  |
+-------------------------------------------------------------+
```

---

### Screen 5: True Interactive Terminal Tab

```text
+-------------------------------------------------------------+
|  <- Portfolio-Website: Terminal (bash)          [Stop] [Trash]|
+-------------------------------------------------------------+
| root@pocketforge:~/workspace/portfolio# htop                |
|                                                             |
|  CPU[|||||||||||||||||||||||                  42.8%] Tasks: |
|  Mem[|||||||||||||||||||||||||||||||||||  1.8G/11.4G] 4 run |
|  Swp[                                         0K/4.0G]      |
|                                                             |
|    PID USER      PRI  NI  VIRT   RES   SHR S CPU% MEM% TIME+|
|   2841 root       20   0  482M  128M   32M S 18.2  1.1  0:12|
|   2890 root       20   0  1.1G  240M   48M S 14.1  2.1  0:24|
|                                                             |
| F1Help  F2Setup F3Search F4Filter F5Tree  F9Kill  F10Quit   |
+-------------------------------------------------------------+
| [ESC] [TAB] [CTRL] [ALT] [ | ] [ - ] [ ~ ] [ / ] [↑] [↓] [←]|
+-------------------------------------------------------------+
|  [💬 Chat]  [📝 Code (VS Code)]  [💻 Terminal]  [🌐 Preview]  |
+-------------------------------------------------------------+
```

---

### Screen 6: Developer Profile & Credits Screen

```text
+-------------------------------------------------------------+
|  <- Settings / Developer & Credits                          |
+-------------------------------------------------------------+
|                                                             |
|           [  Developer Profile Picture  ]                   |
|                    {YOUR_NAME}                              |
|        Full-Stack & Mobile Systems Enthusiast               |
|                                                             |
|  "Empowering everyone to build software anywhere, anytime." |
|                                                             |
|  🌐 Website:  {YOUR_WEBSITE}                                |
|  💼 LinkedIn: {YOUR_LINKEDIN}                               |
|  📦 App Version: 1.0.0 (Build 100)                          |
|                                                             |
|  ---------------------------------------------------------  |
|  OPEN-SOURCE CREDITS & ACKNOWLEDGMENTS                      |
|                                                             |
|  This product is built on the pioneering research and open- |
|  source architecture of:                                    |
|                                                             |
|  ⭐ Mobile Harness (MIT License)                            |
|     Copyright (c) 2026 Mobile Harness Contributors          |
|     [ View Mobile Harness on GitHub ]                       |
|                                                             |
|  ⭐ PRoot Project (GPL-2.0 License)                         |
|     Rootless userspace chroot & bind mount engine.          |
|                                                             |
|  ⭐ libandroid-shmem (BSD-3-Clause)                         |
|     Android System V shared memory shims.                   |
|                                                             |
|  ⭐ Samba talloc (LGPL-3.0+)                                |
|     Hierarchical memory allocator.                          |
|                                                             |
|  [ View Complete Third-Party License Notices ]              |
+-------------------------------------------------------------+
```

---

## 4. Tablet & Foldable Split-Pane Adaptive Layout

When running on an Android tablet, Samsung Galaxy Fold (unfolded), or desktop DEX mode:
- **Dual-Pane Workstation:** Chat interface occupies the left 40% pane; the full VS Code editor or terminal occupies the right 60% pane.
- Eliminates the need to switch tabs constantly while working with the AI agent.

```text
+-----------------------------------┬-----------------------------------------+
| CHAT ASSISTANT (40% Width)        | LIVE VS CODE / PREVIEW (60% Width)      |
+-----------------------------------┼-----------------------------------------+
| [👤 User]                         | index.html                              |
| Build an interactive seat map for | --------------------------------------- |
| a movie theater booking app.      | <div class="cinema-container">          |
|                                   |   <div class="screen">SCREEN</div>      |
| [🤖 Claude Code]                  |   <div class="row">                     |
| Generated seat layout component!  |     <div class="seat occupied"></div>   |
| Streaming changes to editor...    |     <div class="seat selected"></div>   |
|                                   |   </div>                                |
| [Ask follow-up...]            [>] | </div>                                  |
+-----------------------------------┴-----------------------------------------+
```
