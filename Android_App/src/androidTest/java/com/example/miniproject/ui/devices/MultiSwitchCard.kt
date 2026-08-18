package com.example.miniproject.ui.devices

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.SwitchState

@Composable
fun MultiSwitchCard(
    device: Device,
    onSwitchToggle: (List<SwitchState>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = device.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            device.switches?.forEach { switchItem ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = switchItem.label.ifEmpty { "Switch ${switchItem.id}" })
                    Switch(
                        checked = switchItem.isOn,
                        onCheckedChange = { isChecked ->
                            val updatedList = device.switches.map { s ->
                                if (s.id == switchItem.id) s.copy(isOn = isChecked) else s
                            }
                            onSwitchToggle(updatedList)
                        }
                    )
                }
            }
        }
    }
}