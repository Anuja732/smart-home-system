package com.example.miniproject.ui.devices

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus

@Composable
fun ScheduledDeviceCard(
    device: Device,
    onToggle: (DeviceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.status == DeviceStatus.ON)
                MaterialTheme.colorScheme.errorContainer
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
                    Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Safety Limit: ${device.maxOnDurationMins ?: 0} mins",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = {
                        onToggle(if (device.status == DeviceStatus.ON) DeviceStatus.OFF else DeviceStatus.ON)
                    }
                ) {
                    Text(if (device.status == DeviceStatus.ON) "TURN OFF" else "TURN ON")
                }
            }
        }
    }
}