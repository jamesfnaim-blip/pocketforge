package com.jarves.mh.ui.v2.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.theme.ForgeTheme

enum class ForgeButtonStyle {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    GHOST,
    DANGER,
}

@Composable
fun ForgeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ForgeButtonStyle = ForgeButtonStyle.PRIMARY,
    icon: PhosphorIconType? = null,
    trailingIcon: PhosphorIconType? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    minHeight: Dp = 44.dp,
) {
    val colors = ForgeTheme.colors
    val shapes = ForgeTheme.shapes
    val typography = ForgeTheme.typography

    when (style) {
        ForgeButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = minHeight),
                enabled = enabled && !loading,
                shape = shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.White,
                    disabledContainerColor = colors.primary.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f),
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(text, icon, trailingIcon, loading, Color.White, typography.labelLarge)
            }
        }
        ForgeButtonStyle.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = minHeight),
                enabled = enabled && !loading,
                shape = shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surfaceElevated,
                    contentColor = colors.textPrimary,
                    disabledContainerColor = colors.surfaceElevated.copy(alpha = 0.4f),
                    disabledContentColor = colors.textMuted,
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(text, icon, trailingIcon, loading, colors.textPrimary, typography.labelLarge)
            }
        }
        ForgeButtonStyle.OUTLINE -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = minHeight),
                enabled = enabled && !loading,
                shape = shapes.button,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textPrimary,
                    disabledContentColor = colors.textMuted,
                ),
                border = BorderStroke(1.dp, if (enabled) colors.border else colors.border.copy(alpha = 0.4f)),
                contentPadding = contentPadding,
            ) {
                ButtonContent(text, icon, trailingIcon, loading, colors.textPrimary, typography.labelLarge)
            }
        }
        ForgeButtonStyle.GHOST -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = minHeight),
                enabled = enabled && !loading,
                shape = shapes.button,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.textSecondary,
                    disabledContentColor = colors.textMuted,
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(text, icon, trailingIcon, loading, colors.textSecondary, typography.labelLarge)
            }
        }
        ForgeButtonStyle.DANGER -> {
            Button(
                onClick = onClick,
                modifier = modifier.defaultMinSize(minHeight = minHeight),
                enabled = enabled && !loading,
                shape = shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error,
                    contentColor = Color.White,
                    disabledContainerColor = colors.error.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f),
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(text, icon, trailingIcon, loading, Color.White, typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: PhosphorIconType?,
    trailingIcon: PhosphorIconType?,
    loading: Boolean,
    tint: Color,
    style: androidx.compose.ui.text.TextStyle,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = tint,
            )
        } else if (icon != null) {
            PhosphorIcon(icon = icon, size = 18.dp, tint = tint)
        }
        Text(text = text, style = style)
        if (trailingIcon != null && !loading) {
            PhosphorIcon(icon = trailingIcon, size = 16.dp, tint = tint)
        }
    }
}
