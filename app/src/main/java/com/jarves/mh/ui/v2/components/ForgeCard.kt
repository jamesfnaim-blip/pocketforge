package com.jarves.mh.ui.v2.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarves.mh.ui.v2.theme.ForgeTheme

enum class ForgeCardStyle {
    DEFAULT,
    ELEVATED,
    OUTLINED,
    ACCENT_LEFT,
    ACCENT_TINT,
    SUCCESS_TINT,
    WARNING_TINT,
    ERROR_TINT,
}

@Composable
fun ForgeCard(
    modifier: Modifier = Modifier,
    style: ForgeCardStyle = ForgeCardStyle.DEFAULT,
    shape: Shape = ForgeTheme.shapes.card,
    onClick: (() -> Unit)? = null,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = ForgeTheme.colors

    val (bgColor, borderColor) = when (style) {
        ForgeCardStyle.DEFAULT -> colors.surface to colors.border
        ForgeCardStyle.ELEVATED -> colors.surfaceElevated to colors.border
        ForgeCardStyle.OUTLINED -> colors.background to colors.border
        ForgeCardStyle.ACCENT_LEFT -> colors.surface to colors.border
        ForgeCardStyle.ACCENT_TINT -> colors.accent.copy(alpha = 0.08f) to colors.accent.copy(alpha = 0.25f)
        ForgeCardStyle.SUCCESS_TINT -> colors.success.copy(alpha = 0.08f) to colors.success.copy(alpha = 0.25f)
        ForgeCardStyle.WARNING_TINT -> colors.warning.copy(alpha = 0.08f) to colors.warning.copy(alpha = 0.25f)
        ForgeCardStyle.ERROR_TINT -> colors.error.copy(alpha = 0.08f) to colors.error.copy(alpha = 0.25f)
    }

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier.fillMaxWidth(),
        shape = shape,
        color = bgColor,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        if (style == ForgeCardStyle.ACCENT_LEFT) {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(start = 0.dp)
                        .border(
                            width = 3.dp,
                            color = colors.accent,
                            shape = shape,
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding),
                    content = content,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                content = content,
            )
        }
    }
}
