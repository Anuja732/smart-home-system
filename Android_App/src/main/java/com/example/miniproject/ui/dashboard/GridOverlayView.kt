package com.example.miniproject.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus
import com.example.miniproject.data.model.DeviceType
import com.example.miniproject.data.model.GridPosition
import com.example.miniproject.data.model.SwitchState
import kotlin.math.roundToInt

@Composable
fun GridOverlayView(
    floorPlan: FloorPlanLayout,
    devices: List<Device>,
    onDeviceClick: (Device) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cellWidth = maxWidth / floorPlan.cols
        val cellHeight = maxHeight / floorPlan.rows

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = floorPlan.backgroundColor,
            shape = RoundedCornerShape(22.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!floorPlan.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = floorPlan.imageUrl,
                        contentDescription = "Floor plan background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0x66000000))

                    val widthStep = size.width / floorPlan.cols
                    val heightStep = size.height / floorPlan.rows

                    for (column in 0..floorPlan.cols) {
                        val x = column * widthStep
                        drawLine(
                            color = Color(0x22000000),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                    }

                    for (row in 0..floorPlan.rows) {
                        val y = row * heightStep
                        drawLine(
                            color = Color(0x22000000),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }
                }

                devices.forEach { device ->
                    val xOffset = (device.position.x.toFloat() * cellWidth.value).roundToInt()
                    val yOffset = (device.position.y.toFloat() * cellHeight.value).roundToInt()

                    val markerColor = when {
                        device.type == DeviceType.MULTI_SWITCH && device.switches.orEmpty().count { it.isOn } > 0 ->
                            floorPlan.accentColor
                        device.type == DeviceType.MULTI_SWITCH ->
                            MaterialTheme.colorScheme.surfaceVariant
                        (device.status ?: DeviceStatus.OFF) == DeviceStatus.ON ->
                            floorPlan.accentColor
                        else ->
                            MaterialTheme.colorScheme.surfaceVariant
                    }

                    val markerLabel = when (device.type) {
                        DeviceType.MULTI_SWITCH -> {
                            val total = device.switches?.size ?: 0
                            val onCount = device.switches?.count { it.isOn } ?: 0
                            "$onCount/$total ON"
                        }
                        else -> (device.status ?: DeviceStatus.OFF).name
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = xOffset.dp, y = yOffset.dp)
                            .padding(4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = { onDeviceClick(device) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = markerColor,
                                    contentColor = if ((device.status ?: DeviceStatus.OFF) == DeviceStatus.ON || (device.type == DeviceType.MULTI_SWITCH && device.switches.orEmpty().any { it.isOn })) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.border(
                                    width = 1.dp,
                                    color = if ((device.status ?: DeviceStatus.OFF) == DeviceStatus.ON || (device.type == DeviceType.MULTI_SWITCH && device.switches.orEmpty().any { it.isOn })) {
                                        Color.White.copy(alpha = 0.6f)
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            ) {
                                Text(
                                    text = if (device.type == DeviceType.MULTI_SWITCH) markerLabel else device.name.orEmpty(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Text(
                                text = device.name.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun GridOverlayViewPreview() {
    val sampleFloorPlan = FloorPlanLayout(
        id = "preview_floor",
        title = "Preview Floor",
        description = "Sample floor plan for preview",
        rows = 8,
        cols = 10,
        backgroundColor = Color(0xFF0F172A),
        wallColor = Color(0xFF334155),
        accentColor = Color(0xFF7DD3FC),
        walls = listOf(
            WallSegment(0.0f, 0.0f, 1.0f, 0.0f),
            WallSegment(1.0f, 0.0f, 1.0f, 1.0f),
            WallSegment(1.0f, 1.0f, 0.0f, 1.0f),
            WallSegment(0.0f, 1.0f, 0.0f, 0.0f),
            WallSegment(0.34f, 0.0f, 0.34f, 0.62f),
            WallSegment(0.34f, 0.62f, 0.72f, 0.62f)
        ),
        roomLabels = listOf(
            RoomLabel("Living Room", 0.1f, 0.1f),
            RoomLabel("Kitchen", 0.5f, 0.2f)
        ),
        imageUrl = null
    )

    val sampleDevices = listOf(
        Device(
            id = "light-1",
            name = "Main Light",
            type = DeviceType.OUTLET,
            status = DeviceStatus.ON,
            position = GridPosition(2, 2)
        ),
        Device(
            id = "switch-1",
            name = "Dining Switch",
            type = DeviceType.MULTI_SWITCH,
            switches = listOf(
                SwitchState(1, "Light 1", true),
                SwitchState(2, "Light 2", false)
            ),
            position = GridPosition(5, 4)
        )
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF7DD3FC),
            secondary = Color(0xFF22C55E),
            background = Color(0xFF0F172A),
            surface = Color(0xFF111827),
            surfaceVariant = Color(0xFF1F2937),
            onPrimary = Color.Black,
            onBackground = Color(0xFFE2E8F0),
            onSurface = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFFCBD5E1)
        )
    ) {
        GridOverlayView(
            floorPlan = sampleFloorPlan,
            devices = sampleDevices,
            onDeviceClick = {}
        )
    }
}
