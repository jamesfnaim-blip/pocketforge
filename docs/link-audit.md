# Repository Link & Branding Audit

- **Date:** 2026-11-22
- **Fork Target Repository:** [jamesfnaim-blip/pocketforge](https://github.com/jamesfnaim-blip/pocketforge)
- **Upstream Project:** [techjarves/Mobile-Harness](https://github.com/techjarves/Mobile-Harness) (MIT License)

---

## 1. Audit Policy & Classification

Every link and identifier referencing the upstream repository has been audited and classified into three categories:

| Category | Policy | Action Taken |
| :--- | :--- | :--- |
| **A. User-Facing Links** | Must point to `jamesfnaim-blip/pocketforge` | Replaced all references for Privacy Policy, Issues, Source Code, Releases, and In-App Update Manifest. |
| **B. Runtime Bundle Base URLs** | Temporary upstream fallback with isolation | Kept pointing to validated runtime assets (`runtime-2026.09.4`) until mirrored in `jamesfnaim-blip/pocketforge` releases. Isolated in `app/build.gradle.kts` / `RuntimeConstants.kt`. |
| **C. Legal & MIT Attribution** | Must be strictly preserved | Retained in `NOTICE`, `LICENSE`, `DeveloperCreditsScreen.kt`, and `README.md` as required by MIT license ("Based on Mobile Harness by techjarves"). |

---

## 2. File-by-File Decision Record

### 1. `app/build.gradle.kts`
- **Privacy Policy URL:**
  - *Old:* `https://github.com/techjarves/Mobile-Harness/blob/main/PRIVACY.md`
  - *New:* `https://github.com/jamesfnaim-blip/pocketforge/blob/main/PRIVACY.md`
- **In-App Updater Manifest URL:**
  - *Old:* `https://github.com/techjarves/Mobile-Harness/releases/latest/download/mobile-harness-update.json`
  - *New:* `https://github.com/jamesfnaim-blip/pocketforge/releases/latest/download/pocketforge-update.json`
- **Runtime Asset Base URL:**
  - *Value:* `https://github.com/techjarves/Mobile-Harness/releases/download/runtime-2026.09.4`
  - *Decision:* Retained temporarily for asset downloads. Scheduled for mirroring under Milestone ENG-4 with SHA-256 hash pinning.

### 2. `PRIVACY.md`
- Completely rewritten for **PocketForge AI** reflecting local-only execution, Android Keystore encryption for API keys, user-controlled model queries, and opt-in diagnostics.

### 3. `README.md`
- User-facing links (Issues, Releases, Discussions) point to `https://github.com/jamesfnaim-blip/pocketforge`. Upstream credit retained in the Credits section.

### 4. `NOTICE` & `DeveloperCreditsScreen.kt`
- Preserved copyright notice and upstream credits: "Based on Mobile Harness (MIT) by techjarves".

### 5. GitHub Issue Templates
- Created `.github/ISSUE_TEMPLATE/bug_report.md` and `.github/ISSUE_TEMPLATE/feature_request.md` pre-populated for PocketForge AI repository.
