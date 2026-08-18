package com.example.miniproject.data.repository

import com.example.miniproject.data.model.Device
import com.example.miniproject.data.model.DeviceStatus
import com.example.miniproject.data.model.FloorPlan
import com.example.miniproject.data.model.SwitchState
import com.example.miniproject.data.model.TelemetryEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SmartHomeRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getDevicesByFloor(floorId: String): Flow<List<Device>> {
        return db.collection("devices")
            .whereEqualTo("floorId", floorId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    val device = document.toObject(Device::class.java) ?: return@mapNotNull null
                    val rawSwitches = document.get("switches") as? List<*> ?: emptyList<Any?>()
                    val normalizedSwitches = rawSwitches.mapNotNull { item ->
                        val map = item as? Map<*, *> ?: return@mapNotNull null
                        val id = (map["id"] as? Number)?.toInt() ?: 0
                        val label = map["label"] as? String ?: ""
                        val isOn = (map["on"] as? Boolean) ?: (map["isOn"] as? Boolean) ?: false
                        SwitchState(id = id, label = label, isOn = isOn)
                    }
                    device.copy(id = document.id, switches = normalizedSwitches)
                }
            }
    }

    fun getFloorPlans(): Flow<List<FloorPlan>> {
        return db.collection("floors")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        val createdAt = when (val value = document.get("createdAt")) {
                            is com.google.firebase.Timestamp -> value.toDate().time
                            is Long -> value
                            is Number -> value.toLong()
                            else -> System.currentTimeMillis()
                        }
                        document.toObject(FloorPlan::class.java)?.copy(
                            id = document.id,
                            createdAt = createdAt
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
    }

    suspend fun addFloorPlan(floorPlan: FloorPlan) {
        val id = floorPlan.id.ifBlank { UUID.randomUUID().toString() }
        val floor = floorPlan.copy(
            id = id,
            createdAt = floorPlan.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
        )

        db.collection("floors")
            .document(id)
            .set(floor)
            .await()
    }

    suspend fun addDevice(device: Device) {
        val id = device.id.ifBlank { UUID.randomUUID().toString() }
        db.collection("devices")
            .document(id)
            .set(device.copy(id = id))
            .await()
    }

    suspend fun toggleDeviceStatus(deviceId: String, newStatus: DeviceStatus) {
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus.name
        )
        
        if (newStatus == DeviceStatus.ON) {
            updates["turnedOnAt"] = System.currentTimeMillis()
        } else if (newStatus == DeviceStatus.OFF) {
            updates["turnedOnAt"] = com.google.firebase.firestore.FieldValue.delete()
        }

        db.collection("devices")
            .document(deviceId)
            .update(updates)
            .await()
    }

    suspend fun updateMultiSwitches(deviceId: String, updatedSwitches: List<SwitchState>) {
        val firestoreSwitches = updatedSwitches.map { switch ->
            mapOf(
                "id" to switch.id,
                "label" to switch.label,
                "on" to switch.isOn
            )
        }

        db.collection("devices")
            .document(deviceId)
            .update("switches", firestoreSwitches)
            .await()
    }

    fun getTelemetryEvents(): Flow<List<TelemetryEvent>> {
        return db.collection("telemetry")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        val timestamp = when (val value = document.get("timestamp")) {
                            is com.google.firebase.Timestamp -> value.toDate().time
                            is Long -> value
                            is Number -> value.toLong()
                            else -> 0L
                        }
                        document.toObject(TelemetryEvent::class.java)?.copy(
                            timestamp = timestamp
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
    }

    suspend fun logTelemetryEvent(deviceId: String, eventType: String, message: String) {
        val timestamp = System.currentTimeMillis()
        val customId = "${deviceId}_${eventType}_$timestamp"
        
        val event = TelemetryEvent(
            id = customId,
            deviceId = deviceId,
            eventType = eventType,
            timestamp = timestamp,
            message = message
        )

        db.collection("telemetry")
            .document(customId)
            .set(event)
            .await()
    }
}