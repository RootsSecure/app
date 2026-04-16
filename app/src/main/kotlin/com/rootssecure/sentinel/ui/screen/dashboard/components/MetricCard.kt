package com.rootssecure.sentinel.ui.screen.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rootssecure.sentinel.ui.common.GlassCard
import com.rootssecure.sentinel.ui.theme.*

/** Generic telemetry metric card — reusable for latency, battery %, etc. */
@Composable
fun MetricCard(
    label: String,
    value: () -> String,
    unit: String,
    subtitle: String,
    isWarning: () -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    val warning = isWarning()
    val valueColor = if (warning) HighAmber else TealPrimary
    val valueStr = value()

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text  = valueStr,
                        style = SentinelTypography.displayMedium.copy(
                            fontSize = 40.sp,
                            color    = valueColor
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text     = unit,
                        style    = MaterialTheme.typography.titleSmall,
                        color    = valueColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text  = label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface
                )
            }
            if (warning) {
                Text(text = "⚠", fontSize = 28.sp)
            }
        }
    }
}
