package com.example.weatherapp.data.source

import com.example.weatherapp.data.datasource.local.sharedpreference.IPreferenceManager

class FakePreferenceManager : IPreferenceManager {

    var temp = "C"
    var time = "24h"
    var wind = "m/s"
    var lang = "en"
    var first = true
    var notify = true
    var status = true
    var pressure = "hPa"
    var precip = "mm"
    var homeLat = 0.0
    var homeLon = 0.0

    override fun isFirstRun(): Boolean = first

    override fun setFirstRun(isFirstRun: Boolean) {
        this.first = isFirstRun
    }

    override fun saveSettings(tempUnit: String, timeFormat: String, windUnit: String) {
        this.temp = tempUnit
        this.time = timeFormat
        this.wind = windUnit
    }

    override fun getTempUnit(): String = temp

    override fun getTimeFormat(): String = time

    override fun getWindUnit(): String = wind

    override fun savePressureUnit(unit: String) {
        this.pressure = unit
    }

    override fun getPressureUnit(): String = pressure

    override fun savePrecipitationUnit(unit: String) {
        this.precip = unit
    }

    override fun getPrecipitationUnit(): String = precip

    override fun saveLanguage(langCode: String) {
        this.lang = langCode
    }

    override fun getLanguage(): String = lang

    override fun isNotificationsEnabled(): Boolean = notify

    override fun setNotificationsEnabled(enabled: Boolean) {
        this.notify = enabled
    }

    override fun isStatusBarEnabled(): Boolean = status

    override fun setStatusBarEnabled(enabled: Boolean) {
        this.status = enabled
    }

    override fun saveHomeLocation(lat: Double, lon: Double) {
        this.homeLat = lat
        this.homeLon = lon
    }

    override fun getHomeLatitude(): Double = homeLat

    override fun getHomeLongitude(): Double = homeLon
}
