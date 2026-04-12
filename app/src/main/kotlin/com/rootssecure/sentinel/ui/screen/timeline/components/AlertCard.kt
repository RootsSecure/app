package com.rootssecure.sentinel.ui.screen.timeline.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlagCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rootssecure.sentinel.domain.model.AlertEvent
import com.rootssecure.sentinel.domain.model.AlertSeverity
import com.rootssecure.sentinel.ui.common.GlassCard
import com.rootssecure.sentinel.ui.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")
    .withZone(ZoneId.systemDefault())

@Composable
fun AlertCard(
    alert: AlertEvent,
    onClick: () -> Unit,
    onFlagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // ── Visual Evidence Image ─────────────────────────────────────────
            if (alert.imageUrl.isNotBlank()) {
                AsyncImage(
                    model             = alert.imageUrl,
                    contentDescription = "Visual evidence: ${alert.title}",
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(SentinelShapes.medium)
                )
            }

            // ── Card Body ─────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SeverityBadge(severity = alert.severity)
                        if (alert.isFlagged) {
                            Text(
                                text  = "False Alarm",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text  = alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = OnBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text  = alert.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text  = formatter.format(alert.occurredAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }

                // Flag button
                IconButton(onClick = onFlagClick) {
                    Icon(
                        imageVector        = Icons.Outlined.FlagCircle,
                        contentDescription = "Flag as false alarm",
                        tint               = if (alert.isFlagged) HighAmber else OnSurfaceVariant
                    )
                }
            }
        }
    }
}
