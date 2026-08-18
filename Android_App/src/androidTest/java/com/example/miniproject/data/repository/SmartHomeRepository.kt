package com.example.miniproject.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus
import com.example.miniproject.data.model.SwitchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SmartHomeRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getDevicesByFloor(floorId: String): Flow<List<Device>> {
        return db.collection("devices")
            .whereEqualTo("floorId", floorId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Device::class.java) }
    }

    suspend fun toggleDeviceStatus(deviceId: String, newStatus: DeviceStatus) {
        db.collection("devices").document(deviceId)
            .update("status", newStatus.name)
    }

    suspend fun updateMultiSwitches(deviceId: String, updatedSwitches: List<SwitchState>) {
        db.collection("devices").document(deviceId)
            .update("switches", updatedSwitches)
    }
}