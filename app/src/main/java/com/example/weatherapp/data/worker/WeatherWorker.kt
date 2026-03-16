package com.example.weatherapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherapp.notification.NotificationHelper
import java.util.*

class WeatherWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val alertType = inputData.getString("ALERT_TYPE") ?: "NOTIFICATION"
        val isAlarm = alertType == "ALARM"
        
        val startHour = inputData.getInt("START_HOUR", -1)
        val startMinute = inputData.getInt("START_MINUTE", -1)
        val endHour = inputData.getInt("END_HOUR", -1)
        val endMinute = inputData.getInt("END_MINUTE", -1)

        if (!isCurrentTimeInRange(startHour, startMinute, endHour, endMinute)) {
            // If not in range, we could reschedule or just finish. 
            // Since it was scheduled with a delay to the start time, it should usually be in range.
            return Result.success()
        }

        val notificationHelper = NotificationHelper(applicationContext)

        return try {
            // In a real app, you'd fetch weather data here and check conditions
            notificationHelper.showNotification(
                title = if (isAlarm) "Weather Alarm!" else "Weather Update",
                message = "The weather conditions are being monitored within your specified time range.",
                isAlarm = isAlarm
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun isCurrentTimeInRange(sH: Int, sM: Int, eH: Int, eM: Int): Boolean {
        if (sH == -1 || eH == -1) return true // Default to true if not provided

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)

        val currentTime = currentHour * 60 + currentMinute
        val startTime = sH * 60 + sM
        val endTime = eH * 60 + eM

        return if (startTime <= endTime) {
            currentTime in startTime..endTime
        } else {
            // Range overlaps midnight (e.g., 22:00 to 02:00)
            currentTime >= startTime || currentTime <= endTime
        }
    }
}
