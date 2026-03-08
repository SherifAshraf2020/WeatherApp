package com.example.weatherapp.data.datasource.local

import android.content.Context
import android.content.SharedPreferences
import com.example.weatherapp.data.Constants

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_FIRST_RUN = "is_first_run"
        private const val KEY_TEMP_UNIT = "temp_unit"
        private const val KEY_TIME_FORMAT = "time_format"
        private const val KEY_WIND_UNIT = "wind_unit"
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
}
