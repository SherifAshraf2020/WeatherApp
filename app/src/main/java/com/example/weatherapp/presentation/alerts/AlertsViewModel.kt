package com.example.weatherapp.presentation.alerts


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.weatherapp.R
import com.example.weatherapp.worker.WeatherWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    sealed class AlertsUiEvent {
        data class ShowToast(val messageId: Int) : AlertsUiEvent()
    }

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AlertsUiEvent>()
    val uiEvent: SharedFlow<AlertsUiEvent> = _uiEvent.asSharedFlow()

    fun onStartTimeChanged(h: Int, m: Int) {
        _uiState.update { it.copy(startHour = h, startMinute = m) }
    }

    fun onEndTimeChanged(h: Int, m: Int) {
        _uiState.update { it.copy(endHour = h, endMinute = m) }
    }

    fun onTypeChanged(type: String) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun scheduleAlert() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Basic validation: end time should be after start time or on a different day
            // For simplicity, we just calculate delay for start time here.
            val delay = calculateDelay(state.startHour, state.startMinute)

            if (delay < 0) {
                _uiEvent.emit(AlertsUiEvent.ShowToast(R.string.error_invalid_time))
                return@launch
            }

            val data = workDataOf(
                "ALERT_TYPE" to state.selectedType,
                "START_HOUR" to state.startHour,
                "START_MINUTE" to state.startMinute,
                "END_HOUR" to state.endHour,
                "END_MINUTE" to state.endMinute
            )

            val workRequest = OneTimeWorkRequestBuilder<WeatherWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("weather_alert_tag")
                .build()

            WorkManager.getInstance(getApplication()).enqueueUniqueWork(
                "weather_alert_work",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            _uiEvent.emit(AlertsUiEvent.ShowToast(R.string.success_scheduled))
        }
    }

    private fun calculateDelay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis - now
    }
}


class AlertsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
