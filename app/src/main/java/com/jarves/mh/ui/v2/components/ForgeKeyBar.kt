package com.jarves.mh.ui.v2.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun ForgeKeyBar(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
    keys: List<String> = listOf("ESC", "TAB", "CTRL", "ALT", "/", "-", "|", "\$", "~", ":", "\"", "{", "}", "[", "]"),
) {
    val colors = ForgeTheme.colors
    val shapes = ForgeTheme.shapes
    val typography = ForgeTheme.typography

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surfaceElevated,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            keys.forEach { key ->
                Surface(
                    onClick = { onKeyPress(key) },
                    shape = shapes.chip,
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                ) {
                    Text(
                        text = key,
                        style = typography.codeSmall,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}
