# Linux Subsystem Architecture in Mobile Harness

This document provides a comprehensive technical breakdown of how **Mobile Harness** runs a complete, isolated Linux subsystem inside Android without root access (`su`).

---

## 1. Core Operating Principle: Rootless PRoot

Android restricts access to system directories (`/`, `/usr`, `/etc`, `/bin`) and prevents regular apps from mounting filesystems using the kernel `chroot()` or `mount()` system calls.

To circumvent these restrictions without rooting the device, Mobile Harness integrates a specialized fork of **PRoot** (`source/third_party/proot`), an open-source implementation of `chroot`, `mount --bind`, and `binfmt_misc` in pure user space.

```
+---------------------------------------------------------------------------------+
|                               Android OS Kernel                                 |
+---------------------------------------------------------------------------------+
                                       ▲
                                       │ (ptrace / seccomp interception)
+--------------------------------------┴------------------------------------------+
|  Tracer Process: PRoot (libproot.so + libtalloc.so + libandroid-shmem.so)      |
|  - Intercepts sys_open, sys_execve, sys_stat, sys_socket                        |
|  - Rewrites virtual paths (e.g. /usr/bin -> /data/data/.../ubuntu/usr/bin)     |
|  - Emulates fake root UID 0 (fake_id0 extension)                                |
+---------------------------------------------------------------------------------+
                                       ▲
                                       │ (child process execution)
+--------------------------------------┴------------------------------------------+
|  Tracee Process: Ubuntu Subsystem Child                                         |
|  (/bin/bash, /usr/local/bin/node, /usr/bin/git, claude, agy, dsh)               |
+---------------------------------------------------------------------------------+
```

### The `ptrace` and `seccomp` Interception Mechanism
1. **System Call Hooking:** When a process inside the Linux subsystem makes a system call (e.g., `open("/etc/resolv.conf")` or `execve("/bin/ls")`), the Linux kernel pauses the process and notifies PRoot via the `ptrace(PTRACE_SYSCALL, ...)` API.
2. **Path Canonicalization:** PRoot reads the requested path from the child process's CPU registers and memory, checks its internal mount table, and rewrites the path:
   ```text
   /usr/bin/node  -->  /data/data/com.jarves.mh/files/runtime/ubuntu/usr/bin/node
   /workspace     -->  /data/data/com.jarves.mh/files/projects/<project-slug>
   /tmp           -->  /data/data/com.jarves.mh/cache
   ```
3. **Execution Resume:** PRoot modifies the register state to point to the translated path and instructs the kernel to proceed with the system call.

---

## 2. Native Binaries & ELF Packaging Workaround

Under standard Android Gradle Plugin (AGP) packaging, standalone ELF executables are dropped unless packaged as native shared libraries (`.so`). Mobile Harness uses a novel build-system trick in `source/app/src/main/cpp/CMakeLists.txt`:

```cmake
# AGP packages shared-library targets but not executable targets. Each carrier
# advertises the APK entry, then is replaced by the real executable after link.
add_library(proot-carrier SHARED executable_carrier.c)
set_target_properties(proot-carrier PROPERTIES OUTPUT_NAME proot)
add_dependencies(proot-carrier proot)
add_custom_command(TARGET proot-carrier POST_BUILD
    COMMAND "${CMAKE_COMMAND}" -E copy "$<TARGET_FILE:proot>" "$<TARGET_FILE:proot-carrier>"
)

add_library(prootloader-carrier SHARED executable_carrier.c)
set_target_properties(prootloader-carrier PROPERTIES OUTPUT_NAME prootloader)
add_dependencies(prootloader-carrier prootloader)
add_custom_command(TARGET prootloader-carrier POST_BUILD
    COMMAND "${CMAKE_COMMAND}" -E copy "$<TARGET_FILE:prootloader>" "$<TARGET_FILE:prootloader-carrier>"
)
```

### Native Binary Manifest

| Binary Name in APK | Actual Underlying Binary | Source Location | Role |
| :--- | :--- | :--- | :--- |
| **`libproot.so`** | `proot-bin` (Static ELF Executable) | `third_party/proot/src/` | Main PRoot syscall interception tracer daemon. |
| **`libprootloader.so`** | `prootloader-bin` (Static ELF Executable) | `third_party/proot/src/loader/` | Assembly bootstrap trampoline (`assembly.S`) injected into child processes to assist syscall hooking. |
| **`libpocketspawn.so`** | Shared Library (`libpocketspawn.so`) | `app/src/main/cpp/pocket_spawn.c` | JNI bridge managing PTY allocation, process groups, and process spawning. |
| **`libtalloc.so`** | Shared Library (`libtalloc.so.2`) | `third_party/talloc/talloc.c` | Hierarchical memory allocator required by PRoot. |
| **`libandroid-shmem.so`** | Shared Library (`libandroid-shmem.so`) | `third_party/libandroid-shmem/shmem.c` | Intercepts SysV IPC shared memory and maps it to Android `/dev/ashmem`. |

---

## 3. Filesystem Layout & Storage Organization

All Linux files live inside the private internal storage directory of the application:

```text
/data/data/com.jarves.mh/
├── files/
│   ├── runtime/
│   │   ├── ubuntu/                         # Linux Guest Rootfs (Mounted as "/")
│   │   │   ├── bin/ -> usr/bin             # Standard Ubuntu symlinks
│   │   │   ├── etc/                        # System configurations (resolv.conf, hosts, passwd)
│   │   │   ├── home/                       # User home directory (/home)
│   │   │   ├── root/                       # Default login home (/root)
│   │   │   ├── usr/
│   │   │   │   ├── bin/                    # System utilities (bash, git, curl, tar, python3)
│   │   │   │   └── local/
│   │   │   │       ├── bin/                # Toolchain binaries (node, npm, claude, dsh, agy)
│   │   │   │       └── lib/                # Node.js modules and global packages
│   │   │   ├── workspace/                  # Mount point for active user projects
│   │   │   ├── .pocket-runtime-ready       # Marker created after successful installation
│   │   │   ├── .pocket-rootfs-version      # Contains "ubuntu-20.04.5-arm64"
│   │   │   └── .pocket-core-tools-version  # Contains version string (e.g. "core-bundle-2026.09.5")
│   │   └── runtime-downloads/              # Temporary cache for downloading .tar.zst bundles
│   └── projects/                           # Physical location of user project directories
│       ├── my-web-app/
│       └── hello-world/
└── cache/                                  # Android app cache (mounted as /tmp)
```

### Standard Bind Mounts Configured by Mobile Harness
When PRoot launches, `NativeSpawnProcess` and PRoot arguments configure several essential bind mounts:
- `-b /data/data/com.jarves.mh/files/projects/<slug>:/workspace`: Connects the user project directly to `/workspace`.
- `-b /dev`: Exposes Android's device nodes (`/dev/urandom`, `/dev/null`, `/dev/zero`, `/dev/ptmx`).
- `-b /proc`: Mounts Linux `/proc` so `ps`, top, and node can inspect processes and CPU information.
- `-b /sys`: Exposes system hardware trees.
- `-b /data/data/com.jarves.mh/cache:/tmp`: Provides a writable temporary directory.

---

## 4. Rootfs Download, Extraction & Verification

In `source/app/src/main/java/com/jarves/mh/runtime/RuntimeInstaller.kt`:

1. **Download & Hash Verification:**
   - The app reads `dist/runtime-bundles/manifest.json` which pins the SHA-256 checksum, compressed size, and uncompressed size of `pocketdev-core-arm64-2026.09.5.tar.zst`.
   - Downloads over HTTPS with resume support using standard `HttpURLConnection`.
   - Streams bytes through a `MessageDigest("SHA-256")` pipeline to guarantee zero bit corruption.

2. **Native Zstandard Streaming Decompression:**
   - Uses native `ZstdCompressorInputStream` (`com.github.luben:zstd-jni`):
     ```kotlin
     val zstd = ZstdCompressorInputStream(file.inputStream().buffered(64 * 1024))
     val tar = TarArchiveInputStream(zstd)
     ```
   - Avoids writing an intermediate uncompressed `.tar` file to disk, saving ~400 MB of mobile storage.

3. **Unix Metadata & Symlink Restoration:**
   - Android's Java File API does not preserve POSIX file permissions or symlinks.
   - `TarArchiveInputStream` iterates through each `TarArchiveEntry`:
     - **Directories:** Created with `mkdirs()`.
     - **Symlinks (`entry.isSymbolicLink`):** Created using `android.system.Os.symlink(entry.linkName, target.path)`.
     - **Executables:** Marked executable using `target.setExecutable(true, false)`.
     - **File Ownership:** Simulated as UID/GID `0` (`root`) via PRoot's `fake_id0` extension.

4. **Failure-Resilient Atomic Swap (`RootfsRepair.kt`):**
   - Extractions occur in a `.staging` directory.
   - If extraction is interrupted (e.g. user kills app or battery dies), `.staging` is wiped on next boot.
   - On completion, the old rootfs is renamed to `.backup`, `.staging` is renamed to `ubuntu/`, and user data in `/home` is preserved.

---

## 5. Environment Variables & Networking

### Environment Matrix
When launching a process inside PRoot, the following environment variables are strictly defined:

```bash
HOME=/root
USER=root
TERM=xterm-256color
LANG=C.UTF-8
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
TMPDIR=/tmp
SHELL=/bin/bash
PROOT_UNBUNDLE_LOADER=.
```

To prevent Android's dynamic linker from crashing guest Linux binaries, `pocket_launcher.c` unsets Android system linker variables:
```c
unsetenv("LD_LIBRARY_PATH");
unsetenv("LD_PRELOAD");
```

### DNS & Network Resolution
- Android does not use standard Linux `/etc/resolv.conf` (Android manages DNS via `netd` daemon and properties).
- Mobile Harness generates a static `/etc/resolv.conf` inside the guest rootfs:
  ```text
  nameserver 1.1.1.1
  nameserver 8.8.8.8
  ```
- All Linux network sockets (`AF_INET`, `AF_INET6`) route directly through Android's network stack without translation because PRoot does not sandbox network sockets. Loopback `127.0.0.1` is shared between Android and the Linux guest.

---

## 6. How the Terminal Talks to the Shell (Native PTY Architecture)

In `source/app/src/main/cpp/pocket_spawn.c`:

1. **Master/Slave Allocation:**
   ```c
   int master_fd = posix_openpt(O_RDWR | O_NOCTTY | O_CLOEXEC);
   grantpt(master_fd);
   unlockpt(master_fd);
   char *slave_name = ptsname(master_fd);
   ```

2. **Child Process Configuration (Fork & Attach):**
   ```c
   pid_t pid = fork();
   if (pid == 0) {
       setsid(); // Create new session
       int slave_fd = open(slave_name, O_RDWR);
       ioctl(slave_fd, TIOCSCTTY, 0); // TIOCSCTTY: Set slave as controlling terminal!
       
       struct winsize size = { .ws_row = pty_rows, .ws_col = pty_columns };
       ioctl(slave_fd, TIOCSWINSZ, &size); // TIOCSWINSZ: Set terminal window size!
       
       // Redirect stdio to slave PTY
       dup2(slave_fd, STDIN_FILENO);
       dup2(slave_fd, STDOUT_FILENO);
       dup2(slave_fd, STDERR_FILENO);
       close(slave_fd);
       close(master_fd);
       
       prctl(PR_SET_DUMPABLE, 1, 0, 0, 0); // Allow ptrace attachment under Android SELinux
       execve(argv[0], argv, envp);
   }
   ```

3. **Java I/O Pump (`NativeSpawnProcess.kt`):**
   - The master file descriptor (`master_fd`) is wrapped in a `ParcelFileDescriptor`.
   - A dedicated daemon thread (`pocket-pty-output`) reads bytes from `master_fd` and writes them into an output stream captured by `MainViewModel`.
   - User inputs typed into the Android terminal UI are written directly to the child's stdin stream.

---

## 7. Android Limitations & Engineered Workarounds

| Android Constraint | How It Breaks Standard Linux | Mobile Harness Engineering Workaround |
| :--- | :--- | :--- |
| **Android 15+ 16 KiB Page Size** | Binaries built with standard 4 KiB ELF segment alignment crash with `SIGSEGV` on modern devices. | Built with NDK 28 and linked with `-Wl,-z,max-page-size=16384` across all native targets. |
| **No Hard Links on App Data** | `link()` fails on `/data/data/...` under certain Android storage configurations. | PRoot's `link2symlink` extension intercepts `link()` system calls and emulates hard links as symbolic links. |
| **No SysV Shared Memory (`shmget`)** | Linux tools expecting System V IPC crash because Android lacks `/dev/shm`. | Integrates `libandroid-shmem` and PRoot `sysvipc` extensions to map shared memory requests to `/dev/ashmem`. |
| **Process Non-Dumpable Security** | Android sets apps non-dumpable, which prevents PRoot tracer from attaching `ptrace` to child processes. | `pocket_spawn.c` and `pocket_launcher.c` explicitly call `prctl(PR_SET_DUMPABLE, 1, 0, 0, 0)`. |
| **W^X Memory Protection** | Android 10+ denies `execve()` on binaries residing in `/data/data/...`. | Kept `targetSdk = 28` for direct installs; for Play builds, routes through `libprootloader.so` memory-mapped ELF loading. |
| **Parent Process Group Cleanup** | When stopping an agent or terminal task, child background processes stay orphaned. | `pocket_spawn.c` calls `setpgid(0, 0)`, and `NativeSpawn_kill` sends `SIGINT`/`SIGTERM` to `-pid` (the entire negative process group). |
