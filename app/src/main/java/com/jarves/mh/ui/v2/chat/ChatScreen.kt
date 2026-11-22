package com.jarves.mh.ui.v2.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jarves.mh.model.ChangeItem
import com.jarves.mh.model.ChatMessage
import com.jarves.mh.model.ToolRequest
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.components.ForgeAppMode
import com.jarves.mh.ui.v2.components.ForgeCard
import com.jarves.mh.ui.v2.components.ForgeCardStyle
import com.jarves.mh.ui.v2.components.ForgeCheckpointCard
import com.jarves.mh.ui.v2.components.ForgeChip
import com.jarves.mh.ui.v2.components.ForgeChipTone
import com.jarves.mh.ui.v2.components.ForgeInput
import com.jarves.mh.ui.v2.components.ForgeKeyBar
import com.jarves.mh.ui.v2.components.ForgeToolApprovalCard
import com.jarves.mh.ui.v2.theme.ForgeTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChatScreen(
    projectName: String = "PocketForge AI",
    activeModel: String = "Claude 3.5 Sonnet",
    appMode: ForgeAppMode = ForgeAppMode.SIMPLE,
    messages: List<ChatMessage> = emptyList(),
    isRunning: Boolean = false,
    liveThinking: Boolean = false,
    pendingApproval: ToolRequest? = null,
    changes: List<ChangeItem> = emptyList(),
    onSendMessage: (String) -> Unit = {},
    onStopTask: () -> Unit = {},
    onApproveTool: (Boolean) -> Unit = {},
    onUndoChanges: () -> Unit = {},
    onKeepChanges: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onOpenTerminal: (() -> Unit)? = null,
    onOpenPreview: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when a new message arrives or streaming updates
    LaunchedEffect(messages.size, isRunning) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // CHAT HEADER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                        PhosphorIcon(icon = PhosphorIconType.CARET_DOWN, tint = colors.textPrimary)
                    }
                }
                Column {
                    Text(
                        text = projectName,
                        style = typography.titleSmall,
                        color = colors.textPrimary,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        PhosphorIcon(icon = PhosphorIconType.GIT_BRANCH, size = 12.dp, tint = colors.textMuted)
                        Text(text = "main", style = typography.codeSmall, color = colors.textMuted)
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ForgeChip(
                    text = activeModel,
                    icon = PhosphorIconType.SPARKLE,
                    tone = ForgeChipTone.ACCENT,
                )
                if (appMode == ForgeAppMode.PRO && onOpenTerminal != null) {
                    IconButton(onClick = onOpenTerminal, modifier = Modifier.size(28.dp)) {
                        PhosphorIcon(icon = PhosphorIconType.TERMINAL, tint = colors.accent)
                    }
                }
                if (onOpenPreview != null) {
                    IconButton(onClick = onOpenPreview, modifier = Modifier.size(28.dp)) {
                        PhosphorIcon(icon = PhosphorIconType.ARROW_SQUARE_OUT, tint = colors.accent)
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = colors.border)

        // MESSAGE STREAM LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.fromUser) {
                    RealUserMessageBubble(msg)
                } else {
                    RealAgentMessageBubble(msg)
                }
            }

            // Real Interactive Tool Approval Card
            if (pendingApproval != null) {
                item {
                    ForgeToolApprovalCard(
                        toolName = pendingApproval.toolName,
                        commandOrDetails = pendingApproval.commandPreview ?: pendingApproval.explanation,
                        onApproveOnce = { onApproveTool(true) },
                        onApproveAlways = { onApproveTool(true) },
                        onReject = { onApproveTool(false) },
                    )
                }
            }

            // Real Checkpoint Card
            if (changes.isNotEmpty()) {
                item {
                    val summaryText = changes.joinToString("\n") {
                        "${it.path} (+${it.additions}/-${it.deletions})"
                    }
                    ForgeCheckpointCard(
                        changedFileCount = changes.size,
                        summary = summaryText,
                        onAccept = onKeepChanges,
                        onRevert = onUndoChanges,
                        onReviewDiff = {},
                    )
                }
            }

            // Real Streaming & Thinking Indicator
            if (isRunning || liveThinking) {
                item {
                    AgentStreamingIndicator(isThinking = liveThinking)
                }
            }
        }

        // PRO MODE KEYBAR (When Pro mode is active)
        if (appMode == ForgeAppMode.PRO) {
            ForgeKeyBar(onKeyPress = { key -> inputText += key })
        }

        HorizontalDivider(thickness = 1.dp, color = colors.border)

        // CHAT INPUT DOCK
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.size(36.dp),
            ) {
                PhosphorIcon(
                    icon = PhosphorIconType.PAPERCLIP,
                    tint = colors.textSecondary,
                    size = 20.dp,
                )
            }

            ForgeInput(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = "Ask agent to build, edit, or test...",
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 4,
            )

            if (isRunning) {
                // STOP BUTTON when agent is actively running
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(shapes.pill)
                        .background(colors.error)
                        .clickable(onClick = onStopTask),
                    contentAlignment = Alignment.Center,
                ) {
                    PhosphorIcon(
                        icon = PhosphorIconType.X_CIRCLE,
                        size = 20.dp,
                        tint = Color.White,
                    )
                }
            } else {
                // SEND BUTTON
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(shapes.pill)
                        .background(if (inputText.isNotBlank()) colors.primary else colors.surfaceElevated)
                        .clickable(enabled = inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    PhosphorIcon(
                        icon = PhosphorIconType.PAPER_PLANE_RIGHT,
                        size = 18.dp,
                        tint = if (inputText.isNotBlank()) Color.White else colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun RealUserMessageBubble(msg: ChatMessage) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    val timeStr = try {
        DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(msg.createdAt)
    } catch (_: Exception) {
        ""
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(shapes.bubbleUser)
                .background(colors.primary)
                .padding(12.dp)
        ) {
            Text(
                text = msg.text,
                style = typography.bodyMedium,
                color = Color.White,
            )
            if (timeStr.isNotBlank()) {
                Text(
                    text = timeStr,
                    style = typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun RealAgentMessageBubble(msg: ChatMessage) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    var reasoningExpanded by remember { mutableStateOf(false) }

    val timeStr = try {
        DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(msg.createdAt)
    } catch (_: Exception) {
        ""
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(shapes.bubbleAgent)
                .background(colors.surface)
                .border(1.dp, colors.border, shapes.bubbleAgent)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Real Agent Work Items / Collapsible Reasoning
            if (msg.workItems.isNotEmpty()) {
                Surface(
                    onClick = { reasoningExpanded = !reasoningExpanded },
                    shape = shapes.chip,
                    color = colors.surfaceElevated,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        PhosphorIcon(icon = PhosphorIconType.SPARKLE, size = 12.dp, tint = colors.accent)
                        Text(
                            text = if (reasoningExpanded) "Hide Details" else "${msg.workItems.size} activities executed",
                            style = typography.codeSmall,
                            color = colors.accent,
                        )
                        PhosphorIcon(
                            icon = if (reasoningExpanded) PhosphorIconType.CARET_DOWN else PhosphorIconType.CARET_RIGHT,
                            size = 12.dp,
                            tint = colors.accent,
                        )
                    }
                }

                if (reasoningExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.background, shapes.cardSmall)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        msg.workItems.forEach { item ->
                            Text(
                                text = "• ${item.title}: ${item.detail}",
                                style = typography.codeSmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }

            // Message text
            Text(
                text = msg.text,
                style = typography.bodyMedium,
                color = colors.textPrimary,
            )

            // Timestamp
            if (timeStr.isNotBlank()) {
                Text(
                    text = timeStr,
                    style = typography.labelSmall,
                    color = colors.textMuted,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

@Composable
private fun AgentStreamingIndicator(isThinking: Boolean = false) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = colors.accent,
        )
        Text(
            text = if (isThinking) "Agent is reasoning..." else "Agent is responding...",
            style = typography.labelSmall,
            color = colors.accent,
        )
    }
}
