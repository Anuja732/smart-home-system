package com.example.miniproject.data.model

data class TelemetryEvent(
    val id: String = "",
    val deviceId: String = "",
    val eventType: String = "",
    val timestamp: Long = 0L,
    val message: String = ""
)
