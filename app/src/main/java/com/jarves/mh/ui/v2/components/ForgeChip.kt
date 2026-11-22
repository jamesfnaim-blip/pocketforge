package com.jarves.mh.ui.v2.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.theme.ForgeTheme

enum class ForgeChipTone {
    NEUTRAL,
    PRIMARY,
    ACCENT,
    SUCCESS,
    WARNING,
    ERROR,
}

@Composable
fun ForgeChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: PhosphorIconType? = null,
    selected: Boolean = false,
    tone: ForgeChipTone = ForgeChipTone.NEUTRAL,
    onClick: (() -> Unit)? = null,
) {
    val colors = ForgeTheme.colors
    val shapes = ForgeTheme.shapes
    val typography = ForgeTheme.typography

    val (bgColor, textColor, borderColor) = if (selected) {
        Triple(colors.primary, Color.White, colors.primary)
    } else {
        when (tone) {
            ForgeChipTone.NEUTRAL -> Triple(colors.surfaceElevated, colors.textPrimary, colors.border)
            ForgeChipTone.PRIMARY -> Triple(colors.primary.copy(alpha = 0.12f), colors.primary, colors.primary.copy(alpha = 0.3f))
            ForgeChipTone.ACCENT -> Triple(colors.accent.copy(alpha = 0.12f), colors.accent, colors.accent.copy(alpha = 0.3f))
            ForgeChipTone.SUCCESS -> Triple(colors.success.copy(alpha = 0.12f), colors.success, colors.success.copy(alpha = 0.3f))
            ForgeChipTone.WARNING -> Triple(colors.warning.copy(alpha = 0.12f), colors.warning, colors.warning.copy(alpha = 0.3f))
            ForgeChipTone.ERROR -> Triple(colors.error.copy(alpha = 0.12f), colors.error, colors.error.copy(alpha = 0.3f))
        }
    }

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier,
        shape = shapes.chip,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) {
                PhosphorIcon(icon = icon, size = 14.dp, tint = textColor)
            }
            Text(
                text = text,
                style = typography.labelMedium,
                color = textColor,
            )
        }
    }
}
