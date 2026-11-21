package com.jarves.mh.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarves.mh.R
import com.jarves.mh.model.ActivityItem
import kotlinx.coroutines.delay

internal fun ActivitySummaryRow(
    item: ActivityItem?,
    text: String,
    expanded: Boolean,
    showProgress: Boolean,
    onToggle: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            activityIcon(item),
            null,
            Modifier.size(16.dp),
            tint = muted,
        )
        Spacer(Modifier.width(9.dp))
        Text(text, Modifier.weight(1f), fontSize = 13.sp, color = muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (showProgress) {
            AnimatedThinkingDots(dotColor = muted)
            Spacer(Modifier.width(6.dp))
        }
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            if (expanded) stringResource(R.string.ws_collapse_activity) else stringResource(R.string.ws_expand_activity),
            Modifier.size(18.dp),
            tint = muted,
        )
    }
}

@Composable
internal fun activityIcon(item: ActivityItem?): ImageVector {
    // "Files changed" is stored in the language active when it was recorded; match both
    // the current translation and the English title kept in older chat history.
    val filesChangedTitle = stringResource(R.string.vm_files_changed)
    if (item == null) return Icons.Default.AutoAwesome
    val task = item.title
        .removePrefix("Running ")
        .removeSuffix(" completed")
        .trim()
    return when {
        item.isCommand || task.equals("Bash", ignoreCase = true) -> Icons.Default.Terminal
        task.equals("Write", ignoreCase = true) ||
            task.equals("Edit", ignoreCase = true) ||
            task.equals("NotebookEdit", ignoreCase = true) -> Icons.Default.Edit
        task.equals("Read", ignoreCase = true) -> Icons.Default.Description
        task.equals("Glob", ignoreCase = true) ||
            task.equals("Grep", ignoreCase = true) -> Icons.Default.Search
        item.title == filesChangedTitle || task.contains("file", ignoreCase = true) -> Icons.Default.Description
        else -> Icons.Default.AutoAwesome
    }
}

@Composable
internal fun ActivityExpandedDetail(item: ActivityItem?, detail: String) {
    if (item?.isCommand == true) {
        Text(
            detail,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp, end = 8.dp, bottom = 8.dp),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )
    } else {
        MarkdownText(
            markdown = detail,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp, end = 8.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun compactActivityText(item: ActivityItem): String =
    "${activityDisplayName(item)} · ${activityDetail(item).take(180).replace(Regex("\\s+"), " ").take(105)}"

@Composable
internal fun activityDetail(item: ActivityItem): String {
    if (item.title == "Think" && item.detail.contains("reasoning tokens processed", true)) {
        return stringResource(R.string.ws_activity_reviewed_request)
    }
    return item.detail.ifBlank { activityDisplayName(item) }
}

@Composable
internal fun activityHeadline(items: List<ActivityItem>, seconds: Long, thinking: Boolean): String {
    val latest = items.lastOrNull()
    val thinkLabel = stringResource(R.string.ws_activity_think)
    if (latest == null) return "$thinkLabel · ${stringResource(R.string.ws_activity_analyzing_request)} · ${formatDuration(seconds)}"
    if (thinking && latest.title == "Think") return "$thinkLabel · ${latest.detail} · ${formatDuration(seconds)}"
    val latestName = activityDisplayName(latest)
    val detail = latest.detail.take(180).replace(Regex("\\s+"), " ").trim().ifBlank { latestName }
    return "$latestName · ${detail.take(100)} · ${formatDuration(seconds)}"
}

/**
 * Activity titles are stored as written: internal English markers ("Think", "Task stopped",
 * "Running <tool>", "<tool> completed") that the logic above parses, titles already localized
 * by MainViewModel, or English titles persisted by older versions. Translate at display time.
 */
@Composable
internal fun activityDisplayName(item: ActivityItem): String {
    val legacyRes = legacyActivityTitles[item.title]
    return when {
        item.title == "Think" -> stringResource(R.string.ws_activity_think)
        item.title.startsWith("Task stopped") -> stringResource(R.string.ws_task_stopped)
        legacyRes != null -> stringResource(legacyRes)
        // "Running <tool>" / "<tool> completed" display as the tool name (a language-neutral ID).
        else -> activityName(item)
    }
}

/** English activity titles emitted by runtime bridges or saved by earlier app versions. */
internal val legacyActivityTitles: Map<String, Int> = mapOf(
    "Understanding your request" to R.string.vm_understanding_request,
    "Changes kept" to R.string.vm_changes_kept,
    "Changes undone" to R.string.vm_changes_undone,
    "Undo unavailable" to R.string.vm_undo_unavailable,
    "Waiting for approval" to R.string.vm_waiting_approval,
    "Applying approved changes" to R.string.vm_applying_approved,
    "Action approved" to R.string.vm_action_approved,
    "Action rejected" to R.string.vm_action_rejected,
    "Files changed" to R.string.vm_files_changed,
    "Preview ready" to R.string.vm_preview_ready,
    "Task completed" to R.string.vm_task_completed,
    "Task interrupted" to R.string.vm_task_interrupted,
    "API key switched" to R.string.vm_api_key_switched,
    "Permission denied" to R.string.md_activity_permission_denied,
)

internal fun activityName(item: ActivityItem): String = item.title
    .removePrefix("Running ")
    .removeSuffix(" completed")
    .replaceFirstChar { it.uppercase() }

@Composable
internal fun completedProcessSummary(
    processItems: List<ActivityItem>,
    startedAtMillis: Long?,
    finishedAtMillis: Long?,
): String {
    val stopped = processItems.lastOrNull()?.title?.startsWith("Task stopped") == true
    val outcome = if (stopped) stringResource(R.string.ws_task_stopped) else stringResource(R.string.ws_task_completed)
    val steps = if (processItems.size == 1) stringResource(R.string.ws_steps_one) else stringResource(R.string.ws_steps_many, processItems.size)
    val duration = startedAtMillis?.let { start ->
        val end = finishedAtMillis ?: System.currentTimeMillis()
        formatDuration(((end - start) / 1000L).coerceAtLeast(0))
    }
    return if (duration != null) "$outcome · $duration · $steps" else "$outcome · $steps"
}

@Composable
internal fun rememberLiveElapsedSeconds(startedAtMillis: Long): Int {
    var seconds by remember(startedAtMillis) {
        mutableIntStateOf(((System.currentTimeMillis() - startedAtMillis) / 1000L).toInt().coerceAtLeast(0))
    }
    LaunchedEffect(startedAtMillis) {
        while (true) {
            delay(1_000)
            seconds = ((System.currentTimeMillis() - startedAtMillis) / 1000L).toInt().coerceAtLeast(0)
        }
    }
    return seconds
}

@Composable
internal fun formatDuration(totalSeconds: Long): String = when {
    totalSeconds >= 3_600 -> stringResource(R.string.ws_duration_hm, totalSeconds / 3_600, (totalSeconds % 3_600) / 60)
    totalSeconds >= 60 -> stringResource(R.string.ws_duration_ms, totalSeconds / 60, totalSeconds % 60)
    else -> stringResource(R.string.ws_duration_s, totalSeconds)
}

@Composable
