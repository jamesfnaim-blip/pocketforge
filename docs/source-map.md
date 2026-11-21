# Source Map: Mobile Harness Repository Structure

## 1. Project Organization & Modules

The repository is organized as a single-module Android project (`:app`) backed by CMake native C/C++ libraries and bundled submodules.

```text
source/
├── .github/                        # GitHub automation & configuration
│   └── FUNDING.yml                 # GitHub Sponsors and funding targets
├── .vscode/                        # Visual Studio Code development tasks
│   └── tasks.json                  # Editor build and format tasks
├── app/                            # Primary Android Application Module
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/             # Bundled static assets
│   │   │   │   └── licenses/       # Open-source license text files (GPL, LGPL, MIT, BSD)
│   │   │   ├── cpp/                # Native C/C++ source code & JNI bridges
│   │   │   │   ├── CMakeLists.txt  # Native build script (proot, pocketspawn, talloc, shmem)
│   │   │   │   ├── executable_carrier.c # Stub shared library to package ELF executables
│   │   │   │   ├── GenerateLoaderInfo.cmake # CMake script to extract proot ELF loader offsets
│   │   │   │   ├── pocket_launcher.c # Wrapper binary setting dumpable and unsetting LD flags
│   │   │   │   └── pocket_spawn.c  # Native JNI implementation of forkpty/PTY execution
│   │   │   ├── java/com/jarves/mh/ # Kotlin application source code
│   │   │   │   ├── MainActivity.kt # Main Activity, theme setup, language context injection
│   │   │   │   ├── backup/         # Backup & restore subsystem (.mhbackup)
│   │   │   │   │   ├── BackupCrypto.kt  # AES-256-GCM encryption with Argon2/PBKDF2 keys
│   │   │   │   │   ├── BackupManager.kt # ZIP creation/extraction of projects, preferences, keys
│   │   │   │   │   └── BackupService.kt # Foreground service for long-running backup operations
│   │   │   │   ├── data/           # Storage, persistence & Keystore encryption
│   │   │   │   │   ├── ApiKeyVault.kt    # Android Keystore encrypted credentials storage
│   │   │   │   │   └── AppPreferences.kt # SharedPreferences wrapper for app & agent settings
│   │   │   │   ├── model/          # Domain models, enums & event classes
│   │   │   │   │   └── Models.kt         # Project, ProviderProfile, AgentKind, RuntimeEvent
│   │   │   │   ├── network/        # Network communication & API clients
│   │   │   │   │   ├── GitHubClient.kt    # GitHub API integration (repo clones, releases)
│   │   │   │   │   ├── LocalNetwork.kt    # Local HTTP server discovery & port reachability
│   │   │   │   │   └── ProviderApiClient.kt # Validation pings & model discovery for AI APIs
│   │   │   │   ├── runtime/        # Linux subsystem, PRoot, and Agent bridges
│   │   │   │   │   ├── AgentDriver.kt     # High-level coordinator for active agent engine
│   │   │   │   │   ├── AndroidAppInstaller.kt # PackageInstaller API wrapper for APK installation
│   │   │   │   │   ├── AndroidAppInstallReceiver.kt # BroadcastReceiver for install status
│   │   │   │   │   ├── AndroidBuildSupport.kt # Gradle command generator & build output parser
│   │   │   │   │   ├── AndroidProjectTemplateGenerator.kt # Generates Compose/XML starter projects
│   │   │   │   │   ├── AntigravityAuthController.kt # OAuth flow for Google Antigravity CLI
│   │   │   │   │   ├── AntigravityRuntimeBridge.kt # Stream protocol bridge for Antigravity CLI
│   │   │   │   │   ├── BoundedFileReads.kt # Safe file reading with memory constraints
│   │   │   │   │   ├── ClaudeRuntimeBridge.kt # Stream-JSON bridge for Claude Code agent
│   │   │   │   │   ├── DshRuntimeBridge.kt # Process bridge for DeepSeek Harness (dsh)
│   │   │   │   │   ├── LocalFormatGateway.kt # Local HTTP proxy translating OpenAI format
│   │   │   │   │   ├── NativeSpawnProcess.kt # Process wrapper around JNI pocketspawn
│   │   │   │   │   ├── OpenRouterRoutingGateway.kt # Routing helper for OpenRouter models
│   │   │   │   │   ├── RootfsRepair.kt     # Fixes missing symlinks, dev nodes, permissions
│   │   │   │   │   ├── RuntimeBridge.kt    # Interface definition for agent runtimes
│   │   │   │   │   ├── RuntimeCompatibility.kt # Checks for arm64-v8a ABI & 16 KiB page size
│   │   │   │   │   ├── RuntimeExecutionService.kt # Foreground service with wake lock for agents
│   │   │   │   │   ├── RuntimeInstaller.kt # Rootfs & tar.zst bundle extraction logic
│   │   │   │   │   ├── RuntimeSetupService.kt # Foreground service for runtime installation
│   │   │   │   │   └── WorkspaceCheckpoints.kt # Git-based rollback and change tracking
│   │   │   │   ├── ui/             # Jetpack Compose user interface
│   │   │   │   │   ├── AgentScreen.kt     # Agent selection, provider settings & credentials
│   │   │   │   │   ├── AndroidBuildText.kt # Formatted build status & issue annotations
│   │   │   │   │   ├── BackupRestoreUi.kt # Backup creation, restore, and password modals
│   │   │   │   │   ├── LocaleHelper.kt    # In-app language switching helper
│   │   │   │   │   ├── MainViewModel.kt   # Central ViewModel holding complete UI state
│   │   │   │   │   ├── MarkdownText.kt    # Markdown parser and renderer for agent messages
│   │   │   │   │   ├── ModelLabels.kt     # Human-readable labels for LLM models
│   │   │   │   │   ├── PocketDevApp.kt    # Root Composable host: navigation, tabs, dialogs
│   │   │   │   │   ├── SettingsScreenModern.kt # Application settings, themes, language
│   │   │   │   │   ├── TerminalScreen.kt  # Virtual terminal UI with input row and keys
│   │   │   │   │   ├── WhatsNewDialog.kt  # Release notes modal for app updates
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── AppLanguage.kt # Supported languages enum & display names
│   │   │   │   │       └── Theme.kt       # PocketTheme Material3 color schemes (Dark/Light)
│   │   │   │   └── update/         # In-app updater
│   │   │   │       └── AppUpdater.kt      # GitHub Release manifest parser & APK downloader
│   │   │   └── res/                # Android UI resources (drawables, values, localized strings)
│   │   │       ├── values/         # Default English strings & themes
│   │   │       ├── values-ru/      # Russian localized strings
│   │   │       ├── values-zh/      # Chinese localized strings
│   │   │       └── xml/            # Security configs, file provider paths, data extraction
│   │   └── test/                   # Unit test directory
│   ├── build.gradle.kts            # App module build configuration & dependencies
│   └── proguard-rules.pro          # ProGuard / R8 rules
├── dist/                           # Release distribution metadata
│   └── runtime-bundles/
│       └── manifest.json           # SHA-256 digests, versions, sizes of runtime tar.zst files
├── fastlane/                       # Fastlane metadata & automated deployment configs
├── fdroid/                         # F-Droid packaging metadata
│   └── com.jarves.mh.yml           # F-Droid build recipe
├── gradle/wrapper/                 # Gradle wrapper binaries
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties   # Gradle 8.14.3 distribution URL
├── scripts/                        # Utility & release scripts
│   ├── build-play-release.sh       # Release build script for Google Play Bundle
│   ├── install-phone.sh            # Quick adb install script
│   ├── serve-update-server.sh      # Local HTTP test server for updater verification
│   └── runtime-bundles/            # Rootfs generation scripts
│       ├── build-agy-from-official.sh
│       ├── build-claude-from-installed-android.sh
│       ├── build-core-on-rooted-android.sh # Ubuntu 20.04 rootfs builder
│       ├── build-dsh-from-installed-android.sh
│       ├── create_deterministic_tar.py # Produces byte-identical reproducible tarballs
│       └── README.md               # Guide to runtime bundle architecture
├── third_party/                    # External native dependencies
│   ├── libandroid-shmem/           # Shared memory implementation for Android (BSD-3)
│   ├── proot/                      # PRoot user-space chroot/bind mount engine (GPL-2.0)
│   └── talloc/                     # Hierarchical memory allocator for PRoot (LGPL-3.0)
├── build.gradle.kts                # Root project build configuration
├── gradle.properties               # JVM memory (-Xmx3g) and AndroidX flags
├── gradlew / gradlew.bat           # Gradle wrapper scripts
├── LICENSE                         # Project MIT License
├── mobile-harness-update.json      # Sample update manifest
├── PRIVACY.md                      # Privacy policy
├── README.md                       # Upstream project README and architecture overview
└── settings.gradle.kts             # Included project modules (:app)
```

---

## 2. Entry Points

1. **Android UI Entry Point:**
   - **File:** `source/app/src/main/java/com/jarves/mh/MainActivity.kt`
   - **Class:** `com.jarves.mh.MainActivity`
   - **Role:** Handles Activity lifecycle, edge-to-edge rendering (`enableEdgeToEdge()`), system language override via `attachBaseContext()`, and instantiates `PocketDevApp(mainViewModel)`.

2. **Native Execution Entry Point (JNI):**
   - **File:** `source/app/src/main/cpp/pocket_spawn.c`
   - **Function:** `Java_com_jarves_mh_runtime_NativeSpawn_spawn`
   - **Role:** Handles POSIX terminal allocation (`posix_openpt`), sets process group ID (`setpgid`), unsets restrictive environment variables, enables child dumpability (`prctl(PR_SET_DUMPABLE, 1)`), and calls `execve()` to launch PRoot.

3. **PRoot Executable Carrier:**
   - **File:** `source/app/src/main/cpp/CMakeLists.txt` (lines 117–131)
   - **Role:** Tricks the Android Gradle Plugin (AGP) into packaging the `proot-bin` and `prootloader-bin` ELF executables inside the APK's `lib/arm64-v8a/` directory by compiling dummy shared libraries (`proot-carrier` -> `libproot.so`, `prootloader-carrier` -> `libprootloader.so`) and replacing them post-link with the actual static binaries.

4. **Background Execution Services:**
   - **`RuntimeExecutionService`** (`source/app/src/main/java/com/jarves/mh/runtime/RuntimeExecutionService.kt`):
     Runs as a foreground service with `foregroundServiceType="specialUse"` and a partial wake lock to keep AI agent tasks and builds running when the app is minimized.
   - **`RuntimeSetupService`** (`source/app/src/main/java/com/jarves/mh/runtime/RuntimeSetupService.kt`):
     Foreground service that keeps the device awake during large rootfs downloads and decompression.

---

## 3. Build System & SDK Configuration

- **Build Tool:** Gradle 8.14.3
- **Android Gradle Plugin (AGP):** `com.android.application:8.13.2`
- **Kotlin Version:** `2.2.21` (with `org.jetbrains.kotlin.plugin.compose:2.2.21`)
- **CMake Version:** `3.22.1`
- **NDK Version:** `28.2.13676358` (NDK 28 is required to emit 16 KiB page-aligned ELF binaries compatible with Android 15/16).
- **Target ABI:** `arm64-v8a` exclusively.
- **Java Compatibility:** Java 17 (`JavaVersion.VERSION_17`, `jvmTarget = "17"`).
- **SDK Versions:**
  - `compileSdk = 36` (Android 16 preview)
  - `minSdk = 28` (Android 9.0 Pie)
  - `targetSdk`:
    - Direct GitHub/F-Droid APK: `28` (Targeting API 28 bypasses modern Android restrictions on executing user-writable binaries inside app data directories).
    - Google Play Release (`playBuild=true`): `36` (Complies with Play Store target API requirements).

---

## 4. Product Flavors (Runtime Delivery Modes)

Mobile Harness defines two product flavors under the `runtimeDelivery` dimension:

1. **`online` (Default):**
   - APK size is minimal (~15–20 MB).
   - Does not bundle the rootfs tarballs inside the APK.
   - Downloads `pocketdev-core-arm64-2026.09.5.tar.zst` and agent bundles on first run from GitHub Releases (`RUNTIME_RELEASE_BASE_URL`).

2. **`offline`:**
   - Pre-packages all runtime tarballs (`core`, `claude`, `python`, `android`, `dsh`) directly into the APK's `assets/offline/runtime/` folder.
   - Requires zero internet connection for initial setup.
   - Resulting APK size is large (~800 MB - 1.2 GB).

---

## 5. Dependencies and Versions

All production and development dependencies declared in `app/build.gradle.kts`:

| Dependency | Version | Purpose |
| :--- | :--- | :--- |
| `androidx.compose:compose-bom` | `2025.02.00` | Jetpack Compose Bill of Materials |
| `androidx.core:core-ktx` | `1.15.0` | Android KTX core extensions |
| `androidx.activity:activity-compose` | `1.10.0` | Activity integration for Jetpack Compose |
| `androidx.compose.ui:ui` | BOM Managed | Core UI primitives |
| `androidx.compose.ui:ui-tooling-preview` | BOM Managed | Layout previews in IDE |
| `androidx.compose.material3:material3` | BOM Managed | Material Design 3 UI components |
| `androidx.compose.material:material-icons-extended` | BOM Managed | Full Material icon pack |
| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.8.7` | Coroutine scopes tied to lifecycle |
| `androidx.lifecycle:lifecycle-runtime-compose` | `2.8.7` | `collectAsStateWithLifecycle()` flow observer |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.8.7` | ViewModel integration in Compose |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | `2.8.7` | `viewModelScope` extension |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | `1.9.0` | Android main-thread dispatcher |
| `org.apache.commons:commons-compress` | `1.27.1` | TAR archive extraction without external tools |
| `com.github.luben:zstd-jni` | `1.5.7-12@aar` | High-performance native Zstandard decompression |
| `junit:junit` | `4.13.2` | Unit testing framework (test) |
| `org.json:json` | `20250107` | JSON parsing in unit tests (test) |
| `androidx.compose.ui:ui-tooling` | BOM Managed | Interactive tooling & inspection (debug) |
| `androidx.compose.ui:ui-test-manifest` | BOM Managed | Test activity manifest (debug) |

---

## 6. Largest Kotlin Source Files & Modularization Strategy

An exact line-count audit of the Kotlin codebase reveals extreme monolithic concentration:

| Rank | Source File | Exact Line Count | Primary Contents | Why It Must Be Split in the New Project |
| :---: | :--- | :---: | :--- | :--- |
| **1** | `app/src/main/java/com/jarves/mh/ui/PocketDevApp.kt` | **7,297** | Houses almost the entire UI: onboarding flow, projects list, chat tab, file tree, code viewer, and Android dashboard. | **Severe Anti-pattern.** Merge conflicts, impossible to maintain. Must be broken down into individual screen files under `ui/screens/` (e.g. `OnboardingScreen.kt`, `ProjectsScreen.kt`, `WorkspaceScreen.kt`, `ChatTab.kt`, `FilesTab.kt`). |
| **2** | `app/src/main/java/com/jarves/mh/ui/MainViewModel.kt` | **5,177** | God-object ViewModel managing UI state, file operations, process monitoring, agent session states, build tasks, git checkpoints. | Must be decomposed into specialized scoped ViewModels or domain controllers: `ProjectsViewModel`, `ChatViewModel`, `TerminalViewModel`, `AndroidBuildViewModel`. |
| **3** | `app/src/main/java/com/jarves/mh/ui/AgentScreen.kt` | **2,132** | Agent settings sheets, API key inputs, custom gateway protocol pickers, model discovery sheets. | Split into `AgentPickerSheet.kt`, `ProviderCredentialsDialog.kt`, and `ModelCatalogSheet.kt`. |
| **4** | `app/src/main/java/com/jarves/mh/runtime/RuntimeInstaller.kt` | **1,910** | Rootfs downloading, verification, tarball extraction, permission fixing, marker file verification, and toolchain setup. | Separate rootfs extraction (`RootfsExtractor.kt`), bundle download manager (`BundleDownloader.kt`), and toolchain installer (`DevStackInstaller.kt`). |
| **5** | `app/src/main/java/com/jarves/mh/ui/SettingsScreenModern.kt` | **1,121** | Theme settings, language choices, backup/restore buttons, terminal font configurations, system diagnostics. | Break into modular preference sections: `GeneralSettingsTab.kt`, `AppearanceSettingsTab.kt`, `DeveloperSettingsTab.kt`. |

