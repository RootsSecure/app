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
        // Full-width evidence image
        if (alert.imageUrl.isNotBlank()) {
            AsyncImage(
                model             = alert.imageUrl,
                contentDescription = "1080p visual proof",
                contentScale      = ContentScale.Crop,
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(SentinelShapes.medium)
            )
        }

        SeverityBadge(severity = alert.severity)

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
