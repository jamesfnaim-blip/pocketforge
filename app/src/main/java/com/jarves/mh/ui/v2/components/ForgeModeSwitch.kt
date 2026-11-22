package com.jarves.mh.ui.v2.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jarves.mh.ui.v2.theme.ForgeTheme

enum class ForgeAppMode {
    SIMPLE,
    PRO,
}

@Composable
fun ForgeModeSwitch(
    currentMode: ForgeAppMode,
    onModeChange: (ForgeAppMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ForgeTheme.colors
    val shapes = ForgeTheme.shapes
    val typography = ForgeTheme.typography

    Surface(
        modifier = modifier,
        shape = shapes.pill,
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModePillItem(
                title = "Simple",
                selected = currentMode == ForgeAppMode.SIMPLE,
                onClick = { onModeChange(ForgeAppMode.SIMPLE) },
            )
            ModePillItem(
                title = "Pro",
                selected = currentMode == ForgeAppMode.PRO,
                onClick = { onModeChange(ForgeAppMode.PRO) },
            )
        }
    }
}

@Composable
private fun ModePillItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = ForgeTheme.colors
    val shapes = ForgeTheme.shapes
    val typography = ForgeTheme.typography

    Box(
        modifier = Modifier
            .clip(shapes.pill)
            .background(if (selected) colors.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = typography.labelMedium,
            color = if (selected) Color.White else colors.textSecondary,
        )
    }
}
