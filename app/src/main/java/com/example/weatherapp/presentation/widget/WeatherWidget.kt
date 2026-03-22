package com.example.weatherapp.presentation.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.example.weatherapp.R
import com.example.weatherapp.data.db.WeatherDatabase
import com.example.weatherapp.data.util.localize
import com.example.weatherapp.data.util.localizeTemp
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherWidget : GlanceAppWidget() {

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = WeatherDatabase.getDatabase(context)
        val weatherData = database.homeWeatherDao().getHomeWeather().firstOrNull()

        val locale = Locale.getDefault()
        val sdf = SimpleDateFormat("EEEE, dd MMM", locale)
        val dateText = sdf.format(Date()).localize()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.weather_bg))
                    .cornerRadius(20.dp)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "📍 ${weatherData?.current?.name ?: "Skyfall"}",
                            style = TextStyle(
                                color = ColorProvider(R.color.white),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        val fullAddress = weatherData?.address ?: ""
                        if (fullAddress.isNotEmpty()) {
                            Text(
                                text = fullAddress,
                                style = TextStyle(
                                    color = ColorProvider(R.color.white_70),
                                    fontSize = 13.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = "🔄",
                        modifier = GlanceModifier
                            .clickable(actionRunCallback<RefreshActionCallback>()),
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = ColorProvider(R.color.white)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = dateText,
                            style = TextStyle(
                                color = ColorProvider(R.color.white_70),
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = weatherData?.current?.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                            style = TextStyle(
                                color = ColorProvider(R.color.white),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        val tempValue = weatherData?.current?.main?.temp
                        Text(
                            text = tempValue?.localizeTemp() ?: "--",
                            style = TextStyle(
                                color = ColorProvider(R.color.white),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )

                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = GlanceModifier.padding(start = 2.dp, top = 8.dp)
                        ) {
                            Text(
                                text = "°",
                                style = TextStyle(
                                    color = ColorProvider(R.color.white),
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = getWidgetEmoji(weatherData?.current?.weather?.firstOrNull()?.main),
                                style = TextStyle(fontSize = 28.sp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getWidgetEmoji(mainStatus: String?): String {
        return when (mainStatus?.lowercase()) {
            "clear" -> "☀️"
            "clouds" -> "☁️"
            "rain" -> "🌧️"
            "thunderstorm" -> "⛈️"
            "snow" -> "❄️"
            else -> "⛅"
        }
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WeatherWidget().update(context, glanceId)
    }
}
