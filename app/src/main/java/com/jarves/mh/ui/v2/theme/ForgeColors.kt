package com.jarves.mh.ui.v2.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ForgeColors(
    val primary: Color,
    val primaryHover: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val terminalBg: Color,
    val isDark: Boolean,
)

// Electric Indigo & Cyber Cyan Accent System
val BrandIndigo = Color(0xFF6366F1)
val BrandIndigoDark = Color(0xFF4F46E5)
val BrandCyan = Color(0xFF06B6D4)
val StatusEmerald = Color(0xFF10B981)
val StatusAmber = Color(0xFFF59E0B)
val StatusRose = Color(0xFFEF4444)
val TerminalBlack = Color(0xFF07080A)

// Obsidian Dark (Default)
val ObsidianColors = ForgeColors(
    primary = BrandIndigo,
    primaryHover = BrandIndigoDark,
    accent = BrandCyan,
    success = StatusEmerald,
    warning = StatusAmber,
    error = StatusRose,
    background = Color(0xFF0D0F12),
    surface = Color(0xFF16191E),
    surfaceElevated = Color(0xFF1E232B),
    border = Color(0xFF262B35),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    terminalBg = TerminalBlack,
    isDark = true,
)

// AMOLED True Black (OLED Battery Saver)
val AmoledColors = ForgeColors(
    primary = BrandIndigo,
    primaryHover = BrandIndigoDark,
    accent = BrandCyan,
    success = StatusEmerald,
    warning = StatusAmber,
    error = StatusRose,
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceElevated = Color(0xFF141414),
    border = Color(0xFF222222),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA1A1AA),
    textMuted = Color(0xFF71717A),
    terminalBg = Color(0xFF000000),
    isDark = true,
)

// Clean Light
val LightColors = ForgeColors(
    primary = BrandIndigo,
    primaryHover = BrandIndigoDark,
    accent = BrandCyan,
    success = StatusEmerald,
    warning = StatusAmber,
    error = StatusRose,
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    border = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    textMuted = Color(0xFF94A3B8),
    terminalBg = Color(0xFF0F172A),
    isDark = false,
)

val LocalForgeColors = staticCompositionLocalOf { ObsidianColors }
