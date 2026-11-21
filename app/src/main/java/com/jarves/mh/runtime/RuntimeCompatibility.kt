package com.jarves.mh.runtime

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.system.Os
import android.system.OsConstants
import java.io.File

internal data class HardwarePreflightReport(
    val isArm64: Boolean,
    val primaryAbi: String,
    val totalRamBytes: Long,
    val availableDiskBytes: Long,
    val pageSizeBytes: Long,
    val is16KbPageSize: Boolean,
    val isCompatible: Boolean,
    val ramWarning: Boolean,
    val diskWarning: Boolean,
) {
    val totalRamGb: Double get() = totalRamBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
    val availableDiskGb: Double get() = availableDiskBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
    val pageSizeKb: Long get() = pageSizeBytes / 1024L
}

internal fun supportsArm64Runtime(supportedAbis: Array<String>, osArchitecture: String?): Boolean {
    // x86 emulators with ARM translation advertise arm64-v8a too, but PRoot needs a native ARM64
    // kernel. Reject x86 explicitly rather than requiring os.arch == "aarch64": some vendor
    // builds (e.g. vivo OriginOS) report other values on genuine ARM64 hardware.
    val x86 = supportedAbis.any { it.startsWith("x86", ignoreCase = true) } ||
        osArchitecture.orEmpty().let { it.startsWith("x86", ignoreCase = true) || it.startsWith("i686", ignoreCase = true) }
    return !x86 && supportedAbis.any { it.equals("arm64-v8a", ignoreCase = true) }
}

internal fun getSystemPageSizeBytes(): Long {
    return try {
        Os.sysconf(OsConstants._SC_PAGESIZE)
    } catch (_: Throwable) {
        4096L
    }
}

internal fun checkHardwarePreflight(context: Context): HardwarePreflightReport {
    val arm64 = supportsArm64Runtime(Build.SUPPORTED_ABIS, System.getProperty("os.arch"))
    val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
    val activityManager = context.getSystemService(ActivityManager::class.java)
    val memInfo = ActivityManager.MemoryInfo().also { activityManager?.getMemoryInfo(it) }
    val totalRam = memInfo.totalMem
    val freeDisk = try { context.filesDir.usableSpace } catch (_: Throwable) { 0L }
    val pageSize = getSystemPageSizeBytes()
    val is16Kb = pageSize >= 16384L

    // Minimum requirements: ARM64 is strictly required. RAM and disk have advisory warnings.
    val ramWarning = totalRam in 1 until (3L * 1024 * 1024 * 1024) // Warn if < 3GB usable RAM
    val diskWarning = freeDisk in 1 until (2L * 1024 * 1024 * 1024) // Warn if < 2GB free storage

    return HardwarePreflightReport(
        isArm64 = arm64,
        primaryAbi = primaryAbi,
        totalRamBytes = totalRam,
        availableDiskBytes = freeDisk,
        pageSizeBytes = pageSize,
        is16KbPageSize = is16Kb,
        isCompatible = arm64,
        ramWarning = ramWarning,
        diskWarning = diskWarning,
    )
}
