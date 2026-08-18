package com.example.miniproject.data.model

data class FloorPlan(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val rows: Int = 8,
    val cols: Int = 10,
    val createdAt: Long = System.currentTimeMillis()
)
