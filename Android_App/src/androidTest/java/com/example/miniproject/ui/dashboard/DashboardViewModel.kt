package com.example.miniproject.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus
import com.example.miniproject.data.model.SwitchState
import com.example.miniproject.data.repository.SmartHomeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: SmartHomeRepository = SmartHomeRepository()
) : ViewModel() {

    // Automatically streams device updates for the active floor from Firestore
    val devices: StateFlow<List<Device>> = repository.getDevicesByFloor("floor_1")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleDevice(deviceId: String, newStatus: DeviceStatus) {
        viewModelScope.launch {
            repository.toggleDeviceStatus(deviceId, newStatus)
        }
    }

    fun toggleSubSwitches(deviceId: String, updatedSwitches: List<SwitchState>) {
        viewModelScope.launch {
            repository.updateMultiSwitches(deviceId, updatedSwitches)
        }
    }
}