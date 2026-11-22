package com.jarves.mh.ui.v2.state

import androidx.compose.runtime.Immutable
import com.jarves.mh.ui.theme.AppThemeMode
import com.jarves.mh.ui.v2.components.ForgeAppMode
import com.jarves.mh.ui.v2.navigation.ForgeNavRoute

@Immutable
data class ForgeAppState(
    val currentRoute: ForgeNavRoute = ForgeNavRoute.Home,
    val appMode: ForgeAppMode = ForgeAppMode.SIMPLE,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val languageCode: String = "en",
    val isUbuntuRunning: Boolean = false,
    val ubuntuPid: Int? = null,
    val devServerUrl: String? = null,
    val activeProjectId: String? = null,
    val activeProjectName: String? = null,
    val isSetupCompleted: Boolean = true,
    val isTabletLayout: Boolean = false,
    val showLegacyUi: Boolean = false,
)
