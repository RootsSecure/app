package com.rootssecure.sentinel.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rootssecure.sentinel.ui.theme.CriticalRed
import com.rootssecure.sentinel.ui.theme.SafeGreen

/**
 * A simple animated or static dot indicator for online/offline status.
 */
@Composable
fun StatusDot(
    online: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (online) SafeGreen else CriticalRed

    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}
