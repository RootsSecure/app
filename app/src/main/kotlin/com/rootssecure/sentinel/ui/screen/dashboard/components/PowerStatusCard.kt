package com.rootssecure.sentinel.ui.screen.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rootssecure.sentinel.domain.model.PowerStatus
import com.rootssecure.sentinel.ui.common.GlassCard
import com.rootssecure.sentinel.ui.theme.HighAmber
import com.rootssecure.sentinel.ui.theme.OnSurface
import com.rootssecure.sentinel.ui.theme.OnSurfaceVariant
import com.rootssecure.sentinel.ui.theme.SafeGreen
import com.rootssecure.sentinel.ui.theme.TealPrimary

@Composable
fun PowerStatusCard(powerStatus: PowerStatus, modifier: Modifier = Modifier) {
    val isBattery = powerStatus is PowerStatus.BatteryFallback
    val color     = if (isBattery) HighAmber else SafeGreen
    val icon      = if (isBattery) Icons.Filled.BatteryFull else Icons.Filled.ElectricBolt
    val label     = if (isBattery) "Battery Fallback" else "Mains Power Active"
    val subtitle  = if (isBattery) "Mains power disconnected" else "Battery Fallback: Charged"

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector  = icon,
                contentDescription = "Power Status",
                tint         = color,
                modifier     = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text  = "POWER STATUS",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface
                )
            }
        }
    }
}
