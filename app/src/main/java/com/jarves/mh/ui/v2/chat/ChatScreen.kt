package com.jarves.mh.ui.v2.chat

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

data class ChatMessageItem(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val reasoning: String? = null,
    val activityLogs: List<String> = emptyList(),
    val pendingTool: String? = null,
    val checkpointFiles: Int = 0,
)

@Composable
fun ChatScreen(
    projectName: String = "PocketForge AI",
    activeModel: String = "Claude 3.5 Sonnet",
    appMode: ForgeAppMode = ForgeAppMode.SIMPLE,
    onBack: (() -> Unit)? = null,
    onOpenTerminal: (() -> Unit)? = null,
    onOpenPreview: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    var inputText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessageItem(
                id = "1",
                isUser = true,
                text = "Rebuild the UI layer from the approved mockup. Use ForgeDesign, clean components, and zero emoji.",
                timestamp = "10:15 AM",
            ),
            ChatMessageItem(
                id = "2",
                isUser = false,
                text = "I have initiated the complete UI redesign under `ui/v2`! The Obsidian and AMOLED dark themes are applied, components are organized in modular files under 400 lines each, and Phosphor icons are used exclusively.",
                timestamp = "10:16 AM",
                reasoning = "Reviewed docs/mockups/index.html and docs/design-system.md. Confirmed 18 screens and token mapping. Starting UI-0 foundation, UI-1 setup, and UI-2 agent chat.",
                activityLogs = listOf("Checked docs/design-system.md", "Created ForgeColors.kt, ForgeShapes.kt", "Created SetupWizardScreen.kt, HomeScreen.kt"),
                checkpointFiles = 6,
            ),
        )
    }

    val listState = rememberLazyListState()

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
                if (msg.isUser) {
                    UserMessageBubble(msg)
                } else {
                    AgentMessageBubble(msg)
                }
            }

            if (isStreaming) {
                item {
                    AgentStreamingIndicator()
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

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(shapes.pill)
                    .background(if (inputText.isNotBlank()) colors.primary else colors.surfaceElevated)
                    .clickable(enabled = inputText.isNotBlank()) {
                        messages.add(
                            ChatMessageItem(
                                id = System.currentTimeMillis().toString(),
                                isUser = true,
                                text = inputText,
                                timestamp = "Now",
                            )
                        )
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

@Composable
private fun UserMessageBubble(msg: ChatMessageItem) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

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
            Text(
                text = msg.timestamp,
                style = typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun AgentMessageBubble(msg: ChatMessageItem) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    var reasoningExpanded by remember { mutableStateOf(false) }

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
            // Collapsible reasoning
            if (msg.reasoning != null) {
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
                            text = if (reasoningExpanded) "Hide Reasoning" else "Thought Process",
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
                    Text(
                        text = msg.reasoning,
                        style = typography.codeSmall,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.background, shapes.cardSmall)
                            .padding(8.dp),
                    )
                }
            }

            // Message text
            Text(
                text = msg.text,
                style = typography.bodyMedium,
                color = colors.textPrimary,
            )

            // Pending Tool Approval Card (if any)
            if (msg.pendingTool != null) {
                ForgeToolApprovalCard(
                    toolName = msg.pendingTool,
                    commandOrDetails = "git clone https://github.com/example/repo.git",
                    onApproveOnce = {},
                    onApproveAlways = {},
                    onReject = {},
                )
            }

            // Checkpoint Card (if files were modified)
            if (msg.checkpointFiles > 0) {
                ForgeCheckpointCard(
                    changedFileCount = msg.checkpointFiles,
                    summary = "Added ForgeColors.kt, ForgeShapes.kt, ForgeTheme.kt, and SetupWizardScreen.kt",
                    onAccept = {},
                    onRevert = {},
                    onReviewDiff = {},
                )
            }

            // Timestamp
            Text(
                text = msg.timestamp,
                style = typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun AgentStreamingIndicator() {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 8.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = colors.accent,
        )
        Text(
            text = "Agent is working...",
            style = typography.labelSmall,
            color = colors.accent,
        )
    }
}
