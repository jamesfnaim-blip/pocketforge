package com.jarves.mh.ui.v2.bugreport

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarves.mh.BuildConfig
import com.jarves.mh.ui.DeveloperDiagnostics
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.components.ForgeButton
import com.jarves.mh.ui.v2.components.ForgeButtonStyle
import com.jarves.mh.ui.v2.components.ForgeCard
import com.jarves.mh.ui.v2.components.ForgeCardStyle
import com.jarves.mh.ui.v2.components.ForgeChip
import com.jarves.mh.ui.v2.components.ForgeChipTone
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun BugReportScreen(
    diagnostics: DeveloperDiagnostics,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    var copiedToClipboard by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                PhosphorIcon(icon = PhosphorIconType.CARET_DOWN, tint = colors.textPrimary)
            }
            Text(
                text = "Diagnostics & Bug Report",
                style = typography.titleMedium,
                color = colors.textPrimary,
            )
        }

        // HONEST PRIVACY NOTICE CARD
        ForgeCard(style = ForgeCardStyle.SUCCESS_TINT) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhosphorIcon(icon = PhosphorIconType.SHIELD_CHECK, tint = colors.success)
                Text(
                    text = "Honest Data & Privacy Guarantee",
                    style = typography.labelLarge,
                    color = colors.success,
                )
            }
            Text(
                text = "This report collects system performance metrics, memory counters, and buffer lengths only. No project code, user prompts, or secret API keys are ever included or sent.",
                style = typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // ACTION BUTTONS ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ForgeButton(
                text = if (diagnostics.loading) "Refreshing…" else "Refresh Diagnostics",
                icon = PhosphorIconType.ARROW_CLOCKWISE,
                onClick = onRefresh,
                enabled = !diagnostics.loading,
                style = ForgeButtonStyle.OUTLINE,
                modifier = Modifier.weight(1f),
            )
            ForgeButton(
                text = if (copiedToClipboard) "Copied!" else "Copy Report",
                icon = PhosphorIconType.CHECK_CIRCLE,
                onClick = {
                    val reportText = buildDiagnosticText(diagnostics)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("PocketForge Diagnostics", reportText))
                    copiedToClipboard = true
                },
                style = ForgeButtonStyle.PRIMARY,
                modifier = Modifier.weight(1f),
            )
        }

        // MEMORY & RUNTIME HEALTH
        ForgeCard(style = ForgeCardStyle.DEFAULT) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Memory & Runtime Health", style = typography.titleSmall, color = colors.textPrimary)
                DiagnosticRow("App PSS Memory", formatDiagnosticBytes(diagnostics.appPssBytes))
                DiagnosticRow("Java Heap Allocation", "${formatDiagnosticBytes(diagnostics.javaHeapBytes)} / ${formatDiagnosticBytes(diagnostics.javaHeapMaxBytes)}")
                DiagnosticRow("Logcat Buffer", "${diagnostics.logcatLines} lines")
                DiagnosticRow("Terminal Buffer", formatDiagnosticBytes(diagnostics.terminalBufferBytes))
                DiagnosticRow("Build Log Size", formatDiagnosticBytes(diagnostics.buildLogBytes))
            }
        }

        // ACTIVE PROCESSES
        ForgeCard(style = ForgeCardStyle.DEFAULT) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Active Subsystem Processes", style = typography.titleSmall, color = colors.textPrimary)
                    ForgeChip(
                        text = "${diagnostics.activeProcesses.size} running",
                        tone = if (diagnostics.activeProcesses.isNotEmpty()) ForgeChipTone.ACCENT else ForgeChipTone.NEUTRAL,
                    )
                }
                if (diagnostics.activeProcesses.isEmpty()) {
                    Text(text = "No active background daemon processes.", style = typography.bodySmall, color = colors.textMuted)
                } else {
                    diagnostics.activeProcesses.forEach { proc ->
                        Text(text = "• $proc", style = typography.codeSmall, color = colors.textSecondary)
                    }
                }
            }
        }

        // RECENT EXIT OR ERROR
        ForgeCard(style = ForgeCardStyle.DEFAULT) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Recent Process Termination", style = typography.titleSmall, color = colors.textPrimary)
                Text(
                    text = diagnostics.recentExit ?: "None recorded. Subsystem operating stably.",
                    style = typography.codeSmall,
                    color = if (diagnostics.recentExit != null) colors.error else colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = typography.bodySmall, color = colors.textSecondary)
        Text(text = value, style = typography.codeSmall, color = colors.textPrimary)
    }
}

private fun formatDiagnosticBytes(bytes: Long): String = when {
    bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L -> "%.1f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
}

private fun buildDiagnosticText(d: DeveloperDiagnostics): String = buildString {
    appendLine("PocketForge AI Diagnostic Report")
    appendLine("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    appendLine("App PSS Memory: ${formatDiagnosticBytes(d.appPssBytes)}")
    appendLine("Java Heap: ${formatDiagnosticBytes(d.javaHeapBytes)} / ${formatDiagnosticBytes(d.javaHeapMaxBytes)}")
    appendLine("Logcat Lines: ${d.logcatLines}")
    appendLine("Terminal Buffer: ${formatDiagnosticBytes(d.terminalBufferBytes)}")
    appendLine("Build Log: ${formatDiagnosticBytes(d.buildLogBytes)}")
    appendLine("Active Processes: ${d.activeProcesses.joinToString(", ")}")
    appendLine("Recent Exit: ${d.recentExit ?: "None"}")
}
