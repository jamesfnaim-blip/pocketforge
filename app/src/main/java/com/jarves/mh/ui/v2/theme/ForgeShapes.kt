package com.jarves.mh.ui.v2.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class ForgeShapes(
    val card: Shape = RoundedCornerShape(12.dp),
    val cardSmall: Shape = RoundedCornerShape(8.dp),
    val button: Shape = RoundedCornerShape(10.dp),
    val input: Shape = RoundedCornerShape(10.dp),
    val chip: Shape = RoundedCornerShape(8.dp),
    val pill: Shape = RoundedCornerShape(999.dp),
    val modalSheet: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    val bubbleUser: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
    val bubbleAgent: Shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
)

val LocalForgeShapes = staticCompositionLocalOf { ForgeShapes() }
