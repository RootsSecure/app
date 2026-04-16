package com.rootssecure.sentinel.ui.screen.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rootssecure.sentinel.ui.common.GlassCard
import com.rootssecure.sentinel.ui.theme.CriticalRed
import com.rootssecure.sentinel.ui.theme.HighAmber
import com.rootssecure.sentinel.ui.theme.OnSurface
import com.rootssecure.sentinel.ui.theme.OnSurfaceVariant
import com.rootssecure.sentinel.ui.theme.SafeGreen
import com.rootssecure.sentinel.ui.theme.SentinelTypography
import com.rootssecure.sentinel.ui.theme.SentinelShapes

/**
 * Displays the current CPU temperature as a large number with a progress bar.
 * Color changes from green → amber → red as temperature rises toward 80°C.
 */
@Composable
fun CpuTempGauge(
    currentTemp: () -> Double,
    history: () -> List<Float>,   // for future sparkline implementation
    modifier: Modifier = Modifier
) {
    val temp = currentTemp()
    val fraction = (temp / 80.0).coerceIn(0.0, 1.0).toFloat()
    val color = when {
        temp < 60 -> SafeGreen
        temp < 72 -> HighAmber
        else             -> CriticalRed
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Column {
                Text(
                    text  = "${temp.toInt()}°C",
                    style = SentinelTypography.displayMedium.copy(
                        fontSize = 48.sp,
                        color    = color
                    )
                )
                Text(
                    text  = if (temp > 80) "⚠ CRITICAL TEMPERATURE" else "System Temperature: NORMAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text  = "CPU TEMPERATURE",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress          = { fraction },
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(SentinelShapes.extraSmall),
                color             = color,
                trackColor        = color.copy(alpha = 0.15f),
                strokeCap         = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text  = "Limit: 80°C",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}
