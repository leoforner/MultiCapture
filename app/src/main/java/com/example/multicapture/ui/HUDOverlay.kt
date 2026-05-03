package com.example.multicapture.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.multicapture.ui.components.GlassPanel
import com.example.multicapture.ui.theme.GreenAccent
import com.example.multicapture.ui.theme.RedAccent
import com.example.multicapture.ui.theme.TextWhite

@Composable
fun HUDOverlay(
    modifier: Modifier = Modifier,
    batteryLevel: Int,
    isStreamHealthy: Boolean,
    bitrateMbps: Float,
    sessionTime: String
) {
    GlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Stream Health & Bitrate
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CellTower,
                    contentDescription = "Stream Health",
                    tint = if (isStreamHealthy) GreenAccent else RedAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("%.1f Mbps", bitrateMbps),
                    color = TextWhite,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Session Time
            Text(
                text = sessionTime,
                color = TextWhite,
                style = MaterialTheme.typography.titleMedium
            )

            // Battery
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$batteryLevel%",
                    color = TextWhite,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                val batteryIcon = if (batteryLevel > 15) Icons.Default.BatteryFull else Icons.Default.BatteryAlert
                val batteryColor = if (batteryLevel > 15) TextWhite else RedAccent
                Icon(
                    imageVector = batteryIcon,
                    contentDescription = "Battery",
                    tint = batteryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
