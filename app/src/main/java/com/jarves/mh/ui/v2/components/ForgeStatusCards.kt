package com.jarves.mh.ui.v2.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun ForgeSubsystemStatusCard(
    isRunning: Boolean,
    osDescription: String = "Ubuntu 20.04 LTS (ARM64)",
    activeServerUrl: String? = "http://localhost:3000",
    modifier: Modifier = Modifier,
    onManageClick: (() -> Unit)? = null,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    ForgeCard(
        modifier = modifier,
        style = ForgeCardStyle.SUCCESS_TINT,
        onClick = onManageClick,
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
                    icon = if (isRunning) PhosphorIconType.CHECK_CIRCLE else PhosphorIconType.WARNING_CIRCLE,
                    size = 20.dp,
                    tint = if (isRunning) colors.success else colors.warning,
                )
                Column {
                    Text(
                        text = if (isRunning) "Subsystem Active" else "Subsystem Standby",
                        style = typography.titleSmall,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = osDescription,
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
            ForgeChip(
                text = if (isRunning) "Running" else "Stopped",
                tone = if (isRunning) ForgeChipTone.SUCCESS else ForgeChipTone.NEUTRAL,
            )
        }

        if (activeServerUrl != null && isRunning) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PhosphorIcon(
                    icon = PhosphorIconType.ARROW_SQUARE_OUT,
                    size = 14.dp,
                    tint = colors.accent,
                )
                Text(
                    text = "Dev server listening on $activeServerUrl",
                    style = typography.codeSmall,
                    color = colors.accent,
                )
            }
        }
    }
}

@Composable
fun ForgeToolApprovalCard(
    toolName: String,
    commandOrDetails: String,
    onApproveOnce: () -> Unit,
    onApproveAlways: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    ForgeCard(
        modifier = modifier,
        style = ForgeCardStyle.WARNING_TINT,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PhosphorIcon(
                icon = PhosphorIconType.SHIELD_CHECK,
                size = 18.dp,
                tint = colors.warning,
            )
            Text(
                text = "Tool Permission Request",
                style = typography.titleSmall,
                color = colors.warning,
            )
        }

        Text(
            text = "Agent requested permission to execute: $toolName",
            style = typography.bodyMedium,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 6.dp),
        )

        ForgeCard(
            modifier = Modifier.padding(vertical = 8.dp),
            style = ForgeCardStyle.OUTLINED,
            contentPadding = 8.dp,
        ) {
            Text(
                text = commandOrDetails,
                style = typography.codeSmall,
                color = colors.accent,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ForgeButton(
                text = "Allow Once",
                onClick = onApproveOnce,
                style = ForgeButtonStyle.PRIMARY,
                modifier = Modifier.weight(1f),
            )
            ForgeButton(
                text = "Always",
                onClick = onApproveAlways,
                style = ForgeButtonStyle.SECONDARY,
                modifier = Modifier.weight(1f),
            )
            ForgeButton(
                text = "Deny",
                onClick = onReject,
                style = ForgeButtonStyle.OUTLINE,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun ForgeCheckpointCard(
    changedFileCount: Int,
    summary: String,
    onAccept: () -> Unit,
    onRevert: () -> Unit,
    onReviewDiff: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography

    ForgeCard(
        modifier = modifier,
        style = ForgeCardStyle.DEFAULT,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhosphorIcon(
                    icon = PhosphorIconType.GIT_COMMIT,
                    size = 18.dp,
                    tint = colors.primary,
                )
                Text(
                    text = "Checkpoint: $changedFileCount files changed",
                    style = typography.titleSmall,
                    color = colors.textPrimary,
                )
            }
            ForgeChip(
                text = "Review",
                icon = PhosphorIconType.MAGNIFYING_GLASS,
                onClick = onReviewDiff,
                tone = ForgeChipTone.PRIMARY,
            )
        }

        Text(
            text = summary,
            style = typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ForgeButton(
                text = "Accept Changes",
                onClick = onAccept,
                icon = PhosphorIconType.CHECK_CIRCLE,
                style = ForgeButtonStyle.PRIMARY,
                modifier = Modifier.weight(1f),
            )
            ForgeButton(
                text = "Revert",
                onClick = onRevert,
                icon = PhosphorIconType.ARROW_CLOCKWISE,
                style = ForgeButtonStyle.OUTLINE,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
