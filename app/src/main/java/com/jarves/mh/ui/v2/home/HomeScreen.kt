package com.jarves.mh.ui.v2.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jarves.mh.R
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.components.ForgeCard
import com.jarves.mh.ui.v2.components.ForgeCardStyle
import com.jarves.mh.ui.v2.components.ForgeChip
import com.jarves.mh.ui.v2.components.ForgeChipTone
import com.jarves.mh.ui.v2.components.ForgeSubsystemStatusCard
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun HomeScreen(
    userName: String = "Nayem",
    isUbuntuRunning: Boolean = true,
    serverUrl: String? = "http://localhost:3000",
    onStartNewTask: () -> Unit,
    onOpenProject: (String) -> Unit,
    onNewProject: () -> Unit,
    onCloneRepo: () -> Unit,
    onTemplates: () -> Unit,
    onOpenClassicUi: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // GREETING HEADER
        Column {
            Text(
                text = "${stringResource(id = R.string.greeting_day)} $userName",
                style = typography.displayMedium,
                color = colors.textPrimary,
            )
            Text(
                text = "What would you like to build today?",
                style = typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // RUNNING SUBSYSTEM STATUS CARD
        ForgeSubsystemStatusCard(
            isRunning = isUbuntuRunning,
            osDescription = "Ubuntu 20.04 LTS (PRoot ARM64)",
            activeServerUrl = serverUrl,
        )

        // PROMINENT CLASSIC UI TRANSITION BANNER
        if (onOpenClassicUi != null) {
            ForgeCard(
                style = ForgeCardStyle.ELEVATED,
                onClick = onOpenClassicUi,
                contentPadding = 12.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PhosphorIcon(
                            icon = PhosphorIconType.ARROW_CLOCKWISE,
                            size = 20.dp,
                            tint = colors.accent,
                        )
                        Column {
                            Text(
                                text = "Switch to Classic Mobile Harness UI",
                                style = typography.titleSmall,
                                color = colors.textPrimary,
                            )
                            Text(
                                text = "Access Files, Full Terminal, DevStack & Android Builder",
                                style = typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                    PhosphorIcon(
                        icon = PhosphorIconType.CARET_RIGHT,
                        size = 18.dp,
                        tint = colors.textMuted,
                    )
                }
            }
        }

        // HERO CTA: START AUTONOMOUS TASK
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapes.card)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(colors.primary, Color(0xFF4F46E5), colors.accent)
                    )
                )
                .clickable(onClick = onStartNewTask)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(shapes.chip)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        PhosphorIcon(
                            icon = PhosphorIconType.SPARKLE,
                            size = 22.dp,
                            tint = Color.White,
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(id = R.string.cta_new_task),
                            style = typography.titleMedium,
                            color = Color.White,
                        )
                        Text(
                            text = "Describe your goal in plain language",
                            style = typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
                PhosphorIcon(
                    icon = PhosphorIconType.PAPER_PLANE_RIGHT,
                    size = 20.dp,
                    tint = Color.White,
                )
            }
        }

        // QUICK ACTION CHIPS ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ForgeChip(
                text = "New Project",
                icon = PhosphorIconType.PLUS,
                onClick = onNewProject,
            )
            ForgeChip(
                text = "Clone Git",
                icon = PhosphorIconType.GIT_BRANCH,
                onClick = onCloneRepo,
            )
            ForgeChip(
                text = "Templates",
                icon = PhosphorIconType.FILE_CODE,
                onClick = onTemplates,
            )
            ForgeChip(
                text = "DevStack",
                icon = PhosphorIconType.CPU,
                onClick = {},
            )
        }

        // RECENT PROJECTS SECTION
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.recent_projects),
                    style = typography.titleMedium,
                    color = colors.textPrimary,
                )
                Text(
                    text = "View all",
                    style = typography.labelSmall,
                    color = colors.accent,
                    modifier = Modifier.clickable { onOpenProject("all") },
                )
            }

            ProjectItemCard(
                name = "PocketForge AI",
                stack = "Android Jetpack Compose",
                lastActive = "2m ago",
                branch = "main",
                onClick = { onOpenProject("PocketForge AI") },
            )
            ProjectItemCard(
                name = "My Portfolio Web",
                stack = "React / Vite / Tailwind",
                lastActive = "1h ago",
                branch = "feat/v2",
                onClick = { onOpenProject("My Portfolio Web") },
            )
            ProjectItemCard(
                name = "FastAPI Backend",
                stack = "Python 3.11 / SQLite",
                lastActive = "Yesterday",
                branch = "main",
                onClick = { onOpenProject("FastAPI Backend") },
            )
        }
    }
}

@Composable
private fun ProjectItemCard(
    name: String,
    stack: String,
    lastActive: String,
    branch: String,
    onClick: () -> Unit,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    ForgeCard(
        style = ForgeCardStyle.DEFAULT,
        onClick = onClick,
        contentPadding = 12.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PhosphorIcon(
                    icon = PhosphorIconType.FOLDER_SIMPLE,
                    size = 22.dp,
                    tint = colors.primary,
                )
                Column {
                    Text(
                        text = name,
                        style = typography.titleSmall,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = stack,
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = lastActive,
                    style = typography.labelSmall,
                    color = colors.textMuted,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PhosphorIcon(icon = PhosphorIconType.GIT_BRANCH, size = 12.dp, tint = colors.textMuted)
                    Text(text = branch, style = typography.codeSmall, color = colors.textMuted)
                }
            }
        }
    }
}
