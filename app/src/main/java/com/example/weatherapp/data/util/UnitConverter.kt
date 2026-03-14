package com.example.weatherapp.data.util

import java.util.Locale

object UnitConverter {
    fun convertWindSpeed(value: Double, fromTempUnit: String, toWindUnit: String): String {
        // OpenWeatherMap: metric -> m/s, imperial -> mph
        val speedInMs = if (fromTempUnit == "C") value else value * 0.44704
        val result = when (toWindUnit) {
            "m/s" -> speedInMs
            "km/h" -> speedInMs * 3.6
            "mph" -> speedInMs * 2.23694
            "knots" -> speedInMs * 1.94384
            "ft/s" -> speedInMs * 3.28084
            else -> speedInMs
        }
        return String.format(Locale.US, "%.1f", result)
    }

    fun convertPressure(value: Int, toPressureUnit: String): String {
        // OpenWeatherMap always returns pressure in hPa
        val result = when (toPressureUnit) {
            "hPa", "mbar" -> value.toDouble()
            "mmHg" -> value * 0.75006
            "inHg" -> value * 0.02953
            else -> value.toDouble()
        }
        return if (toPressureUnit == "inHg") {
            String.format(Locale.US, "%.2f", result)
        } else {
            String.format(Locale.US, "%.1f", result)
        }
    }

    fun convertPrecipitation(value: Double, toPrecipitationUnit: String): String {
        // OpenWeatherMap returns precipitation in mm
        val result = when (toPrecipitationUnit) {
            "mm" -> value
            "in" -> value * 0.0393701
            else -> value
        }
        return String.format(Locale.US, "%.2f", result)
    }
}
