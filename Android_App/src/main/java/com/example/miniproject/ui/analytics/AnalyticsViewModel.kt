package com.example.miniproject.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.model.TelemetryEvent
import com.example.miniproject.data.repository.SmartHomeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AnalyticsViewModel(
    private val repository: SmartHomeRepository = SmartHomeRepository()
) : ViewModel() {

    val telemetry: StateFlow<List<TelemetryEvent>> = repository.getTelemetryEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
