package com.jarves.mh.ui.v2.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.chat.ChatScreen
import com.jarves.mh.ui.v2.components.ForgeButton
import com.jarves.mh.ui.v2.components.ForgeButtonStyle
import com.jarves.mh.ui.v2.components.ForgeCard
import com.jarves.mh.ui.v2.components.ForgeCardStyle
import com.jarves.mh.ui.v2.home.HomeScreen
import com.jarves.mh.ui.v2.setup.SetupWizardScreen
import com.jarves.mh.ui.v2.state.ForgeAppState
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun ForgeNavHost(
    state: ForgeAppState,
    onNavigate: (ForgeNavRoute) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onToggleLegacyUi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.currentRoute) {
        is ForgeNavRoute.SetupWizard -> {
            SetupWizardScreen(
                currentLanguageCode = state.languageCode,
                onLanguageSelected = onLanguageSelected,
                onSetupComplete = { onNavigate(ForgeNavRoute.Home) },
                modifier = modifier,
            )
        }
        is ForgeNavRoute.Home -> {
            HomeScreen(
                isUbuntuRunning = state.isUbuntuRunning,
                serverUrl = state.devServerUrl,
                onStartNewTask = { onNavigate(ForgeNavRoute.Chat) },
                onOpenProject = { onNavigate(ForgeNavRoute.Chat) },
                onNewProject = { onNavigate(ForgeNavRoute.Chat) },
                onCloneRepo = { onNavigate(ForgeNavRoute.Git) },
                onTemplates = { onNavigate(ForgeNavRoute.Templates) },
                onOpenClassicUi = onToggleLegacyUi,
                modifier = modifier,
            )
        }
        is ForgeNavRoute.Chat -> {
            ChatScreen(
                projectName = state.activeProjectName ?: "PocketForge AI",
                appMode = state.appMode,
                onBack = { onNavigate(ForgeNavRoute.Home) },
                onOpenTerminal = { onNavigate(ForgeNavRoute.Terminal) },
                onOpenPreview = { onNavigate(ForgeNavRoute.Preview) },
                modifier = modifier,
            )
        }
        else -> {
            // Milestone placeholder card for routes in progress (UI-3 to UI-8)
            PlaceholderRouteScreen(
                route = state.currentRoute,
                onToggleLegacyUi = onToggleLegacyUi,
                onBack = { onNavigate(ForgeNavRoute.Home) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PlaceholderRouteScreen(
    route: ForgeNavRoute,
    onToggleLegacyUi: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ForgeCard(
            style = ForgeCardStyle.DEFAULT,
            contentPadding = 20.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PhosphorIcon(
                    icon = route.icon,
                    size = 40.dp,
                    tint = colors.primary,
                )
                Text(
                    text = "${route.route.replace('_', ' ').uppercase()} Screen",
                    style = typography.titleMedium,
                    color = colors.textPrimary,
                )
                Text(
                    text = "This screen is scheduled in the UI roadmap. You can view it in the approved mockup, or switch to the Classic UI view to test the underlying engine.",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )
                ForgeButton(
                    text = "Switch to Classic UI",
                    onClick = onToggleLegacyUi,
                    style = ForgeButtonStyle.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                )
                ForgeButton(
                    text = "Back to Home",
                    onClick = onBack,
                    style = ForgeButtonStyle.OUTLINE,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
