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
fun OutletCard(
    device: Device,
    onToggle: (DeviceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Status: ${device.status}", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = device.status == DeviceStatus.ON,
                onCheckedChange = { isChecked ->
                    onToggle(if (isChecked) DeviceStatus.ON else DeviceStatus.OFF)
                }
            )
        }
    }
}