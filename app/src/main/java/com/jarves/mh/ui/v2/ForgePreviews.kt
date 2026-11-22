package com.jarves.mh.ui.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jarves.mh.ui.theme.ThemeMode
import com.jarves.mh.ui.v2.chat.ChatScreen
import com.jarves.mh.ui.v2.components.ForgeAppMode
import com.jarves.mh.ui.v2.home.HomeScreen
import com.jarves.mh.ui.v2.setup.SetupWizardScreen
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Preview(name = "Home - Obsidian Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewHomeObsidian() {
    ForgeTheme(themeMode = ThemeMode.DARK) {
        Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
            HomeScreen(
                userName = "Nayem",
                isUbuntuRunning = true,
                serverUrl = "http://localhost:3000",
                onStartNewTask = {},
                onOpenProject = {},
                onNewProject = {},
                onCloneRepo = {},
                onTemplates = {},
            )
        }
    }
}

@Preview(name = "Home - AMOLED Black", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewHomeAmoled() {
    ForgeTheme(themeMode = ThemeMode.AMOLED) {
        Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
            HomeScreen(
                userName = "Nayem",
                isUbuntuRunning = true,
                serverUrl = "http://localhost:3000",
                onStartNewTask = {},
                onOpenProject = {},
                onNewProject = {},
                onCloneRepo = {},
                onTemplates = {},
            )
        }
    }
}

@Preview(name = "Home - Clean Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewHomeLight() {
    ForgeTheme(themeMode = ThemeMode.LIGHT) {
        Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
            HomeScreen(
                userName = "Nayem",
                isUbuntuRunning = true,
                serverUrl = "http://localhost:3000",
                onStartNewTask = {},
                onOpenProject = {},
                onNewProject = {},
                onCloneRepo = {},
                onTemplates = {},
            )
        }
    }
}

@Preview(name = "Setup Wizard - Step 1", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewSetupWizard() {
    ForgeTheme(themeMode = ThemeMode.DARK) {
        Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
            SetupWizardScreen(
                currentLanguageCode = "en",
                onLanguageSelected = {},
                onSetupComplete = {},
            )
        }
    }
}

@Preview(name = "Chat - Simple Mode", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewChatSimple() {
    ForgeTheme(themeMode = ThemeMode.DARK) {
        Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
            ChatScreen(
                projectName = "PocketForge AI",
                appMode = ForgeAppMode.SIMPLE,
            )
        }
    }
}

@Preview(name = "Chat - Pro Mode (KeyBar & Terminal)", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PreviewChatPro() {
    ForgeTheme(themeMode = ThemeMode.AMOLED) {
        Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
            ChatScreen(
                projectName = "PocketForge AI",
                appMode = ForgeAppMode.PRO,
            )
        }
    }
}
