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

internal fun LiveClaudeProcess(
    processItems: List<ActivityItem>,
    isRunning: Boolean,
    startedAtMillis: Long?,
    finishedAtMillis: Long?,
    thinkingActive: Boolean,
) {
    val elapsedSeconds = startedAtMillis?.let { rememberLiveElapsedSeconds(it).toLong() } ?: 0L
    ClaudeActivityDisclosure(
        items = processItems,
        headline = activityHeadline(processItems, elapsedSeconds, thinkingActive),
        isRunning = isRunning,
    )
}

@Composable
internal fun WorkBlockCard(message: ChatMessage) {
    val seconds = (message.workedMillis / 1_000L).coerceAtLeast(1L)
    Column {
        ClaudeActivityDisclosure(
            items = message.workItems,
            headline = activityHeadline(message.workItems, seconds, message.workItems.isEmpty()),
        )
        if (message.workItems.lastOrNull()?.title?.startsWith("Task stopped") == true) {
            Text(
                text = stringResource(R.string.ws_worked_for, formatDuration(seconds)),
                modifier = Modifier.padding(start = 29.dp, bottom = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
internal fun ClaudeActivityDisclosure(
    items: List<ActivityItem>,
    headline: String,
    isRunning: Boolean = false,
) {
    var expandedItems by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    // Older saved chats may contain hundreds of polling steps. Rendering all of them inside
    // one lazy-list item can monopolize the UI thread, so show the latest useful window.
    val visibleItems = items.takeLast(32)
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
        if (visibleItems.isEmpty()) {
            ActivitySummaryRow(
                item = null,
                text = headline,
                expanded = 0 in expandedItems,
                showProgress = isRunning,
                onToggle = {
                    expandedItems = if (0 in expandedItems) expandedItems - 0 else expandedItems + 0
                },
            )
            if (0 in expandedItems) ActivityExpandedDetail(null, stringResource(R.string.ws_activity_reviewing_request))
        } else {
            visibleItems.forEachIndexed { index, item ->
                ActivitySummaryRow(
                    item = item,
                    text = compactActivityText(item),
                    expanded = index in expandedItems,
                    showProgress = isRunning && !item.isComplete,
                    onToggle = {
                        expandedItems = if (index in expandedItems) expandedItems - index else expandedItems + index
                    },
                )
                if (index in expandedItems) ActivityExpandedDetail(item, activityDetail(item))
            }
        }
    }
}

