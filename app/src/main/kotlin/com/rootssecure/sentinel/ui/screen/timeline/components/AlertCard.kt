package com.rootssecure.sentinel.ui.screen.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.FlagCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rootssecure.sentinel.domain.model.AlertEvent
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
            // Visual evidence removed per user request for a cleaner message-only list

            // ── Card Body ─────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Evidence Thumbnail
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(SentinelShapes.small)
                            .background(SurfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (alert.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model             = alert.imageUrl,
                                contentDescription = "Event Thumbnail",
                                contentScale      = ContentScale.Crop,
                                modifier          = Modifier.fillMaxSize(),
                                onState = { state ->
                                    if (state is coil3.compose.AsyncImagePainter.State.Error) {
                                        android.util.Log.e("AlertCard", "Image load failed for ${alert.imageUrl}", state.result.throwable)
                                    }
                                }
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = OnSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }

                    if (alert.mediaRefs.size > 1) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            shape = SentinelShapes.extraSmall,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "+${alert.mediaRefs.size - 1}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = OnSurface
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SeverityBadge removed per user request
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
