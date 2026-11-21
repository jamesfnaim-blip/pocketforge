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
import androidx.compose.foundation.interaction.collectIsDraggedAsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun WorkspaceScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onSteerFollowUp: (String) -> Unit,
    onRemoveFollowUp: (String) -> Unit,
    onApproval: (Boolean) -> Unit,
    onRefreshFiles: () -> Unit,
    onLoadProjectDirectory: (String) -> Unit,
    onCreateWorkspaceEntry: (String, String, Boolean) -> Unit,
    onRenameWorkspaceEntry: (String, String) -> Unit,
    onDeleteWorkspaceEntry: (String) -> Unit,
    onUploadToWorkspace: (String, List<Uri>) -> Unit,
    onShareWorkspaceFile: (String) -> Unit,
    onSaveWorkspaceFileCopy: (String, Uri) -> Unit,
    onOpenFile: (WorkspaceEntry) -> Unit,
    onCloseFile: () -> Unit,
    onBeginFileEdit: () -> Unit,
    onUpdateFileDraft: (String) -> Unit,
    onCancelFileEdit: () -> Unit,
    onSaveFile: () -> Unit,
    onReloadFile: () -> Unit,
    onDismissFileConflict: () -> Unit,
    onUndoChanges: () -> Unit,
    onKeepChanges: () -> Unit,
    onUndoFileChange: (String) -> Unit,
    onKeepFileChange: (String) -> Unit,
    onCreateChat: () -> Unit,
    onSwitchChat: (String) -> Unit,
    onTerminalRun: (String) -> Unit,
    onTerminalInput: (String) -> Unit,
    onTerminalInterrupt: () -> Unit,
    onTerminalPrepare: (String) -> Unit,
    onTerminalDraftConsumed: () -> Unit,
    onTerminalOpened: () -> Unit,
    onTerminalStop: () -> Unit,
    onTerminalClear: () -> Unit,
    onTerminalConfirm: () -> Unit,
    onTerminalCancel: () -> Unit,
    onUseSuggestedProjectRoot: () -> Unit,
    onExportProject: (Uri, ExportSelection) -> Unit,
    onAddAttachments: (List<Uri>) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onOpenAttachment: (ChatAttachment) -> Unit,
    onBuildAndRunAndroid: () -> Unit,
    onAndroidAction: (AndroidAction) -> Unit,
    onRefreshAndroidDashboard: () -> Unit,
    onShareAndroidApk: () -> Unit,
    onSaveAndroidApk: (Uri) -> Unit,
    onOpenAndroidAppDetails: () -> Unit,
    onUninstallAndroidApp: () -> Unit,
    onOpenAndroidIssue: (AndroidBuildIssue) -> Unit,
    onAndroidHealthFix: (AndroidHealthFix) -> Unit,
    onRefreshAndroidHealth: () -> Unit,
    onStartAndroidLogcat: () -> Unit,
    onStopAndroidLogcat: () -> Unit,
    onToggleAndroidLogcatPause: () -> Unit,
    onClearAndroidLogcat: () -> Unit,
    onAndroidLogcatQuery: (String) -> Unit,
    onAndroidLogcatLevel: (AndroidLogLevel) -> Unit,
    onOpenAndroidLogLine: (AndroidLogLine) -> Unit,
    onCancelAndroidBuild: () -> Unit,
    onCleanAndroid: () -> Unit,
    onInstallAndroidTools: () -> Unit,
) {
    // Leaving while a terminal command runs asks whether to stop it or keep it running.
    var confirmLeaveTerminal by rememberSaveable { mutableStateOf(false) }
    val requestBack: () -> Unit = {
        if (state.projectTerminalRunning) confirmLeaveTerminal = true else onBack()
    }
    BackHandler(onBack = requestBack)
    if (confirmLeaveTerminal) {
        AlertDialog(
            onDismissRequest = { confirmLeaveTerminal = false },
            title = { Text(stringResource(R.string.ws_terminal_leave_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.ws_terminal_leave_body,
                        state.projectTerminalCommand ?: stringResource(R.string.workspace_project_terminal),
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmLeaveTerminal = false
                    onTerminalStop()
                    onBack()
                }) { Text(stringResource(R.string.ws_terminal_leave_stop)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { confirmLeaveTerminal = false }) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = {
                        confirmLeaveTerminal = false
                        onBack()
                    }) { Text(stringResource(R.string.ws_terminal_leave_keep)) }
                }
            },
        )
    }
    val context = LocalContext.current
    val isAndroidProject = state.androidProjectDetected
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var pendingAndroidAction by remember { mutableStateOf<AndroidAction?>(null) }
    var exportSelection by remember(state.activeProject?.id) { mutableStateOf(ExportSelection()) }
    var exportSelectionMode by remember(state.activeProject?.id) { mutableStateOf(false) }
    val exportProjectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri -> if (uri != null) onExportProject(uri, exportSelection) },
    )
    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = onAddAttachments,
    )
    var pendingUploadParent by rememberSaveable { mutableStateOf<String?>(null) }
    val uploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            pendingUploadParent?.let { parent -> onUploadToWorkspace(parent, uris) }
            pendingUploadParent = null
        },
    )
    var pendingCopyPath by rememberSaveable { mutableStateOf<String?>(null) }
    val saveCopyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri ->
            val path = pendingCopyPath
            if (uri != null && path != null) onSaveWorkspaceFileCopy(path, uri)
            pendingCopyPath = null
        },
    )
    var pendingPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { saved ->
            val uri = pendingPhotoUri?.let(Uri::parse)
            pendingPhotoUri = null
            if (saved && uri != null) onAddAttachments(listOf(uri))
        },
    )
    val unknownAppsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()) {
                pendingAndroidAction?.let(onAndroidAction)
            } else {
                Toast.makeText(context, context.getString(R.string.ws_allow_app_installs), Toast.LENGTH_LONG).show()
            }
            pendingAndroidAction = null
        },
    )
    val saveApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive"),
        onResult = { uri -> if (uri != null) onSaveAndroidApk(uri) },
    )
    val chatListState = rememberLazyListState()
    var userScrolledUp by rememberSaveable { mutableStateOf(false) }

    val chatItemCount = state.messages.size +
        (if (state.liveProcess.isNotEmpty() || state.liveThinking) 1 else 0) +
        (if (state.pendingApproval != null) 1 else 0)

    LaunchedEffect(state.activeChatId) {
        userScrolledUp = false
        if (chatItemCount > 0) chatListState.scrollToItem(chatItemCount - 1)
    }

    // Only a finger drag pauses follow mode; our own scrolls (and the Latest button) must not.
    val chatDragged by chatListState.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(chatDragged) {
        if (chatDragged) userScrolledUp = true
    }
    // Reaching the bottom again, by hand or via Latest, re-enables follow mode.
    LaunchedEffect(chatListState.isScrollInProgress) {
        if (!chatListState.isScrollInProgress && !chatListState.canScrollForward) {
            userScrolledUp = false
        }
    }
    // A new run always starts following.
    LaunchedEffect(state.isRunning) {
        if (state.isRunning) userScrolledUp = false
    }

    // Follow new tokens/updates unless the user has scrolled up to read.
    LaunchedEffect(
        state.messages.size,
        state.messages.lastOrNull()?.text?.length,
        state.liveProcess.size,
        state.liveProcess.lastOrNull()?.detail,
        state.pendingApproval,
        state.isRunning,
    ) {
        if (chatItemCount <= 0 || userScrolledUp || chatDragged) return@LaunchedEffect
        // Don't gate on canScrollForward: this runs before the new item is laid out, so the list
        // still looks "at bottom". Scrolling past the end is clamped, so this is a no-op when already there.
        chatListState.scrollToItem(chatItemCount - 1)
        // The newest item can be taller than the screen; land on its end, not its top.
        chatListState.scrollBy(100_000f)
    }

    var selectedTab by rememberSaveable { mutableStateOf(WorkspaceTab.CHAT) }
    var showChats by rememberSaveable { mutableStateOf(false) }
    var showAndroidBuild by rememberSaveable { mutableStateOf(false) }
    val activeChat = state.projectChats.firstOrNull { it.id == state.activeChatId }
    val requestAndroidAction: (AndroidAction) -> Unit = { action ->
        val mayInstall = action in setOf(AndroidAction.INSTALL, AndroidAction.RUN, AndroidAction.BUILD_AND_RUN)
        if (mayInstall && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            pendingAndroidAction = action
            unknownAppsLauncher.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
        } else {
            onAndroidAction(action)
        }
    }

    LaunchedEffect(state.projectExportSucceededAtMillis) {
        if (state.projectExportSucceededAtMillis != null) {
            exportSelectionMode = false
            exportSelection = ExportSelection()
        }
    }

    if (showAndroidBuild) {
        AndroidDashboardScreen(
            state = state,
            onClose = { onStopAndroidLogcat(); showAndroidBuild = false },
            onAction = requestAndroidAction,
            onCancelBuild = onCancelAndroidBuild,
            onRefresh = onRefreshAndroidDashboard,
            onInstallTools = onInstallAndroidTools,
            onShareApk = onShareAndroidApk,
            onSaveApk = { state.androidApkInfo?.let { saveApkLauncher.launch(it.fileName) } },
            onOpenAppDetails = onOpenAndroidAppDetails,
            onUninstallApp = onUninstallAndroidApp,
            onOpenIssue = { issue -> showAndroidBuild = false; onOpenAndroidIssue(issue) },
            onHealthFix = onAndroidHealthFix,
            onRefreshHealth = onRefreshAndroidHealth,
            onStartLogcat = onStartAndroidLogcat,
            onStopLogcat = onStopAndroidLogcat,
            onToggleLogcatPause = onToggleAndroidLogcatPause,
            onClearLogcat = onClearAndroidLogcat,
            onLogcatQuery = onAndroidLogcatQuery,
            onLogcatLevel = onAndroidLogcatLevel,
            onOpenLogLine = { line -> showAndroidBuild = false; onOpenAndroidLogLine(line) },
        )
        return
    }

    // If a file is open, show the FileViewerScreen on top
    if (state.openedFile != null) {
        FileViewerScreen(
            file = state.openedFile,
            editingAllowed = !state.isRunning && !state.projectTerminalRunning && !state.androidBuildRunning,
            onClose = {
                onCloseFile()
                selectedTab = WorkspaceTab.FILES
            },
            onBeginEdit = onBeginFileEdit,
            onDraftChange = onUpdateFileDraft,
            onCancelEdit = onCancelFileEdit,
            onSave = onSaveFile,
            onReload = onReloadFile,
            onDismissConflict = onDismissFileConflict,
        )
        return
    }

    if (showChats) {
        ChatSwitcherDialog(
            chats = state.projectChats,
            activeChatId = state.activeChatId,
            switchingEnabled = !state.isRunning,
            onDismiss = { showChats = false },
            onCreate = {
                onCreateChat()
                showChats = false
                selectedTab = WorkspaceTab.CHAT
            },
            onSwitch = { chatId ->
                onSwitchChat(chatId)
                showChats = false
                selectedTab = WorkspaceTab.CHAT
            },
        )
    }
    state.pendingTerminalCommand?.let { command ->
        AlertDialog(
            onDismissRequest = onTerminalCancel,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.ws_destructive_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.ws_destructive_desc))
                    Surface(color = Color(0xFF14171E), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            command,
                            Modifier.fillMaxWidth().padding(10.dp),
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                        )
                    }
                }
            },
            confirmButton = { Button(onClick = onTerminalConfirm) { Text(stringResource(R.string.ws_run_anyway)) } },
            dismissButton = { TextButton(onClick = onTerminalCancel) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            state.activeProject?.name.orEmpty(),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    Toast.makeText(context, state.activeProject?.name.orEmpty(), Toast.LENGTH_LONG).show()
                                },
                            ),
                        )
                        // Agent first so it always stays visible; a long chat title is cut with "…".
                        Text(
                            "${if (state.agentKind == AgentKind.ANTIGRAVITY) state.agentKind.localizedTitle() else state.provider.kind.localizedTitle()} · ${activeChat?.localizedTitle() ?: stringResource(R.string.tab_chat)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = requestBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.tab_projects)) } },
                actions = {
                    if (isAndroidProject) {
                        IconButton(
                            onClick = { showAndroidBuild = true },
                            enabled = !state.isRunning && !state.projectTerminalRunning,
                        ) {
                            if (state.androidBuildRunning) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.PlayArrow, stringResource(R.string.ws_build_run_android))
                        }
                    }
                    IconButton(onClick = { showChats = true }) { Icon(Icons.Default.History, stringResource(R.string.ws_project_chats)) }
                    if (state.isRunning) CircularProgressIndicator(Modifier.padding(12.dp).size(20.dp), strokeWidth = 2.dp)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            if (!keyboardVisible) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                WorkspaceTab.entries.filter { it != WorkspaceTab.CHANGES }.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            if (tab == WorkspaceTab.FILES) onRefreshFiles()
                            if (tab == WorkspaceTab.TERMINAL) onTerminalOpened()
                        },
                        icon = { Icon(tab.icon, stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes), fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                WorkspaceTab.CHAT -> ChatTab(
                    state.messages,
                    state.pendingApproval,
                    state.liveProcess,
                    state.isRunning,
                    onSend,
                    onStop,
                    onApproval,
                    listState = chatListState,
                    taskStartedAtMillis = state.workSegmentStartedAtMillis ?: state.taskStartedAtMillis,
                    taskFinishedAtMillis = state.taskFinishedAtMillis,
                    thinkingActive = state.liveThinking,
                    agentKind = state.agentKind,
                    pendingAttachments = state.pendingAttachments,
                    onAttach = {
                        attachmentLauncher.launch(arrayOf("image/*", "text/*", "application/json", "application/xml"))
                    },
                    onTakePhoto = {
                        runCatching {
                            val uri = createCameraPhotoUri(context)
                            pendingPhotoUri = uri.toString()
                            takePhotoLauncher.launch(uri)
                        }.onFailure {
                            pendingPhotoUri = null
                            Toast.makeText(context, context.getString(R.string.chat_camera_unavailable), Toast.LENGTH_LONG).show()
                        }
                    },
                    onRemoveAttachment = onRemoveAttachment,
                    onOpenAttachment = onOpenAttachment,
                    onRunInTerminal = { command ->
                        selectedTab = WorkspaceTab.TERMINAL
                        onTerminalOpened()
                        onTerminalPrepare(command)
                    },
                    queuedFollowUps = state.queuedFollowUps,
                    onSteerFollowUp = onSteerFollowUp,
                    onRemoveFollowUp = onRemoveFollowUp,
                )
                WorkspaceTab.FILES -> FilesTab(
                    files = state.workspaceFiles,
                    loading = state.filesLoading,
                    suggestedProjectRoot = state.suggestedProjectRoot,
                    onRefresh = onRefreshFiles,
                    onLoadDirectory = onLoadProjectDirectory,
                    onOpenFile = onOpenFile,
                    onUseSuggestedProjectRoot = onUseSuggestedProjectRoot,
                    selectionMode = exportSelectionMode,
                    exportSelection = exportSelection,
                    exportRunning = state.projectExportRunning,
                    exportEnabled = !state.isRunning && !state.projectTerminalRunning && !state.androidBuildRunning,
                    onEnterSelection = {
                        exportSelection = ExportSelection()
                        exportSelectionMode = true
                    },
                    onCancelSelection = {
                        exportSelectionMode = false
                        exportSelection = ExportSelection()
                    },
                    onToggleSelection = { entry -> exportSelection = exportSelection.toggled(entry.path, entry.isDirectory) },
                    onSelectAll = { exportSelection = ExportSelection() },
                    onDownloadSelection = {
                        exportProjectLauncher.launch("${state.activeProject?.slug ?: "project"}.zip")
                    },
                    fileOpsEnabled = !state.isRunning && !state.projectTerminalRunning &&
                        !state.androidBuildRunning && !state.projectExportRunning,
                    onCreateEntry = onCreateWorkspaceEntry,
                    onRenameEntry = onRenameWorkspaceEntry,
                    onDeleteEntry = onDeleteWorkspaceEntry,
                    onUploadInto = { parent ->
                        pendingUploadParent = parent
                        uploadLauncher.launch(arrayOf("*/*"))
                    },
                    onShareFile = onShareWorkspaceFile,
                    onSaveFileCopy = { path ->
                        pendingCopyPath = path
                        saveCopyLauncher.launch(path.substringAfterLast('/'))
                    },
                )
                WorkspaceTab.TERMINAL -> TerminalScreen(
                    lines = state.projectTerminalLines,
                    isRunning = state.projectTerminalRunning,
                    onRun = onTerminalRun,
                    onInput = onTerminalInput,
                    onInterrupt = onTerminalInterrupt,
                    onClear = onTerminalClear,
                    onToggleTheme = {},
                    themeMode = state.themeMode,
                    title = stringResource(R.string.workspace_project_terminal),
                    subtitle = "${state.projectTerminalCwd} · Ubuntu PRoot",
                    liveOutput = state.projectTerminalLiveOutput,
                    currentCommand = state.projectTerminalCommand,
                    commandDraft = state.projectTerminalDraft,
                    onCommandDraftConsumed = onTerminalDraftConsumed,
                    promptPath = state.projectTerminalCwd,
                    onStop = onTerminalStop,
                    showThemeAction = false,
                    showQuickCommands = false,
                    compactHeader = true,
                )
                WorkspaceTab.CHANGES -> ChangesTab(
                    state.changes,
                    onUndoChanges,
                    onKeepChanges,
                    onUndoFileChange,
                    onKeepFileChange,
                )
                WorkspaceTab.PREVIEW -> PreviewTab(state.previewReady, state.previewUrl)
            }
        }
    }
}

