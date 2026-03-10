package com.example.weatherapp.data.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatLongDate(dateStr: String?): String {
        if (dateStr == null) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH)
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) { "" }
    }

    fun formatTime(dateStr: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val date = inputFormat.parse(dateStr)
        return outputFormat.format(date ?: Date())
    }

    fun getDayName(dateStr: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
        val date = inputFormat.parse(dateStr)
        return outputFormat.format(date ?: Date())
    }
}