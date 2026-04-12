package com.rootssecure.sentinel.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape tokens for the Digital Panopticon design system.
 *
 * Design principle: Corners should be "sharp enough to feel engineered."
 * - No pill-shaped buttons (those are consumer apps).
 * - Small radius on cards (tactical, precise).
 * - Zero radius on full-screen alert overlays (urgent, no softening).
 */
val SentinelShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // badges, chips
    small      = RoundedCornerShape(8.dp),   // input fields, small buttons
    medium     = RoundedCornerShape(12.dp),  // cards, modal sheets
    large      = RoundedCornerShape(16.dp),  // bottom sheets, large panels
    extraLarge = RoundedCornerShape(0.dp),   // full-screen critical alert overlays
)
