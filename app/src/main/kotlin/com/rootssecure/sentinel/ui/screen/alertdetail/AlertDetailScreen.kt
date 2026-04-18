package com.rootssecure.sentinel.ui.screen.alertdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.rootssecure.sentinel.domain.model.AlertEvent
import com.rootssecure.sentinel.ui.screen.timeline.components.SeverityBadge
import com.rootssecure.sentinel.ui.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val detailFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy · HH:mm:ss")
    .withZone(ZoneId.systemDefault())

@Composable
fun AlertDetailScreen(
    alertId: String,
    onNavigateBack: () -> Unit,
    viewModel: AlertDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ── Back button top bar ───────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = OnBackground
                )
            }
            Text(
                text  = "Incident Report",
                style = MaterialTheme.typography.titleMedium,
                color = OnBackground
            )
        }

        when (val s = state) {
            is AlertDetailUiState.Loading  -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
            is AlertDetailUiState.NotFound -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Alert not found", color = CriticalRed, style = MaterialTheme.typography.bodyLarge)
            }
            is AlertDetailUiState.Success  -> AlertDetailContent(s.alert)
        }
    }
}

@Composable
private fun AlertDetailContent(alert: AlertEvent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Evidence Image(s)
        if (alert.mediaRefs.isNotEmpty()) {
            if (alert.mediaRefs.size > 1) {
                val pagerState = androidx.compose.foundation.pager.rememberPagerState { alert.mediaRefs.size }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(SentinelShapes.medium)
                    ) { index ->
                        AsyncImage(
                            model = alert.mediaRefs[index],
                            contentDescription = "Visual evidence ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Pager Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(alert.mediaRefs.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) TealPrimary else OnSurfaceVariant.copy(alpha = 0.3f)
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(color)
                                    .size(6.dp)
                            )
                        }
                    }
                }
            } else {
                AsyncImage(
                    model = alert.imageUrl,
                    contentDescription = "Visual evidence",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(SentinelShapes.medium)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SeverityBadge(severity = alert.severity)
            if (alert.confidence > 0) {
                Surface(
                    color = SurfaceContainer,
                    shape = SentinelShapes.extraSmall
                ) {
                    Text(
                        text = "Confidence: ${(alert.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Text(
            text  = alert.title,
            style = MaterialTheme.typography.headlineMedium,
            color = OnBackground
        )

        Text(
            text  = alert.reason,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface
        )

        HorizontalDivider(color = GlassBorder)

        // Metadata table
        MetadataRow("Event ID",    alert.id)
        MetadataRow("Occurred At", detailFormatter.format(alert.occurredAt))
        MetadataRow("Burst Count", "${alert.burstCount} frames")
        MetadataRow("Status",      if (alert.isFlagged) "Flagged as False Alarm" else "Active")
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Text(text = value,  style = MaterialTheme.typography.bodySmall,   color = OnBackground)
    }
}
