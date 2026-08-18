package com.example.miniproject.ui.dashboard

import androidx.compose.ui.graphics.Color
import com.example.miniproject.data.model.FloorPlan

data class WallSegment(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float
)

data class RoomLabel(
    val text: String,
    val x: Float,
    val y: Float
)

data class FloorPlanLayout(
    val id: String,
    val title: String,
    val description: String,
    val rows: Int,
    val cols: Int,
    val backgroundColor: Color,
    val wallColor: Color,
    val accentColor: Color,
    val walls: List<WallSegment>,
    val roomLabels: List<RoomLabel>,
    val imageUrl: String? = null
) {
    companion object {
        fun empty(): FloorPlanLayout = FloorPlanLayout(
            id = "",
            title = "New Floor",
            description = "Add a floor plan image and devices",
            rows = 8,
            cols = 10,
            backgroundColor = Color(0xFFF8FAFC),
            wallColor = Color(0xFF334155),
            accentColor = Color(0xFF38BDF8),
            walls = emptyList(),
            roomLabels = emptyList(),
            imageUrl = null
        )
    }
}

fun FloorPlan.toFloorLayout(): FloorPlanLayout {
    val defaultWalls = listOf(
        WallSegment(0.0f, 0.0f, 1.0f, 0.0f),
        WallSegment(1.0f, 0.0f, 1.0f, 1.0f),
        WallSegment(1.0f, 1.0f, 0.0f, 1.0f),
        WallSegment(0.0f, 1.0f, 0.0f, 0.0f),
        WallSegment(0.34f, 0.0f, 0.34f, 0.62f),
        WallSegment(0.34f, 0.62f, 0.72f, 0.62f),
        WallSegment(0.72f, 0.62f, 0.72f, 1.0f),
        WallSegment(0.0f, 0.42f, 0.34f, 0.42f)
    )

    val defaultLabels = listOf(
        RoomLabel("Main", 0.12f, 0.2f),
        RoomLabel("Dining", 0.48f, 0.18f),
        RoomLabel("Hall", 0.42f, 0.74f),
        RoomLabel("Utility", 0.76f, 0.76f)
    )

    return FloorPlanLayout(
        id = id.orEmpty().ifBlank { title.orEmpty() },
        title = title.orEmpty().ifBlank { "Untitled Floor" },
        description = description.orEmpty().ifBlank { "Custom floor plan" },
        rows = rows.coerceAtLeast(1),
        cols = cols.coerceAtLeast(1),
        backgroundColor = Color(0xFFF8FAFC),
        wallColor = Color(0xFF334155),
        accentColor = Color(0xFF38BDF8),
        walls = defaultWalls,
        roomLabels = defaultLabels,
        imageUrl = imageUrl
    )
}
