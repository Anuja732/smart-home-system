package com.example.miniproject.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.SwitchState

@Composable
fun MultiSwitchCard(
    device: Device,
    onSwitchToggle: (List<SwitchState>) -> Unit
) {
    val switches = device.switches.orEmpty()
    val activeCount = switches.count { it.isOn }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$activeCount / ${switches.size} active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (activeCount > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color(0xFF9AA0A6)
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (switches.isEmpty()) {
                Text(
                    text = "No sub-switches configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                switches.forEachIndexed { index, switchItem ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        tonalElevation = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = switchItem.label.orEmpty().ifEmpty { "Switch ${index + 1}" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Switch(
                                checked = switchItem.isOn,
                                onCheckedChange = { isChecked ->
                                    val updatedList = switches.mapIndexed { listIndex, state ->
                                        if (listIndex == index) state.copy(isOn = isChecked) else state
                                    }
                                    onSwitchToggle(updatedList)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}