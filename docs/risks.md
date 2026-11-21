# Risk Assessment & Mitigation Matrix

Running an autonomous Linux subsystem, developer toolchains, and AI coding agents on consumer Android devices introduces unique operating system, hardware, and policy challenges. This document catalogs the top technical risks and defines actionable mitigations.

---

## Risk Matrix Overview

| Risk Identifier | Risk Description | Likelihood | Impact | Severity |
| :---: | :--- | :---: | :---: | :---: |
| **R-01** | Google Play Policy on Executing Downloaded Binaries | **HIGH** | **CRITICAL** | 🔴 Severe |
| **R-02** | Android 15/16 16 KiB Memory Page Size Crashes | **HIGH** | **HIGH** | 🔴 Severe |
| **R-03** | Android 12+ "Phantom Process Killer" Subprocess Purges | **HIGH** | **HIGH** | 🔴 Severe |
| **R-04** | Aggressive OEM Battery Killers (Xiaomi, Samsung, Vivo) | **HIGH** | **HIGH** | 🔴 Severe |
| **R-05** | Internal Storage Exhaustion on Budget Phones | **MEDIUM** | **MEDIUM** | 🟡 Moderate |
| **R-06** | Incompatible x86 Emulators & 32-bit ARM Devices | **MEDIUM** | **MEDIUM** | 🟡 Moderate |
| **R-07** | Linux Subsystem Rootfs Corruption on Sudden Power Loss | **LOW** | **HIGH** | 🟡 Moderate |

---

## Detailed Risk Analysis & Engineered Mitigations

### R-01: Google Play Store Policy on Executing Downloaded Binaries
* **The Hazard:** Google Play Developer Policy (Device and Network Abuse / Malicious Behavior) prohibits apps from downloading executable code (like Linux ELF binaries or dynamically fetched shell binaries) outside of official Google Play app bundles.
* **Impact:** App suspension or account ban if submitted directly to Google Play without proper declaration.
* **Engineered Mitigations:**
  1. **Dual-Distribution Model:** Release the full-featured, unrestricted version primarily on **GitHub Releases** and **F-Droid** (the gold standard for open-source developer tools like Termux).
  2. **Play Store Compliance Flavor (`playBuild`):** If distributing on Google Play, pre-bundle verified open-source binaries inside APK native libraries (`jniLibs/arm64-v8a/`) or dynamic feature modules rather than downloading them dynamically, and file a formal Foreground Service Declaration for Developer Tools.

---

### R-02: Android 15 & 16 16 KiB Memory Page Size Incompatibility
* **The Hazard:** Beginning with Android 15, devices can be configured with 16 KiB kernel memory page sizes (instead of traditional 4 KiB). Any native binary or JNI shared library (`libproot.so`, `libpocketspawn.so`) compiled without 16 KiB alignment crashes instantly with `SIGSEGV` when loaded.
* **Impact:** 100% crash rate on newly released 2025/2026 flagship phones (Pixel 9, Galaxy S25, etc.).
* **Engineered Mitigations:**
  - Build strictly with **Android NDK 28+** (`28.2.13676358`).
  - Pass the linker flag `-Wl,-z,max-page-size=16384` across all CMake targets in `CMakeLists.txt`.
  - Add an automated GitHub Actions CI check verifying ELF segment alignment using `llvm-readelf -l`.

---

### R-03: Android 12+ "Phantom Process Killer"
* **The Hazard:** Android's `ActivityManagerService` enforces a strict system limit of **32 total child processes** across an app's process tree. When Gradle, Node.js, git, or subagents spawn background worker threads or child processes exceeding this limit, the OS terminates the entire app tree without warning.
* **Impact:** Builds and long agent turns fail silently midway through execution.
* **Engineered Mitigations:**
  1. **Concurreny Throttling:** Restrict Gradle (`--max-workers=2 --no-daemon`) and Node (`UV_THREADPOOL_SIZE=2`).
  2. **Shizuku / ADB Automated Bypass:** Provide a 1-tap in-app button that uses Shizuku to execute:
     ```bash
     device_config put activity_manager max_phantom_processes 2147483647
     settings put global settings_enable_monitor_phantom_procs false
     ```

---

### R-04: Aggressive OEM Battery Killers (Xiaomi, Samsung, Vivo, Oppo)
* **The Hazard:** Device manufacturers implement proprietary battery management daemons that force-stop background apps after 5–10 minutes, ignoring standard Android `WakeLock` APIs.
* **Impact:** Minimized agent sessions or web servers die as soon as the screen turns off.
* **Engineered Mitigations:**
  - Build an in-app **Device Longevity Wizard** that detects the user's phone brand (`Build.MANUFACTURER`) and launches direct intent settings for **Autostart**, **No Battery Restrictions**, and **Lock in Recent Apps** (linking to DontKillMyApp guidance).
  - Use a high-priority ongoing notification with an active chronometer and foreground service type `specialUse`.

---

### R-05: Internal Storage Exhaustion
* **The Hazard:** The core Ubuntu rootfs requires ~400 MB. Installing Android SDK build tools, OpenJDK 17, Python, and multiple npm packages can quickly balloon storage consumption to 3–5 GB.
* **Impact:** App crash or phone system warnings ("Storage almost full").
* **Engineered Mitigations:**
  - **Modular On-Demand Toolchains:** Do not pre-install heavy stacks; only download Android SDK (~800 MB) or Python (~60 MB) when the user explicitly requests an Android or Python project.
  - **Proactive Storage Preflight:** Before extracting any toolchain, verify that free storage exceeds `2.5x` the uncompressed bundle size using `StatFs`.
  - **1-Tap Storage Cleaner:** Settings button to purge `apt-get` cache, npm cache (`npm cache clean --force`), and Gradle build cache (`rm -rf ~/.gradle/caches`).

---

### R-06: Incompatible x86 Emulators & 32-bit Devices
* **The Hazard:** PRoot relies on kernel ptrace register layouts. Running an ARM64 PRoot binary inside an x86 PC Android emulator (e.g. Bluestacks, standard Android Studio emulator) fails with illegal instruction or ptrace emulation faults.
* **Impact:** Developer confusion when attempting to test the app in desktop emulators.
* **Engineered Mitigations:**
  - `RuntimeCompatibility.supportsArm64Runtime()` explicitly inspects `Build.SUPPORTED_ABIS` and rejects x86/x86_64 hosts upfront.
  - Instruct developers to test on physical ARM64 phones or official ARM64 system images in Android Studio.

---

### R-07: Rootfs Corruption on Sudden Device Shutdown
* **The Hazard:** If the phone's battery dies while extracting files or upgrading Ubuntu packages, the rootfs can enter a half-installed, broken state.
* **Impact:** The app becomes unable to launch bash or agents.
* **Engineered Mitigations:**
  - Retain `RootfsRepair.kt`'s atomic swap pattern: all modifications take place in `.staging/`; the active rootfs is only replaced upon 100% verified extraction.
  - Automatic snapshot backups before system updates.
