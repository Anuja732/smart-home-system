package com.example.miniproject

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus
import com.example.miniproject.data.model.DeviceType
import com.example.miniproject.data.model.GridPosition
import com.example.miniproject.data.model.SwitchState
import com.example.miniproject.ui.analytics.AnalyticsScreen
import com.example.miniproject.ui.analytics.AnalyticsViewModel
import com.example.miniproject.ui.dashboard.DashboardViewModel
import com.example.miniproject.ui.dashboard.FloorPlanLayout
import com.example.miniproject.ui.dashboard.GridOverlayView
import com.example.miniproject.ui.dashboard.toFloorLayout
import com.example.miniproject.ui.devices.CameraCard
import com.example.miniproject.ui.devices.MultiSwitchCard
import com.example.miniproject.ui.devices.OutletCard
import com.example.miniproject.ui.devices.ScheduledDeviceCard

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val analyticsViewModel: AnalyticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
                var selectedScreen by remember { mutableStateOf("dashboard") }
                val devices by dashboardViewModel.devices.collectAsState()
                val telemetry by analyticsViewModel.telemetry.collectAsState()
                val selectedFloorId by dashboardViewModel.selectedFloor.collectAsState()
                val floorPlans by dashboardViewModel.floorPlans.collectAsState()

                val floorLayouts = floorPlans.map { it.toFloorLayout() }
                
                // Ensure we have a valid floor selected if floorPlans is not empty
                LaunchedEffect(floorPlans) {
                    if (selectedFloorId.isBlank() && floorPlans.isNotEmpty()) {
                        dashboardViewModel.selectFloorPlan(floorPlans.first().id)
                    }
                }

                val selectedFloorPlan = floorLayouts.firstOrNull { it.id == selectedFloorId }
                    ?: if (floorLayouts.isNotEmpty()) floorLayouts.first() else FloorPlanLayout.empty()
                val selectedFloorIndex = floorLayouts.indexOfFirst { it.id == selectedFloorPlan.id }.coerceAtLeast(0)
                val activeDevices = devices.count { (it.status?.name ?: "") == "ON" || it.switches.orEmpty().any { s -> s.isOn } }

                var showAddFloorDialog by remember { mutableStateOf(false) }
                var showAddDeviceDialog by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF111827), Color(0xFF0B1120))
                                )
                            )
                            .padding(8.dp)
                    ) {
                        Header(activeDevices = activeDevices)
                        Spacer(modifier = Modifier.height(6.dp))

                        TabRow(
                            selectedTabIndex = if (selectedScreen == "dashboard") 0 else 1,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Tab(
                                selected = selectedScreen == "dashboard",
                                onClick = { selectedScreen = "dashboard" },
                                text = { Text("Dashboard") }
                            )
                            Tab(
                                selected = selectedScreen == "analytics",
                                onClick = { selectedScreen = "analytics" },
                                text = { Text("Analytics") }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedScreen == "dashboard") {
                            DashboardContent(
                                devices = devices,
                                floorPlans = floorLayouts,
                                selectedFloorPlan = selectedFloorPlan,
                                selectedFloorIndex = selectedFloorIndex,
                                onSelectFloor = dashboardViewModel::selectFloorPlan,
                                onToggleOutlet = { id, status -> dashboardViewModel.toggleDevice(id, status) },
                                onToggleSwitches = { id, switches -> dashboardViewModel.toggleSubSwitches(id, switches) },
                                onAddFloor = { showAddFloorDialog = true },
                                onAddDevice = { showAddDeviceDialog = true }
                            )
                        } else {
                            AnalyticsScreen(
                                telemetry = telemetry,
                                devices = devices,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                if (showAddFloorDialog) {
                    AddFloorDialog(
                        onDismiss = { showAddFloorDialog = false },
                        onSave = { id, title, description, imageUrl ->
                            dashboardViewModel.createFloorPlan(id, title, description, imageUrl)
                            showAddFloorDialog = false
                        }
                    )
                }

                if (showAddDeviceDialog) {
                    AddDeviceDialog(
                        floorId = selectedFloorId.ifBlank { selectedFloorPlan.id },
                        onDismiss = { showAddDeviceDialog = false },
                        onSave = { device ->
                            dashboardViewModel.createDevice(device)
                            showAddDeviceDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(activeDevices: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SmartHomeLogo()
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "Smart Home",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Realtime Monitoring",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            shape = CircleShape
        ) {
            Text(
                text = "$activeDevices active",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SmartHomeLogo() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(34.dp)
    ) {
        Canvas(modifier = Modifier.padding(6.dp)) {
            val roofLeft = Offset(x = size.width * 0.2f, y = size.height * 0.45f)
            val roofTop = Offset(x = size.width * 0.5f, y = size.height * 0.2f)
            val roofRight = Offset(x = size.width * 0.8f, y = size.height * 0.45f)
            val lineColor = Color(0xFF7DD3FC)

            drawLine(lineColor, roofLeft, roofTop, strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(lineColor, roofTop, roofRight, strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(lineColor, Offset(size.width * 0.28f, size.height * 0.45f), Offset(size.width * 0.28f, size.height * 0.8f), strokeWidth = 3f)
            drawLine(lineColor, Offset(size.width * 0.72f, size.height * 0.45f), Offset(size.width * 0.72f, size.height * 0.8f), strokeWidth = 3f)
            drawLine(lineColor, Offset(size.width * 0.28f, size.height * 0.8f), Offset(size.width * 0.72f, size.height * 0.8f), strokeWidth = 3f)
            drawLine(lineColor, Offset(size.width * 0.46f, size.height * 0.62f), Offset(size.width * 0.46f, size.height * 0.8f), strokeWidth = 3f)
            drawLine(lineColor, Offset(size.width * 0.54f, size.height * 0.62f), Offset(size.width * 0.54f, size.height * 0.8f), strokeWidth = 3f)
        }
    }
}

@Composable
private fun DashboardContent(
    devices: List<Device>,
    floorPlans: List<FloorPlanLayout>,
    selectedFloorPlan: FloorPlanLayout,
    selectedFloorIndex: Int,
    onSelectFloor: (String) -> Unit,
    onToggleOutlet: (String, DeviceStatus) -> Unit,
    onToggleSwitches: (String, List<SwitchState>) -> Unit,
    onAddFloor: () -> Unit,
    onAddDevice: () -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapPanel(
                modifier = Modifier.weight(1.25f),
                floorPlans = floorPlans,
                selectedFloorIndex = selectedFloorIndex,
                selectedFloorPlan = selectedFloorPlan,
                onSelectFloor = onSelectFloor,
                devices = devices,
                compactHeader = false,
                fixedMapHeight = null,
                onAddFloor = onAddFloor
            )
            DeviceControlPanel(
                modifier = Modifier.weight(1f),
                devices = devices,
                onToggleOutlet = onToggleOutlet,
                onToggleSwitches = onToggleSwitches,
                onAddDevice = onAddDevice
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            MapPanel(
                modifier = Modifier.fillMaxWidth(),
                floorPlans = floorPlans,
                selectedFloorIndex = selectedFloorIndex,
                selectedFloorPlan = selectedFloorPlan,
                onSelectFloor = onSelectFloor,
                devices = devices,
                compactHeader = false,
                fixedMapHeight = 280.dp,
                onAddFloor = onAddFloor
            )
            Spacer(modifier = Modifier.height(4.dp))
            DeviceControlPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                devices = devices,
                onToggleOutlet = onToggleOutlet,
                onToggleSwitches = onToggleSwitches,
                onAddDevice = onAddDevice
            )
        }
    }
}

@Composable
private fun MapPanel(
    modifier: Modifier,
    floorPlans: List<FloorPlanLayout>,
    selectedFloorIndex: Int,
    selectedFloorPlan: FloorPlanLayout,
    onSelectFloor: (String) -> Unit,
    devices: List<Device>,
    compactHeader: Boolean,
    fixedMapHeight: Dp?,
    onAddFloor: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(if (compactHeader) 6.dp else 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (floorPlans.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedFloorIndex.coerceIn(0, (floorPlans.size - 1).coerceAtLeast(0)),
                        edgePadding = 0.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    ) {
                        floorPlans.forEachIndexed { index, floorPlan ->
                            Tab(
                                selected = index == selectedFloorIndex,
                                onClick = { onSelectFloor(floorPlan.id) },
                                text = { Text(floorPlan.title.orEmpty()) }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Button(onClick = onAddFloor, modifier = Modifier.padding(start = 8.dp)) {
                    Text("+ Add")
                }
            }

            Spacer(modifier = Modifier.height(if (compactHeader) 2.dp else 4.dp))
            if (!compactHeader) {
                Text(
                    text = selectedFloorPlan.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            val mapModifier = if (fixedMapHeight != null) {
                Modifier
                    .fillMaxWidth()
                    .height(fixedMapHeight)
            } else {
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp)
            }

            if (floorPlans.isEmpty()) {
                Box(
                    modifier = mapModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No floor plans yet. Add your first layout.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(modifier = mapModifier) {
                    GridOverlayView(
                        floorPlan = selectedFloorPlan,
                        devices = devices,
                        onDeviceClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceControlPanel(
    modifier: Modifier,
    devices: List<Device>,
    onToggleOutlet: (String, DeviceStatus) -> Unit,
    onToggleSwitches: (String, List<SwitchState>) -> Unit,
    onAddDevice: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Device Controls",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(onClick = onAddDevice) {
                    Text("+ Device")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No devices in this floor yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(devices) { device ->
                        when (device.type) {
                            DeviceType.OUTLET -> OutletCard(
                                device = device,
                                onToggle = { status -> onToggleOutlet(device.id, status) }
                            )
                            DeviceType.MULTI_SWITCH -> MultiSwitchCard(
                                device = device,
                                onSwitchToggle = { switches -> onToggleSwitches(device.id, switches) }
                            )
                            DeviceType.SCHEDULED_HAZARD -> ScheduledDeviceCard(
                                device = device,
                                onToggle = { status -> onToggleOutlet(device.id, status) }
                            )
                            DeviceType.CAMERA -> CameraCard(device = device)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFloorDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String?) -> Unit
) {
    var id by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Floor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("Floor ID (e.g. ground-floor)") },
                    singleLine = true,
                    placeholder = { Text(title.lowercase().replace(" ", "-")) }
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Floor name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Floor photo URL (optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(id, title, description, imageUrl.ifBlank { null })
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddDeviceDialog(
    floorId: String,
    onDismiss: () -> Unit,
    onSave: (Device) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DeviceType.OUTLET) }
    var x by remember { mutableStateOf("3") }
    var y by remember { mutableStateOf("4") }
    var cameraUrl by remember { mutableStateOf("") }
    var switchCount by remember { mutableStateOf("3") }
    var maxDuration by remember { mutableStateOf("60") }
    var expanded by remember { mutableStateOf(false) }
    var switchLabelsState by remember { mutableStateOf(listOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Device") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text("Device ID (e.g. kitchen-light)") },
                    singleLine = true,
                    placeholder = { Text(name.lowercase().replace(" ", "-")) }
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Device name") },
                    singleLine = true
                )
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedType.name)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DeviceType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = x,
                        onValueChange = { x = it },
                        label = { Text("X") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = y,
                        onValueChange = { y = it },
                        label = { Text("Y") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                if (selectedType == DeviceType.MULTI_SWITCH) {
                    var count by remember { mutableStateOf(switchCount.toIntOrNull() ?: 1) }
                    val labels = remember { mutableStateListOf<String>().apply { 
                        repeat(count) { add("Switch ${it + 1}") } 
                    } }

                    OutlinedTextField(
                        value = switchCount,
                        onValueChange = { 
                            switchCount = it
                            val newCount = it.toIntOrNull() ?: 1
                            if (newCount > labels.size) {
                                repeat(newCount - labels.size) { labels.add("Switch ${labels.size + 1}") }
                            } else if (newCount < labels.size && newCount >= 0) {
                                repeat(labels.size - newCount) { labels.removeAt(labels.size - 1) }
                            }
                            count = newCount
                        },
                        label = { Text("Number of switches") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Dynamic Switch Labels
                    Text("Switch Labels:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        labels.forEachIndexed { index, label ->
                            OutlinedTextField(
                                value = label,
                                onValueChange = { labels[index] = it },
                                label = { Text("Label ${index + 1}") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Update the onSave logic below to use these labels
                    LaunchedEffect(labels.toList()) {
                        switchLabelsState = labels.toList()
                    }
                }
                if (selectedType == DeviceType.SCHEDULED_HAZARD) {
                    OutlinedTextField(
                        value = maxDuration,
                        onValueChange = { maxDuration = it },
                        label = { Text("Max active duration (mins)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                if (selectedType == DeviceType.CAMERA) {
                    OutlinedTextField(
                        value = cameraUrl,
                        onValueChange = { cameraUrl = it },
                        label = { Text("Camera URL") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newDevice = Device(
                        id = deviceId,
                        floorId = floorId,
                        name = name.trim(),
                        type = selectedType,
                        status = DeviceStatus.OFF,
                        position = GridPosition(
                            x = x.toLongOrNull() ?: 1L,
                            y = y.toLongOrNull() ?: 1L
                        ),
                        switches = if (selectedType == DeviceType.MULTI_SWITCH) {
                            switchLabelsState.mapIndexed { i, label -> 
                                SwitchState(i + 1, label.ifBlank { "Switch ${i + 1}" }, false) 
                            }
                        } else {
                            null
                        },
                        maxOnDurationMins = if (selectedType == DeviceType.SCHEDULED_HAZARD) {
                            maxDuration.toLongOrNull() ?: 60L
                        } else {
                            null
                        },
                        cameraUrl = cameraUrl.takeIf { selectedType == DeviceType.CAMERA && it.isNotBlank() }
                    )
                    if (newDevice.name.isNotBlank()) {
                        onSave(newDevice)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
