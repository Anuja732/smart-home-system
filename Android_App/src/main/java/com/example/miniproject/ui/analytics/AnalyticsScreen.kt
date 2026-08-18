package com.example.miniproject.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus
import com.example.miniproject.data.model.DeviceType
import com.example.miniproject.data.model.TelemetryEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    telemetry: List<TelemetryEvent>,
    devices: List<Device>,
    modifier: Modifier = Modifier
) {
    val totalEvents = telemetry.size
    val cutoffEvents = telemetry.count { it.eventType.orEmpty().contains("CUTOFF", ignoreCase = true) }
    val activeHazards = devices.count {
        it.type == DeviceType.SCHEDULED_HAZARD && (it.status ?: DeviceStatus.OFF) == DeviceStatus.ON
    }
    val warningCount = devices.count { device ->
        val maxDuration = device.maxOnDurationMins ?: return@count false
        val turnedOnAt = device.turnedOnAt ?: return@count false
        device.type == DeviceType.SCHEDULED_HAZARD &&
            (device.status ?: DeviceStatus.OFF) == DeviceStatus.ON &&
            System.currentTimeMillis() - turnedOnAt > maxDuration * 60_000L
    }

    val usageStats = telemetry.groupBy { it.deviceId }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticsStatCard(title = "Events", value = "$totalEvents", modifier = Modifier.weight(1f))
                AnalyticsStatCard(title = "Cutoffs", value = "$cutoffEvents", modifier = Modifier.weight(1f))
                AnalyticsStatCard(title = "Warnings", value = "$warningCount", modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Safety Overview",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (activeHazards > 0) {
                        Text(
                            text = "$activeHazards hazard devices currently active",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        devices.filter { it.type == DeviceType.SCHEDULED_HAZARD && (it.status ?: DeviceStatus.OFF) == DeviceStatus.ON }
                            .forEach { device ->
                                val elapsed = if (device.turnedOnAt != null) (System.currentTimeMillis() - device.turnedOnAt) / 60000L else 0L
                                Text(
                                    text = "• ${device.name}: ${elapsed}m active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (elapsed > (device.maxOnDurationMins ?: 60L)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                    } else {
                        Text(
                            text = "All safety-critical devices are OFF.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (usageStats.isNotEmpty()) {
            item {
                Text(
                    text = "Usage frequency",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        usageStats.forEach { (deviceId, count) ->
                            val deviceName = devices.find { it.id == deviceId }?.name ?: deviceId
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = deviceName, style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(
                                        progress = { count.toFloat() / (totalEvents.coerceAtLeast(1)) },
                                        modifier = Modifier.width(60.dp).height(6.dp).padding(end = 8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                    Text(text = "$count", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Recent activity log",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (telemetry.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No usage activity recorded yet.",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(telemetry) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val eventType = event.eventType.orEmpty()
                            val deviceName = devices.find { it.id == event.deviceId }?.name ?: "Unknown"
                            Text(
                                text = "$deviceName: $eventType",
                                fontWeight = FontWeight.Bold,
                                color = if (eventType.contains("CUTOFF", ignoreCase = true) || eventType.contains("OFF", ignoreCase = true)) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            Text(
                                text = formatTimestamp(event.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.message.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    val date = Date(epochMillis)
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(date)
}
