package com.rootssecure.sentinel.ui.screen.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rootssecure.sentinel.domain.model.AlertSeverity
import com.rootssecure.sentinel.ui.theme.*

@Composable
fun SeverityBadge(severity: AlertSeverity, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (severity) {
        is AlertSeverity.Critical -> Triple(CriticalContainer, CriticalRed, "CRITICAL")
        is AlertSeverity.High     -> Triple(HighContainer,     HighAmber,   "HIGH")
    }

    Text(
        text     = label,
        style    = MaterialTheme.typography.labelSmall,
        color    = textColor,
        modifier = modifier
            .clip(SentinelShapes.extraSmall)
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
