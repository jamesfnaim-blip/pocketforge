package com.jarves.mh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarves.mh.R
import com.jarves.mh.model.DevStack
import com.jarves.mh.runtime.AndroidAction
import com.jarves.mh.runtime.AndroidBuildIssue
import com.jarves.mh.runtime.AndroidBuildStage
import com.jarves.mh.runtime.AndroidHealthCheck
import com.jarves.mh.runtime.AndroidHealthFix
import com.jarves.mh.runtime.AndroidHealthStatus
import com.jarves.mh.ui.theme.PocketGreen
import com.jarves.mh.ui.theme.PocketOrange

internal fun ProfessionalAndroidOverview(
    state: AppUiState,
    liveElapsedSeconds: Int?,
    showRawLog: Boolean,
    onToggleRawLog: () -> Unit,
    onAction: (AndroidAction) -> Unit,
    onCancelBuild: () -> Unit,
    onInstallTools: () -> Unit,
    onOpenIssue: (AndroidBuildIssue) -> Unit,
) {
    val apk = state.androidApkInfo
    val toolsInstalled = DevStack.ANDROID in state.installedDevStacks
    val primaryAction = when {
        apk == null -> AndroidAction.BUILD
        apk.stale -> AndroidAction.BUILD_AND_RUN
        apk.installedMatches -> AndroidAction.RUN
        else -> AndroidAction.INSTALL
    }
    val primaryLabel = when (primaryAction) {
        AndroidAction.BUILD -> R.string.build
        AndroidAction.INSTALL -> R.string.install
        AndroidAction.RUN -> R.string.run_app
        else -> R.string.build_and_run
    }
    val statusTitle = when {
        state.androidBuildRunning -> androidStageLabel(state.androidBuildStage)
        state.androidBuildStage == AndroidBuildStage.FAILED -> stringResource(R.string.android_build_failed_title)
        apk == null -> stringResource(R.string.android_ready_to_build)
        apk.stale -> stringResource(R.string.android_ready_to_rebuild)
        apk.installedMatches -> stringResource(R.string.android_ready_to_run)
        else -> stringResource(R.string.android_ready_to_install)
    }
    val statusDescription = when {
        state.androidBuildRunning -> state.androidBuildMessage ?: androidStageLabel(state.androidBuildStage)
        apk == null -> stringResource(R.string.android_no_apk_description)
        apk.stale -> stringResource(R.string.android_source_changed_short)
        apk.installedMatches -> stringResource(R.string.android_build_current_installed)
        else -> stringResource(R.string.android_build_current_not_installed)
    }
    val elapsedSeconds = state.androidBuildStartedAtMillis?.let { start ->
        if (state.androidBuildRunning) liveElapsedSeconds?.toLong()
        else ((state.androidBuildFinishedAtMillis ?: start) - start).coerceAtLeast(0L) / 1_000L
    }
    val expectedBuildSeconds = state.androidBuildRecentDurationsMillis
        .sorted()
        .takeIf { it.isNotEmpty() }
        ?.let { durations -> durations[durations.size / 2] / 1_000L }
        ?.coerceAtLeast(1L)
    val estimateText = when {
        state.androidBuildRunning && expectedBuildSeconds != null -> {
            val remaining = (expectedBuildSeconds - (elapsedSeconds ?: 0L)).coerceAtLeast(0L)
            if (remaining > 0L) stringResource(R.string.android_estimated_remaining, formatDuration(remaining))
            else stringResource(R.string.android_finishing_build)
        }
        state.androidBuildRunning -> stringResource(R.string.android_estimating_build)
        primaryAction in setOf(AndroidAction.BUILD, AndroidAction.BUILD_AND_RUN) && expectedBuildSeconds != null ->
            stringResource(R.string.android_usually_takes, formatDuration(expectedBuildSeconds))
        else -> null
    }
    val buildSummary = when {
        state.androidBuildRunning -> stringResource(R.string.android_build_in_progress, formatDuration(elapsedSeconds ?: 0L))
        state.androidBuildStage == AndroidBuildStage.FAILED -> state.androidBuildMessage ?: stringResource(R.string.android_build_failed_title)
        state.androidBuildFinishedAtMillis != null -> stringResource(R.string.android_completed_in, formatDuration(elapsedSeconds ?: 0L))
        else -> stringResource(R.string.android_no_build_yet)
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)),
                shape = RoundedCornerShape(22.dp),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = when {
                                state.androidBuildStage == AndroidBuildStage.FAILED -> MaterialTheme.colorScheme.errorContainer
                                apk?.stale == true -> PocketOrange.copy(alpha = 0.18f)
                                else -> PocketGreen.copy(alpha = 0.18f)
                            },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (state.androidBuildRunning) {
                                    CircularProgressIndicator(Modifier.size(26.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Icon(
                                        if (state.androidBuildStage == AndroidBuildStage.FAILED || apk?.stale == true) Icons.Default.Warning else Icons.Default.Check,
                                        null,
                                        tint = if (state.androidBuildStage == AndroidBuildStage.FAILED) MaterialTheme.colorScheme.error else if (apk?.stale == true) PocketOrange else PocketGreen,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(statusTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(statusDescription, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            apk?.let {
                                Text(
                                    "${it.fileName} · ${formatFileSize(it.sizeBytes)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                                    fontSize = 11.sp,
                                )
                            }
                            estimateText?.let {
                                Text(it, color = PocketOrange, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (state.androidBuildRunning) LinearProgressIndicator(Modifier.fillMaxWidth())
                    if (!toolsInstalled) {
                        Button(onClick = onInstallTools, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                            Text(stringResource(R.string.install_android_tools))
                        }
                    } else if (state.androidBuildRunning) {
                        OutlinedButton(onClick = onCancelBuild, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                            Icon(Icons.Default.Stop, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.cancel_operation))
                        }
                    } else {
                        Button(
                            onClick = { onAction(primaryAction) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(primaryLabel), fontWeight = FontWeight.SemiBold)
                        }
                        if (apk != null) {
                            TextButton(onClick = { onAction(AndroidAction.FORCE_BUILD) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text(stringResource(R.string.android_build_again))
                            }
                        }
                    }
                }
            }
        }

        if (state.androidBuildIssues.isNotEmpty()) {
            item { Text(stringResource(R.string.build_issues), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(state.androidBuildIssues) { rawIssue ->
                val issue = rawIssue.localized(LocalContext.current)
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(enabled = issue.filePath != null) { onOpenIssue(issue) },
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(issue.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(issue.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        issue.suggestion?.let { Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)) }
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(20.dp)) {
                AndroidDashboardSummaryRow(
                    icon = Icons.Default.Description,
                    title = stringResource(R.string.android_build_output),
                    subtitle = buildSummary,
                    trailing = stringResource(R.string.android_view),
                    tint = PocketOrange,
                    onClick = onToggleRawLog,
                )
            }
        }

        if (showRawLog) {
            item {
                Surface(color = Color(0xFF11141A), shape = RoundedCornerShape(16.dp)) {
                    SelectionContainer {
                        Text(
                            state.androidBuildLog.ifBlank { stringResource(R.string.android_no_build_output) },
                            Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()).padding(14.dp),
                            color = Color(0xFFD7DEE9),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AndroidDashboardSummaryRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1)
        }
        trailing?.let {
            Text(it, color = PocketOrange, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun AndroidHealthCheckCard(
    check: AndroidHealthCheck,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    onFix: (AndroidHealthFix) -> Unit,
) {
    val tint = when (check.status) {
        AndroidHealthStatus.PASSED -> PocketGreen
        AndroidHealthStatus.WARNING -> PocketOrange
        AndroidHealthStatus.FAILED -> MaterialTheme.colorScheme.error
        AndroidHealthStatus.RUNNING -> MaterialTheme.colorScheme.primary
    }
    Card {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (check.status == AndroidHealthStatus.PASSED) Icons.Default.Check else Icons.Default.Warning, null, tint = tint)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(check.title, fontWeight = FontWeight.SemiBold)
                Text(check.detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (check.fix != AndroidHealthFix.NONE) {
                TextButton(onClick = {
                    if (check.fix == AndroidHealthFix.COPY_LOGCAT_COMMAND) clipboard.setText(AnnotatedString(MainViewModel.LOGCAT_GRANT_COMMAND))
                    else onFix(check.fix)
                }) { Text(stringResource(if (check.fix == AndroidHealthFix.COPY_LOGCAT_COMMAND) R.string.copy_command else R.string.fix)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
