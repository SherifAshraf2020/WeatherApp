package com.example.weatherapp.data.util

import android.content.Context
import android.os.Build
import android.view.View
import java.util.Locale

object LocaleHelper {
    fun applyLocale(context: Context, langCode: String) {
        val locale = if (langCode == "ar") {
            Locale.forLanguageTag("ar-EG")
        } else {
            Locale.ENGLISH
        }

        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Update layout direction for the current activity window if context is an Activity
        if (context is android.app.Activity) {
            val direction = if (langCode == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            context.window.decorView.layoutDirection = direction
        }
    }

    fun formatNumber(value: Any?): String {
        return if (value == null) "" else String.format(Locale.getDefault(), "%s", value)
    }

    fun formatTemp(value: Double): String {
        return String.format(Locale.getDefault(), "%.0f", value)
    }
}

fun Any?.localize(): String = LocaleHelper.formatNumber(this)
fun Double.localizeTemp(): String = LocaleHelper.formatTemp(this)
