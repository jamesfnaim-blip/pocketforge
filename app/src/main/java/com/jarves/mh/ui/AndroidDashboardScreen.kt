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

@Composable
internal fun androidStageLabel(stage: AndroidBuildStage): String = stringResource(
    when (stage) {
        AndroidBuildStage.IDLE -> R.string.android_stage_idle
        AndroidBuildStage.PREPARING -> R.string.android_stage_preparing
        AndroidBuildStage.RESOLVING -> R.string.android_stage_resolving
        AndroidBuildStage.COMPILING -> R.string.android_stage_compiling
        AndroidBuildStage.RESOURCES -> R.string.android_stage_resources
        AndroidBuildStage.PACKAGING -> R.string.android_stage_packaging
        AndroidBuildStage.FINDING_APK -> R.string.android_stage_finding_apk
        AndroidBuildStage.INSTALLING -> R.string.android_stage_installing
        AndroidBuildStage.LAUNCHING -> R.string.android_stage_launching
        AndroidBuildStage.COMPLETE -> R.string.android_stage_complete
        AndroidBuildStage.FAILED -> R.string.android_stage_failed
        AndroidBuildStage.CANCELLING -> R.string.android_stage_cancelling
        AndroidBuildStage.CANCELLED -> R.string.android_stage_cancelled
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AndroidDashboardScreen(
    state: AppUiState,
    onClose: () -> Unit,
    onAction: (AndroidAction) -> Unit,
    onCancelBuild: () -> Unit,
    onRefresh: () -> Unit,
    onInstallTools: () -> Unit,
    onShareApk: () -> Unit,
    onSaveApk: () -> Unit,
    onOpenAppDetails: () -> Unit,
    onUninstallApp: () -> Unit,
    onOpenIssue: (AndroidBuildIssue) -> Unit,
    onHealthFix: (AndroidHealthFix) -> Unit,
    onRefreshHealth: () -> Unit,
    onStartLogcat: () -> Unit,
    onStopLogcat: () -> Unit,
    onToggleLogcatPause: () -> Unit,
    onClearLogcat: () -> Unit,
    onLogcatQuery: (String) -> Unit,
    onLogcatLevel: (AndroidLogLevel) -> Unit,
    onOpenLogLine: (AndroidLogLine) -> Unit,
) {
    BackHandler(onBack = onClose)
    val clipboard = LocalClipboardManager.current
    var section by rememberSaveable { mutableStateOf(AndroidDashboardSection.OVERVIEW) }
    var showBuildMenu by remember { mutableStateOf(false) }
    var showRawLog by rememberSaveable { mutableStateOf(false) }
    val liveElapsedSeconds = state.androidBuildStartedAtMillis?.let { rememberLiveElapsedSeconds(it) }
    LaunchedEffect(Unit) { onRefresh() }
    LaunchedEffect(section) { onStopLogcat() }
    DisposableEffect(Unit) { onDispose(onStopLogcat) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.android_screen_title), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.close)) }
                },
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, stringResource(R.string.refresh)) }
                    Box {
                        IconButton(onClick = { showBuildMenu = true }) { Icon(Icons.Default.MoreVert, stringResource(R.string.more_options)) }
                        DropdownMenu(expanded = showBuildMenu, onDismissRequest = { showBuildMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.force_rebuild)) },
                                leadingIcon = { Icon(Icons.Default.Build, null) },
                                enabled = !state.androidBuildRunning,
                                onClick = { showBuildMenu = false; onAction(AndroidAction.FORCE_BUILD) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.clean_project)) },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                enabled = !state.androidBuildRunning,
                                onClick = { showBuildMenu = false; onAction(AndroidAction.CLEAN) },
                            )
                            if (state.androidApkInfo != null) {
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.install)) },
                                    leadingIcon = { Icon(Icons.Default.InstallMobile, null) },
                                    enabled = !state.androidBuildRunning,
                                    onClick = { showBuildMenu = false; onAction(AndroidAction.INSTALL) },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.share)) },
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                    onClick = { showBuildMenu = false; onShareApk() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.save_copy)) },
                                    leadingIcon = { Icon(Icons.Default.Download, null) },
                                    onClick = { showBuildMenu = false; onSaveApk() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.copy_path)) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick = {
                                        state.androidApkInfo?.path?.let { clipboard.setText(AnnotatedString(it)) }
                                        showBuildMenu = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.app_info)) },
                                    leadingIcon = { Icon(Icons.Default.Info, null) },
                                    enabled = state.androidApkInfo?.installed == true,
                                    onClick = { showBuildMenu = false; onOpenAppDetails() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.uninstall)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null) },
                                    enabled = state.androidApkInfo?.installed == true,
                                    onClick = { showBuildMenu = false; onUninstallApp() },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = if (section == AndroidDashboardSection.HEALTH) 1 else 0) {
                Tab(
                    selected = section != AndroidDashboardSection.HEALTH,
                    onClick = { section = AndroidDashboardSection.OVERVIEW },
                    text = { Text(stringResource(R.string.android_build_tab)) },
                )
                Tab(
                    selected = section == AndroidDashboardSection.HEALTH,
                    onClick = { section = AndroidDashboardSection.HEALTH },
                    text = { Text(stringResource(R.string.android_health_tab)) },
                )
            }

            when (section) {
                AndroidDashboardSection.OVERVIEW -> ProfessionalAndroidOverview(
                    state = state,
                    liveElapsedSeconds = liveElapsedSeconds,
                    showRawLog = showRawLog,
                    onToggleRawLog = { showRawLog = !showRawLog },
                    onAction = onAction,
                    onCancelBuild = onCancelBuild,
                    onInstallTools = onInstallTools,
                    onOpenIssue = onOpenIssue,
                )
                AndroidDashboardSection.BUILD -> {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))) {
                                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Android, null, tint = PocketGreen)
                                        Spacer(Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(stringResource(R.string.build_progress), fontWeight = FontWeight.SemiBold)
                                            Text(androidStageLabel(state.androidBuildStage), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            liveElapsedSeconds?.let { live ->
                                                val seconds = if (state.androidBuildRunning) live.toLong() else {
                                                    ((state.androidBuildFinishedAtMillis ?: System.currentTimeMillis()) -
                                                        (state.androidBuildStartedAtMillis ?: System.currentTimeMillis())) / 1_000L
                                                }.coerceAtLeast(0L)
                                                Text(formatDuration(seconds), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        if (state.androidBuildRunning) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                                    }
                                    if (state.androidBuildRunning) LinearProgressIndicator(Modifier.fillMaxWidth())
                                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(
                                            AndroidBuildStage.PREPARING, AndroidBuildStage.RESOLVING, AndroidBuildStage.COMPILING,
                                            AndroidBuildStage.RESOURCES, AndroidBuildStage.PACKAGING, AndroidBuildStage.INSTALLING,
                                            AndroidBuildStage.LAUNCHING, AndroidBuildStage.COMPLETE,
                                        ).forEach { stage ->
                                            val active = stage == state.androidBuildStage
                                            Surface(
                                                color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(50),
                                            ) { Text(androidStageLabel(stage), Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 10.sp) }
                                        }
                                    }
                                }
                            }
                        }
                        if (DevStack.ANDROID !in state.installedDevStacks) {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(stringResource(R.string.android_tools_required), color = MaterialTheme.colorScheme.onErrorContainer)
                                        Button(onClick = onInstallTools) { Text(stringResource(R.string.install_android_tools)) }
                                    }
                                }
                            }
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onAction(AndroidAction.BUILD) },
                                        enabled = !state.androidBuildRunning && DevStack.ANDROID in state.installedDevStacks,
                                        modifier = Modifier.weight(1f),
                                    ) { Icon(Icons.Default.Build, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.build)) }
                                    OutlinedButton(
                                        onClick = { onAction(AndroidAction.INSTALL) }, enabled = !state.androidBuildRunning && state.androidApkInfo != null,
                                        modifier = Modifier.weight(1f),
                                    ) { Icon(Icons.Default.InstallMobile, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.install)) }
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onAction(AndroidAction.RUN) }, enabled = !state.androidBuildRunning && state.androidApkInfo != null,
                                        modifier = Modifier.weight(1f),
                                    ) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.run)) }
                                    Button(
                                        onClick = { onAction(AndroidAction.BUILD_AND_RUN) },
                                        enabled = !state.androidBuildRunning && DevStack.ANDROID in state.installedDevStacks,
                                        modifier = Modifier.weight(1f),
                                    ) { Icon(Icons.Default.Launch, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.build_and_run)) }
                                }
                                if (state.androidBuildRunning) {
                                    OutlinedButton(onClick = onCancelBuild, modifier = Modifier.fillMaxWidth()) {
                                        Icon(Icons.Default.Stop, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.cancel_operation))
                                    }
                                }
                            }
                        }
                        if (state.androidBuildIssues.isNotEmpty()) {
                            item { Text(stringResource(R.string.build_issues), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                            items(state.androidBuildIssues) { rawIssue ->
                                val issue = rawIssue.localized(LocalContext.current)
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable(enabled = issue.filePath != null) { onOpenIssue(issue) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                ) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(issue.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                        Text(issue.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                        issue.suggestion?.let { Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)) }
                                        if (issue.filePath != null) Text("${issue.filePath}${issue.line?.let { ":$it" }.orEmpty()}", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        state.androidApkInfo?.let { apk ->
                            item {
                                Card {
                                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Android, null, tint = PocketGreen)
                                            Spacer(Modifier.width(8.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(apk.fileName, fontWeight = FontWeight.Bold)
                                                Text("${formatFileSize(apk.sizeBytes)} · ${apk.packageName ?: stringResource(R.string.unknown_package)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text(if (apk.installedMatches) stringResource(R.string.installed) else stringResource(R.string.not_installed), fontSize = 10.sp)
                                        }
                                        if (apk.stale) {
                                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                                                Text(stringResource(R.string.stale_apk_warning), Modifier.fillMaxWidth().padding(9.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                            }
                                        }
                                        Text(
                                            stringResource(R.string.apk_version_info, apk.versionName ?: "—", apk.versionCode?.toString() ?: "—", apk.minSdk?.toString() ?: "—", apk.targetSdk?.toString() ?: "—"),
                                            fontSize = 11.sp,
                                        )
                                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            TextButton(onClick = onShareApk) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.share)) }
                                            TextButton(onClick = onSaveApk) { Icon(Icons.Default.Download, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.save_copy)) }
                                            TextButton(onClick = { clipboard.setText(AnnotatedString(apk.path)) }) { Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.copy_path)) }
                                            TextButton(onClick = onOpenAppDetails, enabled = apk.installed) { Icon(Icons.Default.Info, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.app_info)) }
                                            TextButton(onClick = onUninstallApp, enabled = apk.installed) { Icon(Icons.Default.Delete, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.uninstall)) }
                                        }
                                    }
                                }
                            }
                        }
                        if (state.androidBuildLog.isNotBlank()) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { showRawLog = !showRawLog }, modifier = Modifier.weight(1f)) {
                                        Text(stringResource(if (showRawLog) R.string.hide_build_details else R.string.show_build_details))
                                    }
                                    TextButton(onClick = { clipboard.setText(AnnotatedString(state.androidBuildLog)) }) {
                                        Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.copy_log))
                                    }
                                }
                                if (showRawLog) Surface(color = Color(0xFF11141A), shape = RoundedCornerShape(12.dp)) {
                                    SelectionContainer {
                                        Text(
                                            state.androidBuildLog,
                                            Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()).padding(12.dp),
                                            color = Color(0xFFD7DEE9), fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                AndroidDashboardSection.LOGCAT -> {
                    val logcat = state.androidLogcat
                    val filtered = remember(logcat.lines, logcat.minimumLevel, logcat.query) {
                        logcat.lines.filter { line ->
                            line.level.ordinal >= logcat.minimumLevel.ordinal &&
                                (logcat.query.isBlank() || line.text.contains(logcat.query, ignoreCase = true))
                        }
                    }
                    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!logcat.available) {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(stringResource(R.string.logcat_access_required), fontWeight = FontWeight.Bold)
                                    Text(logcat.message ?: stringResource(R.string.logcat_access_description), fontSize = 12.sp)
                                    val command = MainViewModel.LOGCAT_GRANT_COMMAND
                                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                                        Text(command, Modifier.fillMaxWidth().clickable { clipboard.setText(AnnotatedString(command)) }.padding(10.dp), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                    }
                                    Button(onClick = { clipboard.setText(AnnotatedString(command)) }) { Text(stringResource(R.string.copy_command)) }
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = logcat.query, onValueChange = onLogcatQuery,
                                    modifier = Modifier.weight(1f), singleLine = true,
                                    label = { Text(stringResource(R.string.search_logs)) },
                                )
                                IconButton(onClick = onToggleLogcatPause) { Icon(if (logcat.paused) Icons.Default.PlayArrow else Icons.Default.Pause, stringResource(if (logcat.paused) R.string.resume else R.string.pause)) }
                                IconButton(onClick = { clipboard.setText(AnnotatedString(filtered.joinToString("\n") { it.text })) }) {
                                    Icon(Icons.Default.ContentCopy, stringResource(R.string.copy_log))
                                }
                                IconButton(onClick = onClearLogcat) { Icon(Icons.Default.ClearAll, stringResource(R.string.clear)) }
                            }
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                AndroidLogLevel.entries.forEach { level ->
                                    FilterChip(selected = logcat.minimumLevel == level, onClick = { onLogcatLevel(level) }, label = { Text(level.name.take(1)) })
                                }
                            }
                            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                items(filtered, key = { it.id }) { line ->
                                    Text(
                                        line.text,
                                        modifier = Modifier.fillMaxWidth().clickable(enabled = line.filePath != null) { onOpenLogLine(line) }.padding(vertical = 2.dp),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = when (line.level) {
                                            AndroidLogLevel.ERROR, AndroidLogLevel.FATAL -> MaterialTheme.colorScheme.error
                                            AndroidLogLevel.WARNING -> PocketOrange
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                AndroidDashboardSection.HEALTH -> {
                    LazyColumn(
                        Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.project_health), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                IconButton(onClick = onRefreshHealth, enabled = !state.androidHealthRunning) {
                                    if (state.androidHealthRunning) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                    else Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
                                }
                            }
                        }
                        items(state.androidHealthChecks, key = { it.id }) { check ->
                            AndroidHealthCheckCard(check, clipboard, onHealthFix)
                        }
                    }
                }
            }
        }
    }
}

