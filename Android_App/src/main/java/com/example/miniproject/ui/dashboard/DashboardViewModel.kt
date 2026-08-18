package com.example.miniproject.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus
import com.example.miniproject.data.model.FloorPlan
import com.example.miniproject.data.model.SwitchState
import com.example.miniproject.data.repository.SmartHomeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class DashboardViewModel(
    private val repository: SmartHomeRepository = SmartHomeRepository()
) : ViewModel() {

    private val selectedFloorId = MutableStateFlow("")

    val selectedFloor: StateFlow<String> = selectedFloorId.asStateFlow()

    val floorPlans: StateFlow<List<FloorPlan>> = repository.getFloorPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val devices: StateFlow<List<Device>> = selectedFloorId
        .flatMapLatest { floorId ->
            if (floorId.isBlank()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.getDevicesByFloor(floorId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectFloorPlan(floorId: String) {
        selectedFloorId.value = floorId
    }

    fun createFloorPlan(id: String, title: String, description: String, imageUrl: String?) {
        val cleanTitle = title.trim()
        val cleanId = id.trim().ifBlank { 
            cleanTitle.lowercase().replace(" ", "-") 
        }.ifBlank { UUID.randomUUID().toString() }

        viewModelScope.launch {
            val floorPlan = FloorPlan(
                id = cleanId,
                title = cleanTitle,
                description = description.trim(),
                imageUrl = imageUrl?.trim().takeUnless { it.isNullOrBlank() },
                rows = 8,
                cols = 10
            )
            repository.addFloorPlan(floorPlan)
            selectedFloorId.value = cleanId
        }
    }

    fun createDevice(device: Device) {
        if (device.name.isBlank() || device.floorId.isBlank()) return
        
        val cleanId = device.id.trim().ifBlank {
            device.name.trim().lowercase().replace(" ", "-")
        }.ifBlank { UUID.randomUUID().toString() }

        viewModelScope.launch {
            repository.addDevice(device.copy(id = cleanId))
        }
    }

    fun toggleDevice(deviceId: String, newStatus: DeviceStatus) {
        viewModelScope.launch {
            if (deviceId.isNotBlank()) {
                repository.toggleDeviceStatus(deviceId, newStatus)
                repository.logTelemetryEvent(
                    deviceId = deviceId,
                    eventType = if (newStatus == DeviceStatus.ON) "DEVICE_ON" else "DEVICE_OFF",
                    message = "Device is now ${newStatus.name}"
                )
            }
        }
    }

    fun toggleSubSwitches(deviceId: String, updatedSwitches: List<SwitchState>) {
        viewModelScope.launch {
            repository.updateMultiSwitches(deviceId, updatedSwitches)
            val onCount = updatedSwitches.count { it.isOn }
            repository.logTelemetryEvent(
                deviceId = deviceId,
                eventType = "SWITCH_UPDATE",
                message = "$onCount switches are active"
            )
        }
    }
}