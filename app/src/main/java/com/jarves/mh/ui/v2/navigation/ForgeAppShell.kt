package com.jarves.mh.ui.v2.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jarves.mh.R
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.theme.ThemeMode
import com.jarves.mh.ui.v2.components.ForgeAppMode
import com.jarves.mh.ui.v2.components.ForgeModeSwitch
import com.jarves.mh.ui.v2.state.ForgeAppState
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun ForgeAppShell(
    state: ForgeAppState,
    onNavigate: (ForgeNavRoute) -> Unit,
    onModeChange: (ForgeAppMode) -> Unit,
    onThemeToggle: () -> Unit,
    onLanguageToggle: () -> Unit,
    onToggleLegacyUi: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            ForgeTopBar(
                state = state,
                onModeChange = onModeChange,
                onThemeToggle = onThemeToggle,
                onLanguageToggle = onLanguageToggle,
                onToggleLegacyUi = onToggleLegacyUi,
            )

            HorizontalDivider(thickness = 1.dp, color = colors.border)

            // BODY CONTENT
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                content()
            }

            // BOTTOM NAVIGATION BAR (Shown for standard bottom destinations)
            if (state.currentRoute.isBottomBarItem || state.currentRoute is ForgeNavRoute.Home) {
                HorizontalDivider(thickness = 1.dp, color = colors.border)
                ForgeBottomNavBar(
                    currentRoute = state.currentRoute,
                    appMode = state.appMode,
                    onNavigate = onNavigate,
                )
            }
        }
    }
}

@Composable
private fun ForgeTopBar(
    state: ForgeAppState,
    onModeChange: (ForgeAppMode) -> Unit,
    onThemeToggle: () -> Unit,
    onLanguageToggle: () -> Unit,
    onToggleLegacyUi: () -> Unit,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Brand Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(shapes.chip)
                    .background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                PhosphorIcon(
                    icon = PhosphorIconType.CODE,
                    size = 16.dp,
                    tint = Color.White,
                )
            }
            Column {
                Text(
                    text = "PocketForge AI",
                    style = typography.titleSmall,
                    color = colors.textPrimary,
                )
                Text(
                    text = if (state.isUbuntuRunning) "Ubuntu 20.04 Active" else "Ready",
                    style = typography.labelSmall,
                    color = if (state.isUbuntuRunning) colors.success else colors.textMuted,
                )
            }
        }

        // Action Pill Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Mode Switch (Simple / Pro)
            ForgeModeSwitch(
                currentMode = state.appMode,
                onModeChange = onModeChange,
            )

            // Language Toggle (EN / বাংলা)
            Surface(
                onClick = onLanguageToggle,
                shape = shapes.pill,
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            ) {
                Text(
                    text = if (state.languageCode == "bn") "বাংলা" else "EN",
                    style = typography.labelSmall,
                    color = colors.accent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                )
            }

            // Theme Toggle
            Surface(
                onClick = onThemeToggle,
                shape = shapes.pill,
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            ) {
                PhosphorIcon(
                    icon = PhosphorIconType.SPARKLE,
                    size = 14.dp,
                    tint = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun ForgeBottomNavBar(
    currentRoute: ForgeNavRoute,
    appMode: ForgeAppMode,
    onNavigate: (ForgeNavRoute) -> Unit,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    val items = ForgeNavRoute.bottomNavRoutes.filter { route ->
        !route.isProOnly || appMode == ForgeAppMode.PRO
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(colors.surface)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val isSelected = currentRoute.route == item.route
            val itemColor = if (isSelected) colors.primary else colors.textMuted

            Column(
                modifier = Modifier
                    .clip(ForgeTheme.shapes.button)
                    .clickable { onNavigate(item) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PhosphorIcon(
                    icon = item.icon,
                    size = 20.dp,
                    tint = itemColor,
                )
                Text(
                    text = stringResource(id = item.titleRes),
                    style = typography.labelSmall,
                    color = itemColor,
                )
            }
        }
    }
}
