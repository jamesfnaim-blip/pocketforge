# License & Attribution Plan

## 1. Analysis of the Original License

The original **Mobile Harness** project (`source/LICENSE`) is licensed under the **MIT License**:

```text
MIT License

Copyright (c) 2026 Mobile Harness Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Third-Party Subcomponents & Bundled Licenses

The codebase links and embeds several external components that carry their own licenses:

| Component | Path | License | Key Obligations |
| :--- | :--- | :--- | :--- |
| **Mobile Harness (Root)** | `source/LICENSE` | **MIT** | Retain copyright notice & permission notice in all copies or substantial portions. |
| **PRoot** | `source/third_party/proot` / `assets/licenses/proot-GPL-2.0.txt` | **GPL-2.0** | Source code of PRoot modifications must be made available under GPL-2.0. |
| **Talloc** | `source/third_party/talloc` / `assets/licenses/talloc-LGPL-3.0-or-later.txt` | **LGPL-3.0-or-later** | Dynamic linking permitted; library modifications must be distributed under LGPL. |
| **libandroid-shmem** | `source/third_party/libandroid-shmem` / `assets/licenses/libandroid-shmem-BSD-3-Clause.txt` | **BSD 3-Clause** | Retain copyright notice, list of conditions, and disclaimer. |
| **Claude Code Android Port** | `assets/licenses/claude-code-android-MIT.txt` | **MIT** | Retain copyright notice and permission text. |
| **Zstandard JNI** | `com.github.luben:zstd-jni` | **BSD 2-Clause / GPLv2** | Retain copyright notice and disclaimers. |
| **Apache Commons Compress** | `org.apache.commons:commons-compress` | **Apache 2.0** | Retain copyright, Apache 2.0 license, and state changes if modified. |

---

## 2. Third-Party Agent Binaries & Legal Risks

Mobile Harness integrates three primary coding agents: **Claude Code**, **Antigravity CLI (`agy`)**, and **DeepSeek Harness (`dsh`)**. An inspection of `scripts/runtime-bundles/` reveals how the original project created and distributed these binary overlays:

### A. Claude Code (`pocketdev-claude-arm64-*.tar.zst`)
* **How Original Obtains It:** `scripts/runtime-bundles/build-claude-from-installed-android.sh` pulls the pre-installed executable `files/runtime/ubuntu/usr/local/bin/claude` from a test device via ADB, checks SHA-256 (`55cdb8e162d19...`, version 2.1.316), and archives it.
* **License & Terms:** Anthropic's Claude Code binary is proprietary software governed by Anthropic's Terms of Service and CLI Evaluation License. It is **NOT** open-source software.
* **Redistribution Risk:** Bundling or re-hosting Anthropic's compiled binaries on GitHub Releases or inside an APK constitutes unauthorized software redistribution and poses severe copyright infringement risks.

### B. Google Antigravity CLI (`pocketdev-agy-arm64-*.tar.zst`)
* **How Original Obtains It:** `scripts/runtime-bundles/build-agy-from-official.sh` downloads the binary directly from Google Cloud Storage (`storage.googleapis.com/antigravity-public/antigravity-cli/.../cli_linux_arm64.tar.gz`), unpacks the ELF binary, and compresses it.
* **License & Terms:** Proprietary to Google LLC. Governed by Google's Terms of Service. Note line 33 of `scripts/runtime-bundles/README.md`: *"Review Google's redistribution terms before publishing an APK containing this binary."*
* **Redistribution Risk:** Direct binary redistribution without Google partner authorization may violate Google API/CLI distribution terms.

### C. DeepSeek Harness (`pocketdev-dsh-arm64-*.tar.zst`)
* **How Original Obtains It:** `scripts/runtime-bundles/build-dsh-from-installed-android.sh` packages the npm installation `usr/local/lib/dsh/node_modules/.bin/dsh`.
* **License & Terms:** DeepSeek Harness is an open-source CLI published via npm under an open-source license.
* **Redistribution Risk:** Low, provided npm package license terms are observed.

### D. Architectural & Legal Recommendation for the New Project
> [!IMPORTANT]
> **Never redistribute proprietary agent binaries inside repository releases or APK assets.**
> 
> Instead, our application must implement a **Runtime Download & Bootstrap Model**:
> 1. The app should download only an open-source base Ubuntu rootfs (or compile it clean).
> 2. When the user selects **Claude Code**, the app runs `npm install -g @anthropic-ai/claude-code` or downloads directly from Anthropic's official endpoint on the user's behalf onto their device.
> 3. When the user selects **Antigravity**, the app downloads the client directly from Google's official public endpoint to the user's local phone storage.
> 4. When the user selects **DeepSeek**, the app installs it via `npm install` inside the Linux environment.
>
> This ensures our open-source project remains 100% compliant with intellectual property laws and avoids unauthorized binary redistribution.

*Marked Status:* UNVERIFIED whether Anthropic permits third-party CLI wrappers to automate initial download tokens; to verify, check Anthropic's developer terms regarding automated npm installation.

---

## 3. What Must Be Kept

Under the MIT License and associated third-party licenses, the following requirements are mandatory:

1. **Original Copyright Notice:**
   `Copyright (c) 2026 Mobile Harness Contributors` must be preserved in all derived code files that originate from Mobile Harness.
2. **Full MIT License Text:**
   The exact text of the MIT license must be included with any distribution of the source code and within the app.
3. **Third-Party Open-Source Notices:**
   The `NOTICE` file and all subcomponent license files (`assets/licenses/*`) must be preserved and kept up-to-date.
4. **Never Remove Original Notices:**
   Original author attributions and copyright headers at the top of existing source files (e.g., C/C++ files, scripts, and Kotlin files) must never be stripped or altered.

---

## 4. License Selection for the New Project

### Recommended License: **MIT License**
- The Android application layer (Kotlin, Jetpack Compose UI, services, vaults) can cleanly remain under the **MIT License**.
- The `third_party/proot` submodule itself must remain **GPL-2.0**.
- Because PRoot runs as a separate subprocess invoked via `fork()`/`execve()` through `libpocketspawn.so`, standard GPL boundary isolation applies to the separate Android GUI host process.

---

## 5. Attribution Matrix: Where Notices Must Appear

To ensure complete legal compliance and ethical attribution, notices will appear in three distinct locations:

### A. Git Repository Root
- **`LICENSE`**: The primary project license (MIT License for the new project).
- **`NOTICE`**: Explicit attribution file listing:
  ```text
  This product includes software developed by:
  1. Mobile Harness Contributors (https://github.com/techjarves/Mobile-Harness)
     Licensed under the MIT License. Copyright (c) 2026 Mobile Harness Contributors.
  2. PRoot project (https://github.com/termux/proot)
     Licensed under the GNU General Public License v2.
  3. libandroid-shmem (https://github.com/termux/libandroid-shmem)
     Licensed under the BSD 3-Clause License.
  4. Samba talloc (https://talloc.samba.org)
     Licensed under the GNU Lesser General Public License v3.0+.
  ```

### B. `README.md`
- A dedicated **"Credits & Acknowledgments"** section at the bottom of the README with clickable links to the original upstream repositories, prominently thanking Mobile Harness and its contributors.

### C. In-App User Interface ("Developer & Credits" Screen)
- Under **Settings → Developer & Credits**:
  - **Developer Profile Card (Configurable Placeholders):**
    - Name: `{YOUR_NAME}`
    - Website: `{YOUR_WEBSITE}`
    - LinkedIn: `{YOUR_LINKEDIN}`
  - **Open-Source Credits Section:**
    - "Based on the pioneering architecture of **Mobile Harness** by Mobile Harness Contributors."
    - Button: **"View Mobile Harness Repository"** (opens `https://github.com/techjarves/Mobile-Harness`).
    - Button: **"Third-Party Open-Source Licenses"** (opens a scrollable modal displaying the full license texts from `assets/licenses/`).
