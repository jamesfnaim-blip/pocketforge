# Security, Cryptography & Sandboxing Model

This document analyzes the security architecture of **Mobile Harness**, identifies inherent security risks associated with running autonomous AI agents and a Linux subsystem on Android, and specifies security enhancements for the new product.

---

## 1. Cryptographic Architecture: Android Keystore & Secret Storage

In `source/app/src/main/java/com/jarves/mh/data/ApiKeyVault.kt`:

### Master Key Generation
Mobile Harness stores master encryption keys in the **Android Keystore System** (`AndroidKeyStore`), ensuring private cryptographic material never exists unencrypted in user storage:

```kotlin
val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
keyGenerator.init(
    KeyGenParameterSpec.Builder("pocket-provider-key", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .setRandomizedEncryptionRequired(true)
        .build()
)
val secretKey = keyGenerator.generateKey()
```

### Encryption & Decryption Scheme
- **Algorithm:** `AES/GCM/NoPadding` (Galois/Counter Mode).
- **Key Length:** 256 bits.
- **Authentication Tag:** 128-bit authentication tag appended to the ciphertext to ensure tamper detection.
- **Initialization Vector (IV):** A unique 12-byte cryptographically secure random IV is generated for every encryption call.
- **Storage:** Ciphertext and IV are Base64-encoded and stored in private app SharedPreferences (`pocket_secrets.xml`), protected by standard Android sandbox file permissions (`MODE_PRIVATE`, `0600`).
- **Hardware Backing:** On modern Android devices, the Keystore keys are protected by hardware-backed security modules: the **Trusted Execution Environment (TEE)** or **StrongBox Keymaster** chip. Even on a rooted phone, the raw private key cannot be extracted from hardware.

---

## 2. Android Permission Model Analysis

In `source/app/src/main/AndroidManifest.xml`:

| Permission | Protection Level | Purpose & Risk Justification |
| :--- | :--- | :--- |
| `android.permission.INTERNET` | Normal | Required for downloading the Ubuntu rootfs, calling LLM provider APIs (Anthropic, OpenRouter), and git cloning. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Checks network connectivity before initiating large downloads or API pings. |
| `android.permission.POST_NOTIFICATIONS` | Runtime (Dangerous) | Required on Android 13+ to post persistent foreground notifications for background tasks. |
| `android.permission.FOREGROUND_SERVICE` | Normal | Allows running background services without being stopped when the app is minimized. |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | Normal (Restricted) | Explicit Android 14 requirement for developer tools executing long-running local tasks. |
| `android.permission.WAKE_LOCK` | Normal | Keeps CPU awake during builds and agent reasoning turns when the screen is off. |
| `android.permission.REQUEST_INSTALL_PACKAGES` | Signature / Special | Allows the app to trigger installation of debug APKs compiled on-device. |
| `android.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION` | Normal | Permits seamless self-updating when upgrading via the built-in app updater. |

---

## 3. Sandboxing & Isolation Boundaries

### What is Protected by Default:
1. **App Sandbox Boundary:** Mobile Harness runs under its own unique Linux UID (e.g. `u0_a245`). Other Android apps on the phone cannot access Mobile Harness's private files, projects, or rootfs.
2. **Rootless Subsystem Execution:** Although PRoot simulates root (`UID 0`) inside the guest environment, this is an illusion created via syscall interception (`fake_id0`). The process cannot escape Android's kernel sandbox; it cannot access system partitions (`/system`, `/vendor`) in write mode, nor can it inspect data belonging to other Android apps.

---

## 4. Threat Model & Identified Vulnerabilities in the Original

A deep architectural review identifies four key security risks in the original implementation:

### 1. Plaintext Secrets in `/proc/<pid>/environ`
* **Vulnerability:** When `ClaudeRuntimeBridge` launches Claude Code, it passes API keys (`ANTHROPIC_API_KEY`, `CLAUDE_CODE_OAUTH_TOKEN`) in the process environment variables (`char **envp`).
* **Risk:** Any process or script executed within the same Linux environment (e.g., an npm package running a post-install script, or a third-party Python tool) can read `/proc/<pid>/environ` of sibling processes sharing the same Android UID and steal the user's API keys!

### 2. Autonomous Prompt Injection & Destructive Bash Execution
* **Vulnerability:** Coding agents have access to a full bash shell. If an agent is fed malicious input (e.g., a prompt instructing it to examine an untrusted GitHub repository that contains a poisoned README), the agent could be tricked into executing destructive commands (`rm -rf /workspace`, or exfiltrating private files to a remote server).
* **Mitigation Gap:** While the original app includes an `ApprovalCard`, users frequently habituate to clicking "Approve" without reading raw command text.

### 3. Cleartext Traffic in Guest Environment
* **Vulnerability:** While `AndroidManifest.xml` specifies `android:usesCleartextTraffic="false"`, this applies only to Android's internal network stacks (e.g. `HttpURLConnection`). Processes running inside the Linux guest (curl, node, python) bypass Android's network security config and can communicate over unencrypted HTTP if not carefully restricted.

### 4. Memory Hygiene
* **Vulnerability:** Strings in Kotlin/Java are immutable and remain in the JVM garbage collector heap until collected. When API keys are converted to `String` instances, they linger in memory and could theoretically be inspected in heap dumps.

---

## 5. Security Enhancements for the New Product

Our new implementation will implement defense-in-depth security hardening:

### A. Ephemeral Credential Delivery (No Environment Leaks)
- Instead of setting long-lived environment variables in child processes, provide secrets via **named Unix pipes (`mkfifo`)** or direct stdin streaming that are closed immediately after the agent authenticates.
- For processes that must read `/proc`, mount a restricted `/proc` inside PRoot with `hidepid=2` semantics to prevent unprivileged child processes from inspecting sibling environments.

### B. Memory Sanitization
- Replace `String` passwords and keys with `CharArray` or `ByteArray`.
- Immediately overwrite sensitive buffers with zeros (`java.util.Arrays.fill(keyArray, 0.toByte())`) after passing them to the JNI layer or crypto cipher.

### C. Fine-Grained Policy Engine for Autonomous Tools
- Implement an automated risk classifier:
  - **Auto-Allowed:** Safe read-only operations (`cat`, `ls`, `grep`, `git diff`, `npm test`).
  - **Warning Required:** Modifying local project files.
  - **High-Risk Hard Stop:** Network requests (`curl`, `wget`, `ssh`), global package installs (`npm install -g`), or file deletions (`rm`).
- The UI will explicitly display exact diff previews and highlight external network destinations before granting permission.

### D. Password-Protected AES-GCM Encrypted Backups
- The `.mhbackup` export will enforce **Argon2id** or **PBKDF2-HMAC-SHA256** (with 100,000+ iterations) to derive a 256-bit encryption key from user passwords before encrypting project databases and credentials.
