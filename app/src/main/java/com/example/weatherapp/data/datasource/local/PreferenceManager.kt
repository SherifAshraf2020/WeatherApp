package com.example.weatherapp.data.datasource.local

import android.content.Context
import android.content.SharedPreferences
import com.example.weatherapp.data.Constants
import java.util.Locale

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_FIRST_RUN = "is_first_run"
        private const val KEY_TEMP_UNIT = "temp_unit"
        private const val KEY_TIME_FORMAT = "time_format"
        private const val KEY_WIND_UNIT = "wind_unit"
        private const val PREF_PRESSURE_UNIT = "pressure_unit"
        private const val PREF_PRECIP_UNIT = "precip_unit"
    }

    fun isFirstRun(): Boolean = sharedPreferences.getBoolean(KEY_IS_FIRST_RUN, true)


    fun setFirstRun(isFirstRun: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_RUN, isFirstRun).apply()
    }

    fun saveSettings(tempUnit: String, timeFormat: String, windUnit: String){
        sharedPreferences.edit().apply(){
            putString(KEY_TEMP_UNIT, tempUnit)
            putString(KEY_TIME_FORMAT, timeFormat)
            putString(KEY_WIND_UNIT, windUnit)
            apply()
        }
    }

    fun getTempUnit(): String = sharedPreferences.getString(KEY_TEMP_UNIT, "C") ?: "C"
    fun getTimeFormat(): String = sharedPreferences.getString(KEY_TIME_FORMAT, "24h") ?: "24h"
    fun getWindUnit(): String = sharedPreferences.getString(KEY_WIND_UNIT, "m/s") ?: "m/s"

    fun savePressureUnit(unit: String) {
        sharedPreferences.edit().putString(PREF_PRESSURE_UNIT, unit).apply()
    }

    fun getPressureUnit(): String = sharedPreferences.getString(PREF_PRESSURE_UNIT, "hPa") ?: "hPa"

    fun savePrecipitationUnit(unit: String) {
        sharedPreferences.edit().putString(PREF_PRECIP_UNIT, unit).apply()
    }

    fun getPrecipitationUnit(): String = sharedPreferences.getString(PREF_PRECIP_UNIT, "mm") ?: "mm"

    fun saveLanguage(langCode: String) {
        sharedPreferences.edit().putString("language_key", langCode).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString("language_key", Locale.getDefault().language) ?: "en"
    }
}
