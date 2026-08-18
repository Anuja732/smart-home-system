package com.example.miniproject.data.model

enum class DeviceType { OUTLET, MULTI_SWITCH, SCHEDULED_HAZARD, CAMERA }
enum class DeviceStatus { ON, OFF, ERROR, DISCONNECTED }

data class SwitchState(
    val id: Int = 0,
    val label: String = "",
    val isOn: Boolean = false
)

data class GridPosition(
    val x: Any = 0,
    val y: Any = 0
)

data class Device(
    val id: String = "",
    val floorId: String = "",
    val name: String = "",
    val type: DeviceType = DeviceType.OUTLET,
    val status: DeviceStatus = DeviceStatus.OFF,
    val position: GridPosition = GridPosition(),
    val switches: List<SwitchState>? = null,
    val maxOnDurationMins: Long? = null,
    val turnedOnAt: Long? = null,
    val cameraUrl: String? = null
)