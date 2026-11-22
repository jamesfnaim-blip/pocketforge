package com.jarves.mh.ui.v2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarves.mh.ui.MainViewModel
import com.jarves.mh.ui.PocketDevApp
import com.jarves.mh.ui.theme.ThemeMode
import com.jarves.mh.ui.v2.components.ForgeAppMode
import com.jarves.mh.ui.v2.navigation.ForgeAppShell
import com.jarves.mh.ui.v2.navigation.ForgeNavHost
import com.jarves.mh.ui.v2.navigation.ForgeNavRoute
import com.jarves.mh.ui.v2.state.ForgeAppState
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun ForgeApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    var showLegacyUi by remember { mutableStateOf(false) }
    var currentRoute by remember {
        mutableStateOf<ForgeNavRoute>(
            if (!uiState.onboardingComplete) ForgeNavRoute.SetupWizard else ForgeNavRoute.Home
        )
    }
    var appMode by remember { mutableStateOf(ForgeAppMode.SIMPLE) }
    var currentThemeMode by remember { mutableStateOf(ThemeMode.DARK) }

    if (showLegacyUi) {
        // Fallback to legacy UI during migration so user never loses access to any sub-feature
        PocketDevApp(viewModel)
    } else {
        val appState = ForgeAppState(
            currentRoute = currentRoute,
            appMode = appMode,
            themeMode = currentThemeMode,
            languageCode = uiState.languageCode,
            isUbuntuRunning = uiState.onboardingComplete,
            activeProjectId = uiState.workspace?.project?.name,
            activeProjectName = uiState.workspace?.project?.name ?: "PocketForge AI",
            isSetupCompleted = uiState.onboardingComplete,
            showLegacyUi = showLegacyUi,
        )

        ForgeTheme(themeMode = currentThemeMode) {
            ForgeAppShell(
                state = appState,
                onNavigate = { newRoute -> currentRoute = newRoute },
                onModeChange = { newMode -> appMode = newMode },
                onThemeToggle = {
                    currentThemeMode = when (currentThemeMode) {
                        ThemeMode.DARK -> ThemeMode.AMOLED
                        ThemeMode.AMOLED -> ThemeMode.LIGHT
                        ThemeMode.LIGHT -> ThemeMode.DARK
                        ThemeMode.SYSTEM -> ThemeMode.DARK
                    }
                },
                onLanguageToggle = {
                    val nextLang = if (uiState.languageCode == "bn") "en" else "bn"
                    viewModel.setLanguage(nextLang)
                },
                onToggleLegacyUi = { showLegacyUi = !showLegacyUi },
                modifier = modifier,
            ) {
                ForgeNavHost(
                    state = appState,
                    onNavigate = { newRoute -> currentRoute = newRoute },
                    onLanguageSelected = { lang -> viewModel.setLanguage(lang) },
                    onToggleLegacyUi = { showLegacyUi = !showLegacyUi },
                )
            }
        }
    }
}
