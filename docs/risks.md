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
| **R-08** | Public Debug Keystore Exposure vs. Secure Production Release Signing | **HIGH** | **HIGH** | 🔴 Severe |
| **R-09** | Cryptographic Signature Collision & Single-Install Constraint (`com.jarves.mh`) | **HIGH** | **CRITICAL** | 🔴 Severe |

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

---

### R-08: Public Debug Keystore Exposure vs. Secure Production Release Signing
* **The Hazard:** The repository commits a public `app/debug.keystore` file to Git with standard credentials (`storepass: android`, `keypass: android`). Anyone with access to the repository can build and sign an APK with this exact debug certificate.
* **Impact:** 
  - Debug builds provide **zero authenticity or tamper protection**. If a user installs a debug APK from an untrusted third party, Android will treat it as legitimate if signed by the same public debug key.
  - Using a public keystore for official public releases would allow bad actors to publish malicious update APKs that overwrite legitimate installations.
* **Engineered Mitigations:**
  1. **Strict Build Separation:**
     - `app/debug.keystore` is **exclusively restricted to `buildTypes.debug`**. It exists solely to maintain deterministic signing across development CI builds, preventing `INSTALL_FAILED_UPDATE_INCOMPATIBLE` during testing iterations.
     - `buildTypes.release` is strictly prohibited from falling back to `debug.keystore`.
  2. **Air-Gapped Production Keystore:**
     - The production signing key must **never be committed to Git**.
     - Release builds in GitHub Actions will pull the production keystore and credentials strictly from **GitHub Actions Encrypted Secrets**:
       - `MH_UPLOAD_STORE_BASE64`: Base64-encoded private release keystore file.
       - `MH_UPLOAD_STORE_PASSWORD`: Keystore master password.
       - `MH_UPLOAD_KEY_ALIAS`: Production key alias.
       - `MH_UPLOAD_KEY_PASSWORD`: Private key password.
  3. **Zero Chat Exposure:**
     - The project lead must never paste private keystores, base64 blobs, or passwords into any chat or public forum. All secrets configuration is performed via GitHub's official web console under `Repository Settings -> Secrets and variables -> Actions`.

---

### R-09: Cryptographic Signature Collision & Single-Install Constraint (`com.jarves.mh`)
* **The Hazard:** The user's physical phone currently has an existing build of Mobile Harness installed, signed with either upstream's release key or a previously generated ephemeral debug certificate. Because Android's Package Manager enforces strict cryptographic certificate matching per `applicationId`, attempting to install a new APK signed with `app/debug.keystore` over the old installation will be immediately rejected with:
  `INSTALL_FAILED_UPDATE_INCOMPATIBLE` (signatures do not match previously installed version).
* **Impact:** 
  - The new debug build **cannot be installed as an update over the existing installed Mobile Harness build**.
  - Because both apps currently share `applicationId = "com.jarves.mh"`, the original Mobile Harness and PocketForge AI **cannot coexist simultaneously** on the same device.
* **Engineered Mitigations & Mandatory User Procedure:**
  1. **Mandatory One-Time Backup & Uninstall:**
     - Before installing the first PocketForge unified debug APK, the user must **export/backup** all critical project files and API keys from their existing app.
     - The user must perform a **one-time complete uninstall** of the previously installed Mobile Harness app.
  2. **Subsequent Frictionless Updates:**
     - Once the initial PocketForge debug APK is installed, every subsequent build produced by CI (signed with the deterministic `app/debug.keystore`) can be updated seamlessly without uninstalling or wiping user data.
  3. **Permanent Solution (Milestone ENG-4):**
     - Under Milestone ENG-4, the application ID will be migrated to `com.pocketforge.ai`. From that milestone onward, PocketForge AI and the original Mobile Harness can be installed and run side-by-side on the same phone without any interference.
