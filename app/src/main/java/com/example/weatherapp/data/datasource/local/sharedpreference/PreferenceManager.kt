package com.example.weatherapp.data.datasource.local.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import com.example.weatherapp.data.Constants
import java.util.Locale

class PreferenceManager(context: Context): IPreferenceManager {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_FIRST_RUN = "is_first_run"
        private const val KEY_TEMP_UNIT = "temp_unit"
        private const val KEY_TIME_FORMAT = "time_format"
        private const val KEY_WIND_UNIT = "wind_unit"
        private const val PREF_PRESSURE_UNIT = "pressure_unit"
        private const val PREF_PRECIP_UNIT = "precip_unit"
        private const val KEY_HOME_LAT = "home_lat"
        private const val KEY_HOME_LON = "home_lon"
    }

    override fun isFirstRun(): Boolean = sharedPreferences.getBoolean(KEY_IS_FIRST_RUN, true)


    override fun setFirstRun(isFirstRun: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_RUN, isFirstRun).apply()
    }

    override fun saveSettings(tempUnit: String, timeFormat: String, windUnit: String){
        sharedPreferences.edit().apply(){
            putString(KEY_TEMP_UNIT, tempUnit)
            putString(KEY_TIME_FORMAT, timeFormat)
            putString(KEY_WIND_UNIT, windUnit)
            apply()
        }
    }

    override fun getTempUnit(): String = sharedPreferences.getString(KEY_TEMP_UNIT, "C") ?: "C"
    override fun getTimeFormat(): String = sharedPreferences.getString(KEY_TIME_FORMAT, "24h") ?: "24h"
    override fun getWindUnit(): String = sharedPreferences.getString(KEY_WIND_UNIT, "m/s") ?: "m/s"

    override fun savePressureUnit(unit: String) {
        sharedPreferences.edit().putString(PREF_PRESSURE_UNIT, unit).apply()
    }

    override fun getPressureUnit(): String = sharedPreferences.getString(PREF_PRESSURE_UNIT, "hPa") ?: "hPa"

    override fun savePrecipitationUnit(unit: String) {
        sharedPreferences.edit().putString(PREF_PRECIP_UNIT, unit).apply()
    }

    override fun getPrecipitationUnit(): String = sharedPreferences.getString(PREF_PRECIP_UNIT, "mm") ?: "mm"

    override fun saveLanguage(langCode: String) {
        sharedPreferences.edit().putString("language_key", langCode).apply()
    }

    override fun getLanguage(): String {
        return sharedPreferences.getString("language_key", Locale.getDefault().language) ?: "en"
    }


    override fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    override fun isStatusBarEnabled(): Boolean {
        return sharedPreferences.getBoolean("status_bar_enabled", true)
    }

    override fun setStatusBarEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("status_bar_enabled", enabled).apply()
    }

    override fun saveHomeLocation(lat: Double, lon: Double) {
        sharedPreferences.edit().apply {
            putLong(KEY_HOME_LAT, java.lang.Double.doubleToRawLongBits(lat))
            putLong(KEY_HOME_LON, java.lang.Double.doubleToRawLongBits(lon))
            apply()
        }
    }

    override fun getHomeLatitude(): Double {
        return java.lang.Double.longBitsToDouble(sharedPreferences.getLong(KEY_HOME_LAT, java.lang.Double.doubleToRawLongBits(0.0)))
    }

    override fun getHomeLongitude(): Double {
        return java.lang.Double.longBitsToDouble(sharedPreferences.getLong(KEY_HOME_LON, java.lang.Double.doubleToRawLongBits(0.0)))
    }
}
