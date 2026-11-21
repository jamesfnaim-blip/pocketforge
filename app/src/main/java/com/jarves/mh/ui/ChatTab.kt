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
import androidx.compose.ui.platform.LocalView

@Composable
internal fun ChatTab(
    messages: List<ChatMessage>,
    approval: ToolRequest?,
    liveProcess: List<ActivityItem>,
    isRunning: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onApproval: (Boolean) -> Unit,
    listState: LazyListState,
    taskStartedAtMillis: Long?,
    taskFinishedAtMillis: Long?,
    thinkingActive: Boolean,
    agentKind: AgentKind,
    pendingAttachments: List<ChatAttachment>,
    onAttach: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onOpenAttachment: (ChatAttachment) -> Unit,
    onRunInTerminal: (String) -> Unit,
    onTakePhoto: () -> Unit = {},
    queuedFollowUps: List<QueuedFollowUp> = emptyList(),
    onSteerFollowUp: (String) -> Unit = {},
    onRemoveFollowUp: (String) -> Unit = {},
    readOnly: Boolean = false,
    readOnlyBlocked: Boolean = false,
    onContinueHere: () -> Unit = {},
) {
    val view = LocalView.current
    // Keep the screen on while the selected agent is working in this chat. Released automatically
    // when the task finishes or the user leaves the chat tab.
    DisposableEffect(isRunning) {
        view.keepScreenOn = isRunning
        onDispose { view.keepScreenOn = false }
    }
    var prompt by rememberSaveable { mutableStateOf("") }
    val chatScope = rememberCoroutineScope()
    // True while the newest item (message, live panel, or approval card) is on screen.
    val readerAtBottom by remember {
        derivedStateOf {
            !listState.canScrollForward
        }
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        Box(Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages, key = { it.id }) { message ->
                    if (message.workItems.isNotEmpty()) {
                        WorkBlockCard(message)
                    } else {
                        MessageBubble(message, onRunInTerminal, onOpenAttachment)
                    }
                }
                if (liveProcess.isNotEmpty() || thinkingActive) {
                    item(key = "live-claude-process") {
                        LiveClaudeProcess(
                            processItems = liveProcess,
                            isRunning = isRunning,
                            startedAtMillis = taskStartedAtMillis,
                            finishedAtMillis = taskFinishedAtMillis,
                            thinkingActive = thinkingActive,
                        )
                    }
                }
                approval?.let { request -> item { ApprovalCard(request, onApproval) } }
            }
            if (!readerAtBottom) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .clickable {
                            chatScope.launch {
                                listState.animateScrollToItem(
                                    (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0),
                                )
                            }
                        },
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        Modifier.padding(start = 13.dp, end = 15.dp, top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.ws_latest),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
        if (readOnly) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.ws_readonly_history), fontWeight = FontWeight.SemiBold)
                        Text(
                            if (readOnlyBlocked) {
                                stringResource(R.string.ws_readonly_blocked)
                            } else {
                                stringResource(R.string.ws_readonly_finished)
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!readOnlyBlocked) {
                        TextButton(onClick = onContinueHere) { Text(stringResource(R.string.ws_open)) }
                    }
                }
            }
        } else Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (queuedFollowUps.isNotEmpty()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        queuedFollowUps.forEach { followUp ->
                            QueuedFollowUpCard(
                                followUp = followUp,
                                onSteer = { onSteerFollowUp(followUp.id) },
                                onRemove = { onRemoveFollowUp(followUp.id) },
                            )
                        }
                    }
                }
                if (pendingAttachments.isNotEmpty()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        pendingAttachments.forEach { attachment ->
                            AttachmentChip(
                                attachment = attachment,
                                onOpen = null,
                                onRemove = { onRemoveAttachment(attachment.id) },
                            )
                        }
                    }
                }

                val canSend = prompt.isNotBlank() || pendingAttachments.isNotEmpty()

                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (canSend) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        var attachMenuOpen by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { attachMenuOpen = true },
                                enabled = pendingAttachments.size < 5,
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ph_paperclip),
                                    contentDescription = stringResource(R.string.chat_attach_files),
                                    modifier = Modifier.size(20.dp),
                                    tint = if (pendingAttachments.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            DropdownMenu(expanded = attachMenuOpen, onDismissRequest = { attachMenuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.chat_take_photo)) },
                                    leadingIcon = { Icon(painterResource(R.drawable.ph_camera), null, modifier = Modifier.size(18.dp)) },
                                    onClick = { attachMenuOpen = false; onTakePhoto() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.chat_choose_files)) },
                                    leadingIcon = { Icon(painterResource(R.drawable.ph_paperclip), null, modifier = Modifier.size(18.dp)) },
                                    onClick = { attachMenuOpen = false; onAttach() },
                                )
                            }
                        }

                        BasicTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp, vertical = 10.dp)
                                .heightIn(min = 20.dp, max = 130.dp),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (prompt.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.chat_message_agent, stringResource(agentKind.titleRes())),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 15.sp,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )

                        Spacer(Modifier.width(4.dp))

                        if (isRunning) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = CircleShape,
                                    )
                                    .clickable(onClick = onStop),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ph_x_circle),
                                    contentDescription = stringResource(R.string.ws_stop_ai_task),
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            if (canSend) Spacer(Modifier.width(6.dp))
                        }
                        if (!isRunning || canSend) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape,
                                    )
                                    .clickable(
                                        enabled = canSend,
                                        onClick = {
                                            if (canSend) {
                                                onSend(prompt)
                                                prompt = ""
                                            }
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ph_paper_plane_right),
                                    contentDescription = stringResource(R.string.chat_send),
                                    tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

