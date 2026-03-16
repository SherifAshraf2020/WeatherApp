package com.example.weatherapp.presentation.alerts

data class AlertsUiState(
    val startHour: Int = 12,
    val startMinute: Int = 0,
    val endHour: Int = 13,
    val endMinute: Int = 0,
    val selectedType: String = "NOTIFICATION", // "NOTIFICATION" or "ALARM"
    val isLoading: Boolean = false
)