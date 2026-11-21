# Background Execution & Process Longevity

This document provides a technical audit of how the original **Mobile Harness** keeps tasks running in the background, followed by an architectural blueprint for a **significantly more robust background execution engine** for our new application.

---

## Part 1: How the Original Mobile Harness Keeps Tasks Alive

In the original codebase, background persistence is handled primarily by `source/app/src/main/java/com/jarves/mh/runtime/RuntimeExecutionService.kt` and `RuntimeSetupService.kt`:

### 1. Foreground Service Architecture
In `AndroidManifest.xml`:
```xml
<service
    android:name=".runtime.RuntimeExecutionService"
    android:exported="false"
    android:stopWithTask="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="User-started local development task" />
</service>
```
- **Service Type:** Uses Android 14+'s `specialUse` foreground service type, which requires an explicit subtype declaration explaining its purpose.
- **`stopWithTask="false"`:** Ensures the service is not automatically stopped when the user swipes away the task from the recent apps list.

### 2. Wake Lock Handling
- When a task or agent turn starts, `RuntimeExecutionService` acquires a `PowerManager.WakeLock`:
  ```kotlin
  wakeLock = getSystemService(PowerManager::class.java).newWakeLock(
      PowerManager.PARTIAL_WAKE_LOCK,
      "pocketdev:runtime-execution",
  ).apply {
      setReferenceCounted(false)
      acquire(12 * 60 * 60 * 1000L) // 12-hour timeout
  }
  ```
- **`PARTIAL_WAKE_LOCK`:** Keeps the phone's CPU running at full clock speed even when the screen is turned off or the phone is placed on a table.

### 3. Notification & Interactivity
- Displays a persistent, non-dismissible notification (`RUNNING_NOTIFICATION_ID = 2001`).
- Contains a red **"Stop"** action button that sends `ACTION_STOP` to `RuntimeExecutionService`, which in turn triggers `RuntimeTaskController.requestStop()` to signal child processes.

### 4. Weaknesses & Gaps in the Original Implementation
While functional for basic tasks, the original design has major vulnerabilities:
1. **No Phantom Process Protection:** Does not handle Android 12+'s 32-process limit; running an agent with multiple subshells or a Gradle daemon causes Android to abruptly kill the processes.
2. **No Terminal Session Persistence:** If the Android OS terminates `MainActivity` under memory pressure, the active terminal session dies instantly because the PTY is bound to the Activity's memory.
3. **No OEM Battery Killer Management:** Devices from Samsung, Xiaomi, and Vivo aggressively murder background services after ~5–10 minutes regardless of wake locks, unless deep OEM-specific settings are toggled.
4. **No Health Monitoring:** The user cannot see how much CPU, RAM, or battery their background agents are consuming.

---

## Part 2: Blueprint for a Stronger Background Engine

To achieve true desktop-grade reliability on mobile, our project will engineer an enterprise-grade background lifecycle system:

```
+---------------------------------------------------------------------------------+
|                         Enterprise Background Engine                            |
+---------------------------------------------------------------------------------+
        │                                                     │
        ▼                                                     ▼
+───────────────────────────+                         +───────────────────────────+
| Foreground Service Layer  |                         |  Subprocess Supervisor    |
| - Sticky Foreground Svc   |                         |  - Process Group Monitor  |
| - High-Priority Channel   |                         |  - SIGSTOP/SIGCONT Queue  |
| - Interactive Stop/Pause  |                         |  - Tmux / Socket Daemon   |
+───────────────────────────+                         +───────────────────────────+
        │                                                     │
        ▼                                                     ▼
+───────────────────────────+                         +───────────────────────────+
|  OS Exemption Management  |                         |   Health & Diagnostics    |
|  - Battery Opt Flow       |                         |   - Live Process Tree     |
|  - Phantom Process Bypass |                         |   - CPU & Memory Watchdog |
|  - OEM Setup Guides       |                         |   - Thermal Throttling    |
+───────────────────────────+                         +───────────────────────────+
```

---

### 1. Advanced Foreground Service & Priority Notifications

1. **Sticky Execution:**
   - Return `START_STICKY` in `onStartCommand()`. If the OS terminates the host service due to extreme RAM pressure, Android will automatically resurrect the service once memory pressure subsides.
2. **Rich Notification Updates:**
   - Real-time elapsed timer (`NotificationCompat.Builder.setUsesChronometer(true)`).
   - Dynamic action buttons: **Stop**, **Pause**, and **View Log**.
   - Dual notification channels:
     - `ongoing_tasks`: Low-importance sound, no vibration, ongoing.
     - `task_completed`: High-importance alert, vibration, informs the user when a build or long agent turn completes.

---

### 2. The Android 12+ "Phantom Process Killer" Solution

#### The Problem
Introduced in Android 12 (API 31), the **Phantom Process Killer** monitors background processes spawned by apps. If an app and its child processes exceed **32 total child processes**, or if child processes consume excessive CPU while in the background, Android's `ActivityManagerService` kills the entire process tree without notifying the app!
Because PRoot, bash, Node.js, git, and subagents routinely spawn multiple subprocesses (e.g., Gradle builds spawn dozens of workers), this is the #1 reason coding tools crash in the background on modern phones.

#### Our Multi-Tier Solution
1. **Tier 1: Bounded Process Concurrency (Pure App-Level):**
   - Configure Gradle with `--max-workers=2` and `--no-daemon` to strictly limit thread/process counts.
   - Configure Node.js with `UV_THREADPOOL_SIZE=2`.
   - Prevent background agent CLIs from launching parallel subagents without limit.
2. **Tier 2: Shizuku / ADB Automated Exemption (One-Tap Fix):**
   - Provide an optional one-tap fix inside the app using **Shizuku** (or a simple copy-paste ADB command) that disables the Phantom Process Killer at the system level:
     ```bash
     /system/bin/device_config put activity_manager max_phantom_processes 2147483647
     # or on Android 12L/13+:
     settings put global settings_enable_monitor_phantom_procs false
     ```
   - Our app will detect if Shizuku is running and offer a single **"Fix Phantom Process Killer"** button.

---

### 3. Session Persistence (Terminal Detach/Attach Engine)

In our version, terminal sessions must survive app kills, configuration changes, and screen rotations:

1. **Background Socket Daemon (`mh-daemon`):**
   - Instead of binding the PTY directly to `MainActivity` or Compose state, terminal sessions will attach to a local Unix domain socket daemon (`/tmp/terminal.sock`) running inside the Linux subsystem (powered by a lightweight background `tmux` or custom socket multiplexer).
2. **Instant Reconnect:**
   - When the user minimizes or closes the app and reopens it 2 hours later, `TerminalScreen` connects to the active Unix socket and replays the current screen buffer without interrupting the running script.

---

### 4. OEM-Specific Aggressive Battery Killer Bypass

Many smartphone manufacturers (Xiaomi, Samsung, Oppo, Vivo, Huawei, OnePlus) implement non-standard proprietary task killers that ignore standard Android `WakeLock` APIs.

To combat this, we will implement an in-app **Device Longevity Wizard** powered by heuristics from [DontKillMyApp.com](https://dontkillmyapp.com/):

```
+---------------------------------------------------------------------------------+
|                    Device Battery Optimization Setup                            |
+---------------------------------------------------------------------------------+
|  Detected Phone: Xiaomi POCO F5 (MIUI 14 / HyperOS)                             |
|                                                                                 |
|  [✓] 1. Android Battery Optimization Disabled (Passed)                          |
|  [!] 2. Autostart Permission Required (Action needed)                           |
|      -> Tap to open "Manage Apps -> Mobile Harness -> Autostart"                |
|  [!] 3. MIUI Battery Saver set to "No Restrictions" (Action needed)             |
|      -> Tap to open MIUI Battery Saver settings                                 |
|  [✓] 4. Lock in Recent Apps (Recommended)                                       |
|      -> Swipe down on app card in Recent Apps and tap the Padlock icon.         |
|                                                                                 |
|  [ Open OEM Settings ]                         [ Re-Check Status ]              |
+---------------------------------------------------------------------------------+
```

- Directly deep-links into manufacturer-specific intent actions:
  - **Xiaomi:** `miui.intent.action.OP_AUTO_START`
  - **Samsung:** `com.samsung.android.sm.ACTION_BATTERY`
  - **Huawei:** `com.huawei.systemmanager.optimize.bootstart`
  - **Oppo/Realme:** `coloros.safecenter.permission.startup.StartupAppListActivity`

---

### 5. In-App System Health & Resource Monitor

A dedicated diagnostics screen in our new design:
- **Running Processes Card:** Displays active Linux processes (`PID`, command name, memory in MB, CPU %).
- **Memory Pressure Gauge:** Real-time RAM bar showing device free memory and application heap limit.
- **Thermal Status Watchdog:** Monitors Android `PowerManager.OnThermalStatusChangedListener`. If device thermal state hits `THERMAL_STATUS_SEVERE`, the app throttles agent concurrency to protect the phone from overheating and throttling.
- **Battery Drain Metric:** Tracks mAh consumed per build/agent session.
