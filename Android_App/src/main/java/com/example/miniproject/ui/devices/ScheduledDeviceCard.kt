package com.example.miniproject.ui.devices

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus
import kotlin.math.max

@Composable
fun ScheduledDeviceCard(
    device: Device,
    onToggle: (DeviceStatus) -> Unit
) {
    val safetyLimitMinutes = device.maxOnDurationMins ?: 0L
    val elapsedMinutes = if (device.status == DeviceStatus.ON && device.turnedOnAt != null) {
        val turnedOnAt = device.turnedOnAt ?: 0L
        ((System.currentTimeMillis() - turnedOnAt) / 60_000L).coerceAtLeast(0L)
    } else {
        0L
    }
    val hasTimedOut = device.status == DeviceStatus.ON &&
        device.maxOnDurationMins != null &&
        device.turnedOnAt != null &&
        (System.currentTimeMillis() - (device.turnedOnAt ?: 0L)) > (device.maxOnDurationMins ?: 0L) * 60_000L

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasTimedOut)
                MaterialTheme.colorScheme.errorContainer
            else if (device.status == DeviceStatus.ON)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = device.name.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Safety Limit: ${safetyLimitMinutes} mins",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if ((device.status ?: DeviceStatus.OFF) == DeviceStatus.ON) {
                        Text(
                            text = "Elapsed: ${elapsedMinutes} mins",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Button(
                    onClick = {
                        onToggle(if ((device.status ?: DeviceStatus.OFF) == DeviceStatus.ON) DeviceStatus.OFF else DeviceStatus.ON)
                    }
                ) {
                    Text(if ((device.status ?: DeviceStatus.OFF) == DeviceStatus.ON) "TURN OFF" else "TURN ON")
                }
            }

            if (hasTimedOut) {
                Spacer(modifier = Modifier.height(10.dp))
                AssistChip(
                    onClick = { },
                    label = {
                        Text("TIMEOUT WARNING: exceeded ${safetyLimitMinutes} min limit")
                    }
                )
            }
        }
    }
}