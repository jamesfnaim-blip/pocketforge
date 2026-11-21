package com.jarves.mh.ui

import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarves.mh.model.AgentKind
import com.jarves.mh.model.DevStack

@Composable
fun PocketDevApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val projectsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.consumeToast()
        }
    }
    BackupRestartEffect()
    when {
        state.startupStage == StartupStage.CHECKING -> StartupLoadingScreen(
            state = state,
            themeMode = state.themeMode,
            onToggleTheme = viewModel::toggleTheme,
        )
        !state.initialLanguageSelected && (state.startupStage == StartupStage.SETUP_REQUIRED || !state.onboardingComplete) ->
            InitialLanguageSetupScreen(
                currentLanguageCode = state.languageCode,
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
                onSelectLanguage = viewModel::setLanguage,
                onContinue = viewModel::finishInitialLanguageSetup,
            )
        !state.backgroundSetupComplete && state.startupStage == StartupStage.SETUP_REQUIRED ->
            BackgroundTaskSetupScreen(
                currentLanguageCode = state.languageCode,
                onSetLanguage = viewModel::setLanguage,
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
                onContinue = viewModel::finishBackgroundSetup,
            )
        state.startupStage == StartupStage.SETUP_REQUIRED -> RuntimeSetupPromptScreen(
            selectedStacks = state.selectedDevStacks,
            selectedAgent = state.agentKind,
            themeMode = state.themeMode,
            currentLanguageCode = state.languageCode,
            onSetLanguage = viewModel::setLanguage,
            onToggleTheme = viewModel::toggleTheme,
            onToggleStack = viewModel::toggleDevStack,
            onSelectAgent = viewModel::selectAgent,
            onDownload = viewModel::startRuntimeSetup,
        )
        state.startupStage == StartupStage.INSTALLING && state.showDetailedSetupProgress ->
            RuntimeInstallationScreen(
                state = state,
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
            )
        state.startupStage == StartupStage.INSTALLING || state.startupStage == StartupStage.INITIALIZING ->
            StartupLoadingScreen(
                state = state,
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
            )
        state.startupStage == StartupStage.ERROR -> StartupErrorScreen(
            message = state.startupError,
            isOffline = state.startupErrorIsOffline,
            logs = state.startupLogs,
            themeMode = state.themeMode,
            onToggleTheme = viewModel::toggleTheme,
            onRetry = viewModel::retryStartup,
        )
        state.startupStage == StartupStage.MODEL_SETUP && state.agentKind == AgentKind.ANTIGRAVITY ->
            AntigravityOnboardingScreen(
                state = state,
                currentLanguageCode = state.languageCode,
                onSetLanguage = viewModel::setLanguage,
                onStartLogin = viewModel::startAntigravityLogin,
                onSubmitCode = viewModel::submitAntigravityCode,
                onContinue = viewModel::finishAntigravityOnboarding,
                onSkip = viewModel::skipOnboarding,
                onSelectAgent = viewModel::chooseOnboardingAgent,
                onToggleTheme = viewModel::toggleTheme,
            )
        state.startupStage == StartupStage.MODEL_SETUP -> ProviderSetupScreen(
            initial = state.provider,
            onboarding = true,
            agentKind = state.agentKind,
            initialStep = 1,
            currentLanguageCode = state.languageCode,
            onSetLanguage = viewModel::setLanguage,
            onSave = viewModel::finishOnboarding,
            onSkip = viewModel::skipOnboarding,
            onDiscover = viewModel::discoverModels,
            onValidate = viewModel::validateProvider,
            onSelectAgent = viewModel::chooseOnboardingAgent,
            onToggleTheme = viewModel::toggleTheme,
            themeMode = state.themeMode,
        )
        state.startupStage == StartupStage.READY && !state.backgroundSetupComplete ->
            BackgroundTaskSetupScreen(
                currentLanguageCode = state.languageCode,
                onSetLanguage = viewModel::setLanguage,
                themeMode = state.themeMode,
                onToggleTheme = viewModel::toggleTheme,
                onContinue = viewModel::finishBackgroundSetup,
            )
        state.readOnlyProject != null -> ReadOnlyProjectScreen(
            state = state,
            onBack = viewModel::closeReadOnlyProject,
            onSwitchChat = viewModel::switchReadOnlyChat,
            onContinueHere = viewModel::activateReadOnlyProject,
        )
        state.activeProject != null && state.workspaceVisible -> WorkspaceScreen(
            state = state,
            onBack = viewModel::closeProject,
            onSend = viewModel::sendPrompt,
            onStop = viewModel::stopTask,
            onSteerFollowUp = viewModel::steerQueuedFollowUp,
            onRemoveFollowUp = viewModel::removeQueuedFollowUp,
            onApproval = viewModel::answerApproval,
            onRefreshFiles = viewModel::refreshProjectFiles,
            onLoadProjectDirectory = viewModel::loadProjectDirectory,
            onCreateWorkspaceEntry = viewModel::createWorkspaceEntry,
            onRenameWorkspaceEntry = viewModel::renameWorkspaceEntry,
            onDeleteWorkspaceEntry = viewModel::deleteWorkspaceEntry,
            onUploadToWorkspace = viewModel::uploadToWorkspace,
            onShareWorkspaceFile = viewModel::shareWorkspaceFile,
            onSaveWorkspaceFileCopy = viewModel::saveWorkspaceFileCopy,
            onOpenFile = viewModel::openFile,
            onCloseFile = viewModel::closeFile,
            onBeginFileEdit = viewModel::beginFileEdit,
            onUpdateFileDraft = viewModel::updateFileDraft,
            onCancelFileEdit = viewModel::cancelFileEdit,
            onSaveFile = viewModel::saveOpenedFile,
            onReloadFile = viewModel::reloadOpenedFile,
            onDismissFileConflict = viewModel::dismissFileConflict,
            onUndoChanges = viewModel::undoLastChanges,
            onKeepChanges = viewModel::keepLastChanges,
            onUndoFileChange = viewModel::undoFileChange,
            onKeepFileChange = viewModel::keepFileChange,
            onCreateChat = viewModel::createChat,
            onSwitchChat = viewModel::switchChat,
            onTerminalRun = viewModel::requestProjectTerminalCommand,
            onTerminalInput = viewModel::sendProjectTerminalInput,
            onTerminalInterrupt = viewModel::interruptProjectTerminalCommand,
            onTerminalPrepare = viewModel::prepareProjectTerminalCommand,
            onTerminalDraftConsumed = viewModel::consumeProjectTerminalDraft,
            onTerminalOpened = viewModel::openProjectTerminal,
            onTerminalStop = viewModel::stopProjectTerminalCommand,
            onTerminalClear = viewModel::clearProjectTerminal,
            onTerminalConfirm = viewModel::confirmProjectTerminalCommand,
            onTerminalCancel = viewModel::cancelProjectTerminalCommand,
            onUseSuggestedProjectRoot = viewModel::useSuggestedProjectRoot,
            onExportProject = viewModel::exportActiveProject,
            onAddAttachments = viewModel::addChatAttachments,
            onRemoveAttachment = viewModel::removePendingAttachment,
            onOpenAttachment = viewModel::openChatAttachment,
            onBuildAndRunAndroid = viewModel::buildAndRunAndroidApp,
            onAndroidAction = viewModel::performAndroidAction,
            onRefreshAndroidDashboard = viewModel::refreshAndroidDashboard,
            onShareAndroidApk = viewModel::shareAndroidApk,
            onSaveAndroidApk = viewModel::saveAndroidApk,
            onOpenAndroidAppDetails = viewModel::openInstalledAndroidAppDetails,
            onUninstallAndroidApp = viewModel::uninstallAndroidApp,
            onOpenAndroidIssue = viewModel::openAndroidIssue,
            onAndroidHealthFix = viewModel::performAndroidHealthFix,
            onRefreshAndroidHealth = viewModel::refreshAndroidHealth,
            onStartAndroidLogcat = viewModel::startAndroidLogcat,
            onStopAndroidLogcat = viewModel::stopAndroidLogcat,
            onToggleAndroidLogcatPause = viewModel::toggleAndroidLogcatPause,
            onClearAndroidLogcat = viewModel::clearAndroidLogcat,
            onAndroidLogcatQuery = viewModel::setAndroidLogcatQuery,
            onAndroidLogcatLevel = viewModel::setAndroidLogcatLevel,
            onOpenAndroidLogLine = viewModel::openAndroidLogLine,
            onCancelAndroidBuild = viewModel::cancelAndroidBuild,
            onCleanAndroid = viewModel::cleanAndroidProject,
            onInstallAndroidTools = { viewModel.installDevStack(DevStack.ANDROID) },
        )
        else -> RootScreenHost(state, viewModel, projectsListState)
    }
}

