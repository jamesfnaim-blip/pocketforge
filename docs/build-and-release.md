# Build, Signing & Release Pipeline

This guide explains how to build, cryptographically sign, and release **PocketForge AI** without needing Android Studio or a local developer machine, powered by automated **GitHub Actions CI/CD workflows**.

---

## 1. Automated GitHub Actions Build Workflow

You can build complete release APKs in the cloud for free using GitHub Actions. Create the following workflow file in your repository:

### Workflow File: `.github/workflows/build-release.yml`

```yaml
name: Build PocketForge Release APK

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:

jobs:
  build:
    name: Build & Package Android APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4
        with:
          submodules: recursive

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Set up Android NDK (r28b)
        uses: nttld/setup-ndk@v1
        with:
          ndk-version: r28b
          link-to-sdk: true

      - name: Cache Gradle Packages
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Grant Execute Permission for Gradlew
        run: chmod +x gradlew

      - name: Build Online Release APK
        run: ./gradlew assembleOnlineRelease --stacktrace

      - name: Sign Release APK
        uses: r0adkll/sign-android-release@v1
        id: sign_app
        with:
          releaseDirectory: app/build/outputs/apk/online/release
          signingKey: ${{ secrets.ANDROID_SIGNING_KEY }}
          alias: ${{ secrets.ANDROID_KEY_ALIAS }}
          keyStorePassword: ${{ secrets.ANDROID_KEYSTORE_PASSWORD }}
          keyPassword: ${{ secrets.ANDROID_KEY_PASSWORD }}

      - name: Upload APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: pocketforge-ai-release
          path: ${{ steps.sign_app.outputs.signedReleaseFile }}

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        if: startsWith(github.ref, 'refs/tags/')
        with:
          files: ${{ steps.sign_app.outputs.signedReleaseFile }}
          generate_release_notes: true
          draft: false
          prerelease: false
```

---

## 2. Generating Your Cryptographic Release Keystore

To sign official APKs that can be updated on users' phones without "App not installed" errors, generate a release keystore using the standard `keytool` command:

```bash
keytool -genkey -v \
  -keystore pocketforge-release.jks \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -alias pocketforge-key
```

### GitHub Repository Secrets Setup
Convert the generated `.jks` file to Base64 and save it under your GitHub repository's **Settings → Secrets and variables → Actions**:
- `ANDROID_SIGNING_KEY`: Output of `base64 -w 0 pocketforge-release.jks`
- `ANDROID_KEY_ALIAS`: `pocketforge-key`
- `ANDROID_KEYSTORE_PASSWORD`: Your chosen password
- `ANDROID_KEY_PASSWORD`: Your chosen key password

---

## 3. Versioning Strategy

Version numbers are governed by semantic versioning (`major.minor.patch`):
- `versionCode` (Integer): Incremented by `1` with every release (e.g. `101`, `102`, `103`). Android uses this number internally to determine if an update is newer.
- `versionName` (String): User-visible version string (e.g. `1.0.0`, `1.0.1`, `1.1.0`).

Configure in `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 100
    versionName = "1.0.0"
}
```

---

## 4. Distribution Channels

### A. GitHub Releases (Primary Tier)
- Tag a release in Git (`git tag v1.0.0 && git push origin v1.0.0`).
- GitHub Actions automatically compiles, signs, and attaches `pocketforge-1.0.0.apk` to the GitHub release page.
- Direct download link is immediately available to users.

### B. F-Droid (Open-Source Community Tier)
- F-Droid requires building 100% from source code on clean Debian runners without proprietary binary blobs.
- Create metadata file `fdroid/com.pocketforge.ai.yml`.
- Because our project downloads proprietary agents (Claude Code) at runtime rather than bundling them inside the APK, the core app passes F-Droid's strict Free Software Inclusion guidelines.

---

## 5. Open-Source Repository Governance Files

Every professional open-source project requires standardized documentation files in the repository root:

1. **`README.md`:** Product pitch, architecture overview, screenshots, quickstart guide, and acknowledgments.
2. **`CONTRIBUTING.md`:** Code standards, how to submit PRs, how to build locally.
3. **`CODE_OF_CONDUCT.md`:** Contributor Covenant standard for community interactions.
4. **`LICENSE`:** Full text of the project's **MIT License**.
5. **`NOTICE`:** Mandatory legal attribution honoring **Mobile Harness Contributors (MIT)**, **PRoot (GPL-2.0)**, **libandroid-shmem (BSD-3)**, and **talloc (LGPL-3)**.
6. **`SECURITY.md`:** Responsible disclosure instructions for reporting potential Keystore or sandbox vulnerabilities.
