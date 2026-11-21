package com.jarves.mh.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Obsidian Dark Scheme (Default) ---
private val ObsidianColors = darkColorScheme(
    primary = ForgeElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = ForgeCyberCyan,
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFE0F7FA),
    tertiary = ForgeEmerald,
    onTertiary = Color.White,
    background = ForgeObsidianBackground,
    onBackground = ForgeObsidianTextPrimary,
    surface = ForgeObsidianSurface,
    onSurface = ForgeObsidianTextPrimary,
    surfaceVariant = ForgeObsidianSurfaceElevated,
    onSurfaceVariant = ForgeObsidianTextSecondary,
    outline = ForgeObsidianBorder,
    outlineVariant = Color(0xFF333B4A),
    error = ForgeRose,
    onError = Color.White,
)

// --- AMOLED Black Scheme (100% Pure Black for OLED Battery Savings) ---
private val AmoledColors = darkColorScheme(
    primary = ForgeElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = ForgeCyberCyan,
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF003333),
    onSecondaryContainer = Color(0xFFE0F7FA),
    tertiary = ForgeEmerald,
    onTertiary = Color.White,
    background = ForgeAmoledBackground,
    onBackground = ForgeAmoledTextPrimary,
    surface = ForgeAmoledSurface,
    onSurface = ForgeAmoledTextPrimary,
    surfaceVariant = ForgeAmoledSurfaceElevated,
    onSurfaceVariant = ForgeAmoledTextSecondary,
    outline = ForgeAmoledBorder,
    outlineVariant = Color(0xFF262626),
    error = ForgeRose,
    onError = Color.White,
)

// --- Clean Light Scheme ---
private val CleanLightColors = lightColorScheme(
    primary = ForgeElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECFEFF),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = ForgeLightBackground,
    onBackground = ForgeLightTextPrimary,
    surface = ForgeLightSurface,
    onSurface = ForgeLightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = ForgeLightTextSecondary,
    outline = ForgeLightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = ForgeRose,
    onError = Color.White,
)

enum class AppThemeMode { SYSTEM, DARK, LIGHT, AMOLED }

@Composable
fun ForgeTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemDark
    }

    val colorScheme = when (themeMode) {
        AppThemeMode.AMOLED -> AmoledColors
        AppThemeMode.DARK -> ObsidianColors
        AppThemeMode.LIGHT -> CleanLightColors
        AppThemeMode.SYSTEM -> if (isSystemDark) ObsidianColors else CleanLightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ForgeTypography,
        content = content,
    )
}

@Composable
fun PocketTheme(themeMode: AppThemeMode = AppThemeMode.SYSTEM, content: @Composable () -> Unit) {
    ForgeTheme(themeMode = themeMode, content = content)
}
