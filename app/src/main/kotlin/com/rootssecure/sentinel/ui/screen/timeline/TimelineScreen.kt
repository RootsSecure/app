package com.rootssecure.sentinel.ui.screen.timeline

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rootssecure.sentinel.ui.common.TopBar
import com.rootssecure.sentinel.ui.screen.timeline.components.AlertCard
import com.rootssecure.sentinel.ui.theme.Background
import com.rootssecure.sentinel.ui.theme.CriticalRed
import com.rootssecure.sentinel.ui.theme.OnBackground

@Composable
fun TimelineScreen(
    onAlertClick: (String) -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopBar(
            title = "Alert Timeline",
            actions = {
                androidx.compose.material3.IconButton(onClick = { viewModel.clearAllAlerts() }) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.DeleteSweep,
                        contentDescription = "Clear All Alerts",
                        tint = com.rootssecure.sentinel.ui.theme.CriticalRed
                    )
                }
            }
        )

        AnimatedContent(targetState = state, label = "timeline_state") { uiState ->
            when (uiState) {
                is TimelineUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "Awaiting alerts from Edge Node…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnBackground.copy(alpha = 0.5f)
                    )
                }

                is TimelineUiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CriticalRed
                    )
                }

                is TimelineUiState.Success -> {
                    if (uiState.alerts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = "No alerts. Your plot is secure.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnBackground.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier           = Modifier.fillMaxSize(),
                            contentPadding     = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Text(
                                        text  = "SITE MONITORAL STATUS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = com.rootssecure.sentinel.ui.theme.TealPrimary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text  = "Monitoring construction progress and security. Real-time detection of site personnel and heavy machinery.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnBackground.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            items(
                                items = uiState.alerts,
                                key   = { it.id }
                            ) { alert ->
                                AlertCard(
                                    alert        = alert,
                                    onClick      = { onAlertClick(alert.id) },
                                    onFlagClick  = { viewModel.flagAsFalseAlarm(alert.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
