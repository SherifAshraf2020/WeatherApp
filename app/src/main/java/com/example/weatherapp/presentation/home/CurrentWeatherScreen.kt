package com.example.weatherapp.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastItem
import com.example.weatherapp.data.models.home.FullWeatherData
import com.example.weatherapp.data.util.UnitConverter
import com.example.weatherapp.data.util.localize
import com.example.weatherapp.data.util.localizeTemp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CurrentWeatherScreen(
    data: FullWeatherData,
    unit: String,
    timeFormat: String,
    windUnit: String,
    pressureUnit: String,
    precipitationUnit: String,
    address: String = ""
) {
    val current = data.current
    val todayDate = LocalDate.now()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.weather_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
                MainWeatherHeader(current, LocalDateTime.now(), unit, timeFormat, address)
            }

            item {
                WeatherExtraDetails(
                    current = current,
                    tempUnit = unit,
                    windUnit = windUnit,
                    pressureUnit = pressureUnit,
                    precipitationUnit = precipitationUnit
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.hourly_forecast),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                HourlyForecastCard(data.forecast.list.take(8), timeFormat)
            }

            item {
                Text(
                    text = stringResource(id = R.string.next_5_days),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                val distinctForecast = data.forecast.list
                    .filter {
                        val datePart = it.dtTxt.split(" ")[0]
                        runCatching { LocalDate.parse(datePart) }.getOrNull()?.isAfter(todayDate) == true
                    }
                    .distinctBy { it.dtTxt.split(" ")[0] }
                    .take(5)

                DailyForecastCard(distinctForecast)
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainWeatherHeader(
    current: CurrentWeatherResponse,
    dateTime: LocalDateTime,
    unit: String,
    timeFormat: String,
    address: String = ""
) {
    val locale = Locale.getDefault()
    val is12h = timeFormat.contains("12")

    val timeValue = dateTime.format(DateTimeFormatter.ofPattern(if (is12h) "hh:mm" else "HH:mm", locale)).localize()
    val amPm = if (is12h) dateTime.format(DateTimeFormatter.ofPattern(" a", locale)) else ""

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "${current.name}, ${current.sys.country}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (address.isNotEmpty()) {
                    Text(
                        text = address,
                        color = Color.White.copy(0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = dateTime.format(DateTimeFormatter.ofPattern("EEEE, dd MMM", locale)).localize(),
                    color = Color.White.copy(0.7f),
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeValue,
                        color = Color.White,
                        fontSize = if (is12h) 44.sp else 50.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (is12h) {
                        Text(
                            text = amPm,
                            color = Color.White.copy(0.9f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = current.main.temp.localizeTemp(),
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.W100
                )
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                ) {
                    Text(text = "°$unit", color = Color.White, fontSize = 22.sp)
                    Icon(
                        imageVector = getWeatherIcon(current.weather.firstOrNull()?.main),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherExtraDetails(
    current: CurrentWeatherResponse,
    tempUnit: String,
    windUnit: String,
    pressureUnit: String,
    precipitationUnit: String
) {
    val convertedWind = UnitConverter.convertWindSpeed(current.wind.speed, tempUnit, windUnit)
    val convertedPressure = UnitConverter.convertPressure(current.main.pressure, pressureUnit)
    val rainValue = current.rain?.h1 ?: current.rain?.h3 ?: 0.0
    val convertedPrecipitation = UnitConverter.convertPrecipitation(rainValue, precipitationUnit)

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        color = Color.Black.copy(0.25f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailItem(stringResource(id = R.string.wind_label), "${convertedWind.localize()} $windUnit", Icons.Rounded.Air)
                DetailItem(stringResource(id = R.string.humidity_label), "${current.main.humidity.localize()}%", Icons.Rounded.WaterDrop)
                DetailItem(stringResource(id = R.string.pressure_label), "${convertedPressure.localize()} $pressureUnit", Icons.Rounded.Compress)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DetailItem(stringResource(id = R.string.precip_label), "${convertedPrecipitation.localize()} $precipitationUnit", Icons.Rounded.InvertColors)
                DetailItem(stringResource(id = R.string.clouds_label), "${current.clouds.all.localize()}%", Icons.Rounded.Cloud)
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, color = Color.White.copy(0.5f), fontSize = 10.sp)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HourlyForecastCard(list: List<ForecastItem>, timeFormat: String) {
    val locale = Locale.getDefault()
    val is12h = timeFormat.contains("12")
    val timePattern = if (is12h) "h a" else "HH:mm"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(0.3f),
        shape = RoundedCornerShape(20.dp)
    ) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(list) { hour ->
                val timeResult = runCatching {
                    LocalDateTime.parse(hour.dtTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                }.getOrNull()

                if (timeResult != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(timeResult.format(DateTimeFormatter.ofPattern(timePattern, locale)).localize(), color = Color.White, fontSize = 12.sp)
                        Icon(getWeatherIcon(hour.weather.firstOrNull()?.main), null, tint = Color.White, modifier = Modifier.size(26.dp))
                        Text("${hour.main.temp.localizeTemp()}°", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailyForecastCard(list: List<ForecastItem>) {
    val locale = Locale.getDefault()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(0.3f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            list.forEach { day ->
                val dateResult = runCatching {
                    LocalDateTime.parse(day.dtTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                }.getOrNull()

                if (dateResult != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dateResult.format(DateTimeFormatter.ofPattern("EEE", locale)),
                            color = Color.White.copy(0.7f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(getWeatherIcon(day.weather.firstOrNull()?.main), null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${day.main.temp.localizeTemp()}°", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun getWeatherIcon(mainStatus: String?): ImageVector {
    return when (mainStatus?.lowercase()) {
        "clear" -> Icons.Rounded.WbSunny
        "clouds" -> Icons.Rounded.Cloud
        "rain" -> Icons.Rounded.WaterDrop
        "thunderstorm" -> Icons.Rounded.FlashOn
        "snow" -> Icons.Rounded.AcUnit
        else -> Icons.Rounded.CloudQueue
    }
}