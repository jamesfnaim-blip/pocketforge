# Milestone 0: Baseline - Build the Unmodified Original

- **Status:** COMPLETED (CI Build Succeeded, Artifact Uploaded, Ready for Phone Test)
- **Date:** 2026-11-21
- **Repository:** [jamesfnaim-blip/pocketforge](https://github.com/jamesfnaim-blip/pocketforge)
- **CI Run ID:** [44898436969](https://github.com/jamesfnaim-blip/pocketforge/actions/runs/44898436969)
- **Build Duration:** 3 minutes 47 seconds
- **Commit:** `c4ba15d` (`ci: fix sdkmanager path in build-debug workflow`)
- **Artifact Name:** `pocketforge-baseline-apk`

---

## 1. Objectives & Scope
The goal of Milestone 0 was to prove that the unmodified original **Mobile Harness** codebase compiles and packages cleanly into a bootable ARM64 Android debug APK in the cloud via GitHub Actions, without modifying any application code, package names, or native architecture.

---

## 2. Work Implemented
1. **Repository Setup:**
   - Initialized Git repository on branch `main`.
   - Updated `.gitignore` to protect against committing scratch directories (`source/`, `rea-tutorial/`, `*.zip`).
   - Registered Git submodules for `third_party/proot` (commit `61681c6481197e3c0cec6726075053adb740f235`) and `third_party/libandroid-shmem` (commit `7f0bd7e25dbdd146265aff7c6a890029e374622d`).
   - Added baseline notice to `README.md`.
2. **Automated CI/CD Workflow (`.github/workflows/build-debug.yml`):**
   - Configured runner on `ubuntu-latest` with JDK 17 (`temurin`).
   - Integrated official `gradle/actions/setup-gradle@v4` for fast, cached execution.
   - Configured Android SDK platform 36, Build-Tools 35.0.0, NDK 28.2.13676358, and CMake 3.22.1.
   - Added pre-build asset fetch for `pocketdev-agy-arm64-2026.09.1.tar.zst` required by Gradle's `prepareBundledAgentAssets` task.
   - Executed `./gradlew assembleOnlineDebug --stacktrace`.
   - Uploaded generated APK as `pocketforge-baseline-apk`.
3. **Authentication & Remote Sync:**
   - Authenticated GitHub CLI with `workflow` and `repo` scopes.
   - Pushed `main` branch to user repository `https://github.com/jamesfnaim-blip/pocketforge`.

---

## 3. Problems Encountered & Resolutions
1. **Missing `workflow` Scope:**
   - *Error:* Push rejected: `refusing to allow an OAuth App to create or update workflow .github/workflows/build-debug.yml without workflow scope`.
   - *Fix:* Re-authenticated GitHub CLI with `--scopes workflow,repo` via standard browser device flow.
2. **Missing `sdkmanager` in Runner Path:**
   - *Error:* `/home/runner/work/_temp/...sh: line 4: sdkmanager: command not found`.
   - *Fix:* Explicitly exported `$ANDROID_HOME/cmdline-tools/latest/bin` to `PATH` before invoking `sdkmanager`.
3. **External Pre-Packaged Runtime Assets:**
   - *Detection:* `app/build.gradle.kts` task `prepareBundledAgentAssets` expects `dist/runtime-bundles/pocketdev-agy-arm64-2026.09.1.tar.zst` which is gitignored in the repository.
   - *Fix:* Added an automated curl download step in GitHub Actions before the Gradle build step.

---

## 4. Verification Evidence
- GitHub Actions run completed with status `success` in 3m 47s:
  - `Checkout repository with submodules` [PASS]
  - `Set up JDK 17` [PASS]
  - `Set up Gradle` [PASS]
  - `Install Android SDK platform 36, NDK 28, and CMake` [PASS]
  - `Download bundled agent runtime asset` [PASS]
  - `Build Online Debug APK` [PASS]
  - `Upload Baseline APK` [PASS]

---

## 5. Next Steps
1. **Phone Checkpoint M0:** User downloads `pocketforge-baseline-apk` from GitHub Actions, installs on physical ARM64 Android device, completes first-run setup with online runtime, runs verification commands (`uname -a`, `node -v`, `git --version`), and reports back.
2. Proceed to **Milestone 1**: App Foundation & New Design System Shell (`ForgeDesign`).
