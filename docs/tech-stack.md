# Technology Stack: Mobile Harness

This document outlines the complete technology stack powering **Mobile Harness**, covering programming languages, frameworks, UI toolkits, native components, build systems, security mechanisms, and OS integration strategies.

---

## 1. Programming Languages

| Layer | Language | Version / Standard | Scope & Purpose |
| :--- | :--- | :--- | :--- |
| **Android Application** | **Kotlin** | `2.2.21` (JVM target 17) | 100% of application logic, UI components, background services, state management, and agent bridges. |
| **Native Runtime & PTY** | **C** | `C11` / GNU extensions (`_GNU_SOURCE`) | POSIX pseudo-terminal handling (`pocket_spawn.c`), executable packaging stub (`executable_carrier.c`), child launcher (`pocket_launcher.c`). |
| **PRoot & Allocator** | **C & GNU Assembly** | `C99` / ASM (`assembly.S`) | PRoot core engine (`third_party/proot`), Samba `talloc` memory manager, and Android shared memory shims. |
| **Automation & Bundles** | **Bash & Python** | Bash 5.x / Python 3.8+ | Deterministic tarball creation (`create_deterministic_tar.py`), rootfs build scripts, and local update servers. |
| **Guest OS Runtime** | **JavaScript / TypeScript / Bash** | Node.js 24.19.0, Bash 5.0 | Execution environment for coding agents (Claude Code, DeepSeek Harness, Antigravity CLI) inside the Ubuntu rootfs. |

---

## 2. Frameworks, UI & Architecture

- **UI Framework:** **Jetpack Compose** (via Compose BOM `2025.02.00` & Compose Compiler plugin `2.2.21`).
- **Design System:** **Material Design 3 (`androidx.compose.material3:material3`)** with custom theme adaptations:
  - Dynamic Dark and Light themes.
  - Custom **AMOLED Pure Black (`#000000`)** surface palette.
  - Extended Material Icons library (`androidx.compose.material:material-icons-extended`).
- **Architecture Pattern:** **MVI / MVVM (Model-View-ViewModel)** with unidirectional data flow:
  - Centralized StateFlow: `MainViewModel` exposes `state: StateFlow<AppUiState>`.
  - UI observes state via `collectAsStateWithLifecycle()`.
  - Single event bus (`MutableSharedFlow<RuntimeEvent>`) for asynchronous process events.
- **Concurrency & Asynchrony:**
  - **Kotlin Coroutines (`org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0`)**: Used for all background dispatching, network validation, and disk I/O.
  - **Kotlin Flows (`StateFlow`, `SharedFlow`, `Flow`)**: Used for streaming live agent output, tool execution states, and build progress.

---

## 3. Libraries & Dependencies

### Android & Core Libraries
- **`androidx.core:core-ktx:1.15.0`**: Core Android Kotlin extensions.
- **`androidx.activity:activity-compose:1.10.0`**: Activity integration for Jetpack Compose.
- **`androidx.lifecycle:lifecycle-runtime-ktx:2.8.7`** & **`lifecycle-viewmodel-ktx:2.8.7`**: Lifecycle-aware coroutine scopes and state retention across configuration changes.

### Compression & Decompression
- **`com.github.luben:zstd-jni:1.5.7-12@aar`**:
  - High-performance JNI bindings to Zstandard compression.
  - Enables streaming decompression of `.tar.zst` runtime packages at native CPU speed without intermediate disk extraction.
- **`org.apache.commons:commons-compress:1.27.1`**:
  - Pure-Java TAR archive stream parsing (`TarArchiveInputStream`).
  - Preserves Unix file modes, permissions, symlinks, and directory structures inside the rootfs.

### Cryptography & Security
- **Android Keystore System (`android.security.keystore`)**:
  - Hardware-backed key generation (`KeyGenParameterSpec`, `KeyProperties.PURPOSE_ENCRYPT`).
  - Stores the master AES-256 key (`pocket-provider-key`) in the device's secure enclave (TEE/StrongBox).
- **Java Cryptography Architecture (`javax.crypto.Cipher`)**:
  - `AES/GCM/NoPadding` with 128-bit authentication tag and 12-byte random IV for API keys and database backups.

---

## 4. Native Engine & System Layer (PRoot & PTY)

1. **Rootless Subsystem Engine: PRoot**
   - **Origin:** Custom fork based on `termux/proot` (`VERSION 5.1.107.91`).
   - **Mechanism:** Intercepts system calls using `ptrace` (`PTRACE_SYSCALL`) and `seccomp` filters.
   - **Extensions Activated:**
     - `fake_id0`: Simulates root UID/GID `0` so `apt-get` and package managers function normally.
     - `link2symlink`: Emulates hard links as symbolic links on filesystems (like Android's `/data/data`) that disallow hard links.
     - `ashmem_memfd` & `sysvipc`: Intercepts shared memory calls and routes them through Android's anonymous shared memory driver (`/dev/ashmem`).
     - `WITH_LIBANDROID_SHMEM`: Links `libandroid-shmem` (`shmem.c`) to redirect System V IPC to Android-compatible shared memory.

2. **Native Process Spawning & PTY (`libpocketspawn.so`)**
   - Implements native JNI functions in `cpp/pocket_spawn.c`:
     - Allocates pseudo-terminal masters with `posix_openpt(O_RDWR | O_NOCTTY | O_CLOEXEC)`.
     - Calls `ioctl(slave_fd, TIOCSCTTY, 0)` to make the slave PTY the **controlling terminal** for the child process.
     - Calls `ioctl(slave_fd, TIOCSWINSZ, &size)` to set the **terminal window dimensions** (rows and columns).
     - Calls `prctl(PR_SET_DUMPABLE, 1)` to allow PRoot's tracer process to attach `ptrace` to child processes under Android's security policies.
     - Sets distinct process group IDs (`setpgid(0, 0)`) so signals (`SIGINT`, `SIGTERM`, `SIGKILL`) cleanly terminate entire process subtrees.

3. **Memory Allocator: Samba Talloc**
   - Embedded hierarchical memory allocator (`third_party/talloc/talloc.c`, version 2.4.3).
   - Provides safe, leak-free memory pools for PRoot's internal path canonicalization and system call tracking.

---

## 5. `targetSdk 28` Strategy & Android Restrictions

Mobile Harness employs a dual-target strategy (`targetSdk = if (playBuild) 36 else 28`):

### Why `targetSdk 28` is Used in Direct APKs
Starting with **Android 10 (API level 29)**, Android enforced strict **W^X (Write XOR Execute)** policies on application internal storage:
- The `/data/data/<package>/` directory is mounted with the `noexec` flag or guarded by SELinux rules that deny `execve()` on any binary residing within user-writable app storage (`denied { execute } for path="/data/data/..."`).
- If an app sets `targetSdkVersion >= 29`, any attempt to execute a binary extracted to `/data/data/com.jarves.mh/files/runtime/ubuntu/usr/bin/bash` or `node` fails immediately with `EACCES` (Permission denied).
- However, Android provides backward-compatibility SELinux exemptions for apps targeting **API 28 (Android 9.0)** or lower, allowing them to execute binaries in their private app data directory.

### Risks and Failure Modes of `targetSdk 28`
1. **Google Play Store Ban:** Google Play strictly requires all new apps and updates to target API 34+ (currently API 35/36). An APK with `targetSdkVersion 28` cannot be published on Google Play.
2. **Android 14+ Installation Gate:** Android 14+ blocks the installation of apps targeting `targetSdkVersion < 23` by default and warns heavily on API 28. Sideloading on Android 14/15 requires running:
   ```bash
   adb install --bypass-low-target-sdk-block app-release.apk
   ```
3. **OEM Firmware Aggression:** Certain OEM builds (e.g., Xiaomi MIUI/HyperOS, Huawei EMUI, Samsung OneUI) flag target-28 apps with persistent "App built for an older version of Android" warnings.

### How `playBuild` Targets API 36 (Android 16)
When built with `-PplayBuild=true`, Mobile Harness switches `targetSdk = 36`. To work around W^X:
- In `CMakeLists.txt` (lines 117–131), PRoot and its loader are packaged as shared libraries:
  `libproot.so` and `libprootloader.so`.
- Android's native library directory (`nativeLibraryDir`, `/data/app/.../lib/arm64`) is mounted with executable permissions (`rx`).
- PRoot's custom loader (`libprootloader.so`) intercepts execution and uses `mmap(..., PROT_EXEC, ...)` to load guest ELF code into memory rather than directly executing from `/data/data`.

### Alternatives for the New Project
1. **Packaging Core Utilities as Native Shared Libraries:** Compile core binaries (bash, proot, node) into `lib<name>.so` inside `jniLibs/arm64-v8a/`. This allows standard execution under targetSdk 36.
2. **Anonymous Memory (`memfd_create`):** Creating anonymous memory file descriptors and executing via `fexecve()` (supported on Linux kernels >= 3.17).
3. **Termux Bootstrap Model:** Using PRoot's user-space ELF loader combined with `proot -k` (kompat extension).

### Verification on a Real Phone
*To verify targetSdk behavior on physical hardware:*
1. Build both APKs:
   - Sideload APK: `./gradlew assembleOnlineRelease` (`targetSdk 28`).
   - Play APK: `./gradlew assembleOnlineRelease -PplayBuild=true` (`targetSdk 36`).
2. Install on an Android 14 or 15 device via `adb install`.
3. Launch the terminal tab and run:
   ```bash
   uname -a
   /usr/bin/node -v
   ```
4. Observe if `execve` succeeds or throws `EACCES (Permission denied)` under `targetSdk 36`.
*Marked Status:* UNVERIFIED whether Play Store review bots will accept PRoot's `prootloader-bin` executable carrier under strict 2026 developer policies; must be validated via Google Play Console internal test track.
