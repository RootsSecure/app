package com.rootssecure.sentinel.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rootssecure.sentinel.ui.theme.GlassBorder
import com.rootssecure.sentinel.ui.theme.SentinelShapes
import com.rootssecure.sentinel.ui.theme.SurfaceContainer

/**
 * A premium semi-transparent card with a subtle border.
 * Implements the "Glassmorphism" aesthetic for the Sentinel dashboard.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(SentinelShapes.medium)
            .background(SurfaceContainer)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassBorder,
                        GlassBorder.copy(alpha = 0.05f)
                    )
                ),
                shape = SentinelShapes.medium
            )
    ) {
        content()
    }
}
