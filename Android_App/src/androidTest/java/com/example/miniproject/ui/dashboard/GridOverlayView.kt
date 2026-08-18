package com.example.miniproject.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.miniproject.data.model.Device

@Composable
fun GridOverlayView(
    floorImageUrl: String,
    devices: List<Device>,
    onDeviceClick: (Device) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = floorImageUrl,
            contentDescription = "Floor Plan Layout",
            modifier = Modifier.fillMaxSize()
        )

        devices.forEach { device ->
            val posX = device.position.x.toString().toDoubleOrNull()?.toInt() ?: 0
            val posY = device.position.y.toString().toDoubleOrNull()?.toInt() ?: 0
            Box(
                modifier = Modifier.offset(
                    x = (posX * 40).dp,
                    y = (posY * 40).dp
                )
            ) {
                Button(
                    onClick = { onDeviceClick(device) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (device.status.name == "ON")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("${device.name}\n[${device.status}]")
                }
            }
        }
    }
}