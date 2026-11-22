package com.jarves.mh.ui.v2.navigation

import androidx.annotation.StringRes
import com.jarves.mh.R
import com.jarves.mh.ui.components.PhosphorIconType

sealed class ForgeNavRoute(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: PhosphorIconType,
    val isBottomBarItem: Boolean = false,
    val isProOnly: Boolean = false,
) {
    // Setup / Onboarding
    data object SetupWizard : ForgeNavRoute("setup_wizard", R.string.preflight_title, PhosphorIconType.SHIELD_CHECK)

    // Core Tabs
    data object Home : ForgeNavRoute("home", R.string.nav_home, PhosphorIconType.HOUSE, isBottomBarItem = true)
    data object Chat : ForgeNavRoute("chat", R.string.nav_agent, PhosphorIconType.CHAT_CIRCLE_DOTS, isBottomBarItem = true)
    data object Projects : ForgeNavRoute("projects", R.string.nav_projects, PhosphorIconType.FOLDER_SIMPLE, isBottomBarItem = true)
    data object Terminal : ForgeNavRoute("terminal", R.string.nav_terminal, PhosphorIconType.TERMINAL, isBottomBarItem = true, isProOnly = true)
    data object Settings : ForgeNavRoute("settings", R.string.nav_settings, PhosphorIconType.GEAR_SIX, isBottomBarItem = true)

    // Sub-screens & Tools
    data object Editor : ForgeNavRoute("editor", R.string.tree_edit, PhosphorIconType.CODE)
    data object Preview : ForgeNavRoute("preview", R.string.tree_prev, PhosphorIconType.ARROW_SQUARE_OUT)
    data object Builder : ForgeNavRoute("builder", R.string.tree_build, PhosphorIconType.HAMMER)
    data object Git : ForgeNavRoute("git", R.string.tree_git, PhosphorIconType.GIT_BRANCH)
    data object Templates : ForgeNavRoute("templates", R.string.tree_temp, PhosphorIconType.PLUS)
    data object Tasks : ForgeNavRoute("tasks", R.string.tree_task, PhosphorIconType.SPARKLE)
    data object BatteryWizard : ForgeNavRoute("battery_wizard", R.string.tree_batt, PhosphorIconType.WARNING_CIRCLE)
    data object DeveloperCredits : ForgeNavRoute("developer_credits", R.string.tree_dev, PhosphorIconType.USER_CIRCLE)
    data object BugReport : ForgeNavRoute("bug_report", R.string.tree_bug, PhosphorIconType.BUG)

    companion object {
        val bottomNavRoutes: List<ForgeNavRoute> = listOf(
            Home,
            Chat,
            Projects,
            Terminal,
            Settings,
        )
    }
}
