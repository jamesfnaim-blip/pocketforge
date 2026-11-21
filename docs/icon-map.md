# PocketForge AI Icon Map & System Specification

## 1. Icon Standard: Phosphor Icons (MIT)
PocketForge AI adopts **Phosphor Icons** (MIT License) as its sole, universal icon family across both the web mockup and the native Android Compose application.

### Strict Rules
1. **Zero Emoji as UI Icons:** No emoji characters (e.g. lightning, gear, chat, folder, battery, hammer, cart) or plain Unicode symbols (e.g. arrows, checks, crosses) are used as interface icons. Every visual indicator is a vector asset.
2. **Unified Stroke & Grid:** All icons are designed on a **24x24dp grid** (rendered at 24dp for primary controls and 20dp for compact inline tags/chips) with a consistent 2dp stroke width.
3. **Weight Variants:**
   - **Regular (Stroke):** Used by default for idle, inactive, and standard navigation items.
   - **Fill:** Used exclusively for active, selected, or pinned states (e.g. active tab in bottom navigation, bookmarked file).

---

## 2. Icon Inventory & Usage Mapping

| Icon Name | Phosphor ID | Weight (Default / Active) | Screen / Component Location | Functional Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **House** | `ph-house` | Regular / Fill | Bottom Navigation, Home Screen | Return to main dashboard |
| **ChatCircleDots** | `ph-chat-circle-dots` | Regular / Fill | Bottom Navigation, Chat Tab | Open AI Agent chat interface |
| **FolderSimple** | `ph-folder-simple` | Regular / Fill | Bottom Navigation, Project Tab | Access workspace projects and file tree |
| **Terminal** | `ph-terminal` | Regular / Fill | Bottom Navigation, Terminal Tab | Linux PRoot interactive shell |
| **GearSix** | `ph-gear-six` | Regular / Fill | Bottom Navigation, Header | Access application settings |
| **Play** | `ph-play` | Regular / Fill | Quick Actions, Run Server | Execute scripts or launch background tasks |
| **Plus** | `ph-plus` | Regular | Home Screen, Project Screen | Create new project, file, or chat |
| **Paperclip** | `ph-paperclip` | Regular | Chat Input Bar | Open file / image attachment sheet |
| **Camera** | `ph-camera` | Regular | Chat Attachment Sheet | Capture photo via camera for agent analysis |
| **Image** | `ph-image` | Regular | Chat Attachment Sheet | Pick image from device storage |
| **PaperPlaneRight** | `ph-paper-plane-right` | Regular / Fill | Chat Input Bar | Send message / prompt to agent |
| **Sparkle** | `ph-sparkle` | Regular / Fill | Reasoning Chip, Agent Status | Indicates autonomous AI generation & thinking |
| **CheckCircle** | `ph-check-circle` | Regular / Fill | Hardware Preflight, Approval Card | Success state, approve action, verified key |
| **XCircle** | `ph-x-circle` | Regular / Fill | Error Cards, Tool Approval Card | Rejection action, build failure, network error |
| **WarningCircle** | `ph-warning-circle` | Regular / Fill | Tool Approval Card, Battery Alerts | High-risk confirmation, rate-limit warnings |
| **GitBranch** | `ph-git-branch` | Regular | Project Header, Git Panel | Git branch indicator, switch branch |
| **GitCommit** | `ph-git-commit` | Regular | Git Panel, Checkpoints | View and create git commits / undo checkpoints |
| **ArrowClockwise** | `ph-arrow-clockwise` | Regular | Web Preview, Terminal, Updates | Refresh browser, restart session, check updates |
| **ArrowSquareOut** | `ph-arrow-square-out`| Regular | Live Preview, External Links | Open in standalone browser or system viewer |
| **Hammer** | `ph-hammer` | Regular | Android Builder Dashboard | Initiate on-device Gradle build |
| **DownloadSimple** | `ph-download-simple`| Regular | DevStack Manager, Rootfs Installer| Download toolchains, bundles, or APKs |
| **UploadSimple** | `ph-upload-simple` | Regular | Backup & Restore, File Upload | Upload files, export project archive |
| **Trash** | `ph-trash` | Regular | File Actions, Chat List | Delete file, directory, or conversation |
| **PencilSimple** | `ph-pencil-simple` | Regular | File Editor, Rename Project | Edit code file or rename entity |
| **MagnifyingGlass** | `ph-magnifying-glass` | Regular | Command Palette, Global Search | Trigger search across files and commands |
| **Cpu** | `ph-cpu` | Regular | Hardware Check, Health Monitor | Device CPU architecture indicator |
| **HardDrive** | `ph-hard-drive` | Regular | Storage Check, DevStacks | Disk storage utilization indicator |
| **BatteryCharging** | `ph-battery-charging`| Regular | Battery Setup Wizard | Battery optimization and power management |
| **LockKey** | `ph-lock-key` | Regular / Fill | Key Vault, App Lock | Hardware Keystore and biometric security |
| **UserCircle** | `ph-user-circle` | Regular / Fill | Developer & Credits Screen | Developer profile and author card |
| **Globe** | `ph-globe` | Regular | Language Selector, Live Preview | System language and network domains |
| **ShieldCheck** | `ph-shield-check` | Regular | Licenses, Antigravity Auth | Open-source compliance & OAuth security |
| **Code** | `ph-code` | Regular | Code Editor, Project Types | Source code editing mode |
| **FileCode** | `ph-file-code` | Regular | File Tree Items | Code files (.ts, .kt, .py, .c) |
| **FileText** | `ph-file-text` | Regular | File Tree Items | Documentation and text files (.md, .txt) |
| **Fingerprint** | `ph-fingerprint` | Regular | Biometric Lock Screen | Biometric fingerprint authentication |
| **Bell** | `ph-bell` | Regular / Fill | Notification Center, FGS | Foreground service notification controls |
| **Bug** | `ph-bug` | Regular | Logcat Viewer, Diagnostics | Android Logcat and error report export |
| **CaretDown** | `ph-caret-down` | Regular | Dropdowns, Accordions | Expand/collapse details and reasoning blocks |
| **CaretRight** | `ph-caret-right` | Regular | File Tree Nodes, Breadcrumbs | Expand nested folders, path navigation |

---

## 3. Implementation in Mockups & Android Codebase

### 3.1 Web Prototype Implementation
The HTML/CSS mockup embeds all icons as an optimized inline SVG sprite (`<svg style="display:none">...<symbol id="...">...</symbol></svg>`). Icons are referenced cleanly via:
```html
<svg class="icon"><use href="#ph-house"></use></svg>
```
This guarantees zero external HTTP requests, instant rendering, and complete offline functionality.

### 3.2 Android Jetpack Compose Implementation
For the native Android app, Phosphor Icons SVGs are converted to Android Vector Drawables (`VectorDrawable`) stored in `app/src/main/res/drawable/` and exposed via Compose helper composables:
```kotlin
@Composable
fun PhosphorIcon(
    icon: PhosphorIcons,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current
) {
    Icon(
        painter = painterResource(id = icon.drawableResId),
        contentDescription = contentDescription,
        modifier = modifier.size(24.dp),
        tint = tint
    )
}
```

---

## 4. Open-Source License & Attribution
- **Phosphor Icons:**
  - Copyright (c) 2020 Phosphor Icons
  - Licensed under the **MIT License**
  - Repository: `https://github.com/phosphor-icons/core`
  - Attribution is included in the in-app Developer & Credits screen and in `docs/license-plan.md`.
