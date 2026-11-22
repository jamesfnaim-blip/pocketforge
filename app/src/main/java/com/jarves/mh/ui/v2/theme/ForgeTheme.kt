package com.jarves.mh.ui.v2.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.jarves.mh.ui.theme.AppThemeMode

object ForgeTheme {
    val colors: ForgeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalForgeColors.current

    val typography: ForgeTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalForgeTypography.current

    val shapes: ForgeShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalForgeShapes.current
}

@Composable
fun ForgeTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    shapes: ForgeShapes = ForgeShapes(),
    typography: ForgeTypography = ForgeTypography(),
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    val forgeColors = when (themeMode) {
        AppThemeMode.SYSTEM -> if (isSystemDark) ObsidianColors else LightColors
        AppThemeMode.DARK -> ObsidianColors
        AppThemeMode.AMOLED -> AmoledColors
        AppThemeMode.LIGHT -> LightColors
    }

    val materialColors = if (forgeColors.isDark) {
        darkColorScheme(
            primary = forgeColors.primary,
            secondary = forgeColors.accent,
            background = forgeColors.background,
            surface = forgeColors.surface,
            surfaceVariant = forgeColors.surfaceElevated,
            error = forgeColors.error,
            outline = forgeColors.border,
            onPrimary = LightColors.textPrimary,
            onSecondary = LightColors.textPrimary,
            onBackground = forgeColors.textPrimary,
            onSurface = forgeColors.textPrimary,
            onSurfaceVariant = forgeColors.textSecondary,
        )
    } else {
        lightColorScheme(
            primary = forgeColors.primary,
            secondary = forgeColors.accent,
            background = forgeColors.background,
            surface = forgeColors.surface,
            surfaceVariant = forgeColors.surfaceElevated,
            error = forgeColors.error,
            outline = forgeColors.border,
            onPrimary = LightColors.surface,
            onSecondary = LightColors.surface,
            onBackground = forgeColors.textPrimary,
            onSurface = forgeColors.textPrimary,
            onSurfaceVariant = forgeColors.textSecondary,
        )
    }

    CompositionLocalProvider(
        LocalForgeColors provides forgeColors,
        LocalForgeShapes provides shapes,
        LocalForgeTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content,
        )
    }
}
