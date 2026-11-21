package com.jarves.mh.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarves.mh.BuildConfig
import com.jarves.mh.R
import com.jarves.mh.model.*
import com.jarves.mh.network.*
import com.jarves.mh.runtime.*
import com.jarves.mh.ui.components.*
import com.jarves.mh.ui.theme.*
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RootScreenHost(
    state: AppUiState,
    viewModel: MainViewModel,
    projectsListState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() },
) {
    var screen by rememberSaveable { mutableStateOf(RootScreen.PROJECTS) }
    var showQuickTerminal by rememberSaveable { mutableStateOf(false) }
    var openBackup by rememberSaveable { mutableStateOf(false) }

    if (state.showWhatsNew) {
        WhatsNewDialog(
            onDismiss = viewModel::dismissWhatsNew,
            onOpenBackup = {
                viewModel.dismissWhatsNew()
                openBackup = true
                screen = RootScreen.SETTINGS
            },
        )
    }
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()
    val isTerminalRunning by viewModel.isTerminalRunning.collectAsStateWithLifecycle()
    val terminalLiveOutput by viewModel.terminalLiveOutput.collectAsStateWithLifecycle()
    val terminalCurrentCommand by viewModel.terminalCurrentCommand.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!keyboardVisible) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                RootScreen.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = screen == tab,
                        onClick = { screen = tab },
                        icon = {
                            BadgedBox(
                                badge = { if (tab == RootScreen.SETTINGS && !state.backupFeatureSeen) Badge() },
                            ) { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) }
                        },
                        label = { Text(stringResource(tab.labelRes), fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            if (screen == RootScreen.PROJECTS && !keyboardVisible && !showQuickTerminal) {
                ExtendedFloatingActionButton(
                    onClick = { showQuickTerminal = true },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                    text = { Text(stringResource(R.string.projects_terminal), fontWeight = FontWeight.SemiBold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                RootScreen.PROJECTS -> ProjectsScreen(
                    state = state,
                    listState = projectsListState,
                    onOpen = viewModel::openProject,
                    onCreate = viewModel::createProject,
                    onCreateAndroid = viewModel::createAndroidProject,
                    onCreateQuickProject = viewModel::createQuickProject,
                    onImportZip = viewModel::importZipProject,
                    onCloneGit = viewModel::clonePublicGitRepository,
                    onStartGitHubLogin = viewModel::startGitHubLogin,
                    onGenerateNewGitHubCode = viewModel::generateNewGitHubCode,
                    onRefreshGitHub = viewModel::refreshGitHubRepositories,
                    onDisconnectGitHub = viewModel::disconnectGitHub,
                    onCloneGitHub = viewModel::cloneGitHubRepository,
                    onRenameProject = viewModel::renameProject,
                    onDeleteProject = viewModel::deleteProject,
                    onSettings = { screen = RootScreen.SETTINGS },
                    onPing = viewModel::pingApi,
                    onToggleTheme = viewModel::toggleTheme,
                    onInstallUpdate = viewModel::installAppUpdate,
                )
                RootScreen.AGENT -> AgentScreen(
                    state = state,
                    onSaveProvider = { profile, key ->
                        viewModel.updateProvider(profile, key)
                    },
                    onDiscoverModels = viewModel::discoverModels,
                    onValidateProvider = viewModel::validateProvider,
                    onPing = viewModel::pingApi,
                    getSavedApiKey = viewModel::getSavedApiKey,
                    getSavedApiKeys = viewModel::getSavedApiKeys,
                    onAddApiKey = viewModel::addApiKey,
                    onActivateApiKey = viewModel::activateApiKey,
                    onRemoveApiKey = viewModel::removeApiKey,
                    onSelectAgent = viewModel::selectAgent,
                    onInstallAgent = viewModel::installAgent,
                    onCheckAgentUpdates = viewModel::checkAgentUpdates,
                    onUpdateAgent = viewModel::updateAgent,
                    onStartAntigravityLogin = viewModel::startAntigravityLogin,
                    onSubmitAntigravityCode = viewModel::submitAntigravityCode,
                    onLogoutAntigravity = viewModel::logoutAntigravity,
                    onRefreshAntigravityModels = viewModel::refreshAntigravityModels,
                    onSetAntigravityModel = viewModel::setAntigravityModel,
                    onSetAntigravityEffort = viewModel::setAntigravityEffort,
                )
                RootScreen.SETTINGS -> SettingsScreen(
                    state = state,
                    showBackupDot = !state.backupFeatureSeen,
                    onBackupOpened = {
                        openBackup = false
                        viewModel.markBackupFeatureSeen()
                    },
                    openBackup = openBackup,
                    onSaveProvider = { profile, key ->
                        viewModel.updateProvider(profile, key)
                    },
                    onDiscoverModels = viewModel::discoverModels,
                    onValidateProvider = viewModel::validateProvider,
                    onSetThemeMode = viewModel::setThemeMode,
                    onSetLanguage = viewModel::setLanguage,
                    onPing = viewModel::pingApi,
                    onClearTerminal = viewModel::clearTerminal,
                    getSavedApiKey = viewModel::getSavedApiKey,
                    getSavedApiKeys = viewModel::getSavedApiKeys,
                    onAddApiKey = viewModel::addApiKey,
                    onActivateApiKey = viewModel::activateApiKey,
                    onRemoveApiKey = viewModel::removeApiKey,
                    onInstallDevStack = viewModel::installDevStack,
                    onRemoveDevStack = viewModel::removeDevStack,
                    onInstallAgent = viewModel::installAgent,
                    onCheckAgentUpdates = viewModel::checkAgentUpdates,
                    onUpdateAgent = viewModel::updateAgent,
                    onStartAntigravityLogin = viewModel::startAntigravityLogin,
                    onSubmitAntigravityCode = viewModel::submitAntigravityCode,
                    onLogoutAntigravity = viewModel::logoutAntigravity,
                    onRefreshAntigravityModels = viewModel::refreshAntigravityModels,
                    onSetAntigravityModel = viewModel::setAntigravityModel,
                    onSetAntigravityEffort = viewModel::setAntigravityEffort,
                    initialDebugUpdateManifestUrl = viewModel.debugUpdateManifestUrl(),
                    onSetDebugUpdateManifestUrl = viewModel::setDebugUpdateManifestUrl,
                    onClearDebugUpdateManifestUrl = viewModel::clearDebugUpdateManifestUrl,
                    onRefreshDiagnostics = viewModel::refreshDeveloperDiagnostics,
                    onStopInactiveProcesses = viewModel::stopInactiveDeveloperProcesses,
                    onClearRuntimeCache = viewModel::clearDeveloperRuntimeCache,
                )
            }
        }
    }
    if (showQuickTerminal) {
        QuickTerminalSheet(
            onDismiss = { showQuickTerminal = false },
        ) {
            TerminalScreen(
                lines = terminalLines,
                isRunning = isTerminalRunning,
                onRun = viewModel::runTerminalCommand,
                onInput = viewModel::sendTerminalInput,
                onInterrupt = viewModel::interruptTerminalCommand,
                onClear = viewModel::clearTerminal,
                onToggleTheme = viewModel::toggleTheme,
                themeMode = state.themeMode,
                liveOutput = terminalLiveOutput,
                currentCommand = terminalCurrentCommand,
                showThemeAction = false,
                showQuickCommands = true,
                compactHeader = true,
            )
        }
    }
}

