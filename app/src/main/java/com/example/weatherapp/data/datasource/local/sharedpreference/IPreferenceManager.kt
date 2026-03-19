package com.example.weatherapp.data.datasource.local.sharedpreference

interface IPreferenceManager {
    fun isFirstRun(): Boolean
    fun setFirstRun(isFirstRun: Boolean)
    fun saveSettings(tempUnit: String, timeFormat: String, windUnit: String)
    fun getTempUnit(): String
    fun getTimeFormat(): String
    fun getWindUnit(): String
    fun savePressureUnit(unit: String)
    fun getPressureUnit(): String
    fun savePrecipitationUnit(unit: String)
    fun getPrecipitationUnit(): String
    fun saveLanguage(langCode: String)
    fun getLanguage(): String
    fun isNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(enabled: Boolean)
    fun isStatusBarEnabled(): Boolean
    fun setStatusBarEnabled(enabled: Boolean)
}