package com.jarves.mh.ui.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.jarves.mh.ui.theme.AppThemeMode
import com.jarves.mh.ui.v2.chat.ChatScreen
import com.jarves.mh.ui.v2.components.ForgeAppMode
import com.jarves.mh.ui.v2.home.HomeScreen
import com.jarves.mh.ui.v2.setup.SetupWizardScreen
import com.jarves.mh.ui.v2.theme.ForgeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xhdpi")
class ForgeRealScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -------------------------------------------------------------------------
    // HOME SCREEN REAL RENDERS (Obsidian, AMOLED, Light - EN & BN)
    // -------------------------------------------------------------------------

    @Test
    fun renderHomeObsidianEn() {
        composeTestRule.setContent {
            ForgeTheme(themeMode = AppThemeMode.DARK) {
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
                        onOpenClassicUi = {},
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("docs/ui-review/real_home_obsidian_en.png")
    }

    @Test
    fun renderHomeAmoledEn() {
        composeTestRule.setContent {
            ForgeTheme(themeMode = AppThemeMode.AMOLED) {
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
                        onOpenClassicUi = {},
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("docs/ui-review/real_home_amoled_en.png")
    }

    @Test
    fun renderHomeLightEn() {
        composeTestRule.setContent {
            ForgeTheme(themeMode = AppThemeMode.LIGHT) {
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
                        onOpenClassicUi = {},
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("docs/ui-review/real_home_light_en.png")
    }

    @Test
    @Config(qualifiers = "w390dp-h844dp-xhdpi-b+bn")
    fun renderHomeObsidianBn() {
        composeTestRule.setContent {
            ForgeTheme(themeMode = AppThemeMode.DARK) {
                Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
                    HomeScreen(
                        userName = "নাঈম",
                        isUbuntuRunning = true,
                        serverUrl = "http://localhost:3000",
                        onStartNewTask = {},
                        onOpenProject = {},
                        onNewProject = {},
                        onCloneRepo = {},
                        onTemplates = {},
                        onOpenClassicUi = {},
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("docs/ui-review/real_home_obsidian_bn.png")
    }

    // -------------------------------------------------------------------------
    // SETUP WIZARD REAL RENDERS
    // -------------------------------------------------------------------------

    @Test
    fun renderSetupObsidianEn() {
        composeTestRule.setContent {
            ForgeTheme(themeMode = AppThemeMode.DARK) {
                Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
                    SetupWizardScreen(
                        currentLanguageCode = "en",
                        onLanguageSelected = {},
                        onSetupComplete = {},
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("docs/ui-review/real_setup_obsidian_en.png")
    }

    @Test
    @Config(qualifiers = "w390dp-h844dp-xhdpi-b+bn")
    fun renderSetupObsidianBn() {
        composeTestRule.setContent {
            ForgeTheme(themeMode = AppThemeMode.DARK) {
                Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
                    SetupWizardScreen(
                        currentLanguageCode = "bn",
                        onLanguageSelected = {},
                        onSetupComplete = {},
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("docs/ui-review/real_setup_obsidian_bn.png")
    }

    // -------------------------------------------------------------------------
    // CHAT SCREEN REAL RENDERS (Simple Mode & Pro Mode)
    // -------------------------------------------------------------------------

    @Test
    fun renderChatSimpleObsidianEn() {
        composeTestRule.setContent {
            ForgeTheme(themeMode = AppThemeMode.DARK) {
                Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
                    ChatScreen(
                        projectName = "PocketForge AI",
                        activeModel = "Claude 3.5 Sonnet",
                        appMode = ForgeAppMode.SIMPLE,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("docs/ui-review/real_chat_simple_obsidian_en.png")
    }

    @Test
    fun renderChatProAmoledEn() {
        composeTestRule.setContent {
            ForgeTheme(themeMode = AppThemeMode.AMOLED) {
                Box(modifier = Modifier.fillMaxSize().background(ForgeTheme.colors.background)) {
                    ChatScreen(
                        projectName = "PocketForge AI",
                        activeModel = "Claude 3.5 Sonnet",
                        appMode = ForgeAppMode.PRO,
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage("docs/ui-review/real_chat_pro_amoled_en.png")
    }
}
