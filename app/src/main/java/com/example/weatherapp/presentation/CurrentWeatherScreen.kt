package com.example.weatherapp.presentation

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import com.example.weatherapp.data.models.home.FullWeatherData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CurrentWeatherScreen(data: FullWeatherData, unit: String) {
    val current = data.current
    val currentDateTime = LocalDateTime.now()
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
                MainWeatherHeader(current, currentDateTime, unit)
            }

            item {
                WeatherExtraDetails(current)
            }

            item {
                Text(
                    "Hourly Forecast",
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                HourlyForecastCard(data.forecast.list.take(8))
            }

            item {
                Text(
                    "Next 5 Days",
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                val distinctForecast = data.forecast.list
                    .filter {
                        val date = LocalDate.parse(it.dtTxt.split(" ")[0])
                        date.isAfter(todayDate)
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
    current: com.example.weatherapp.data.models.current.CurrentWeatherResponse,
    dateTime: LocalDateTime,
    unit: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${current.name}, ${current.sys.country}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    dateTime.format(DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.ENGLISH)),
                    color = Color.White.copy(0.7f)
                )
                Text(
                    dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    color = Color.White,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${current.main.temp.toInt()}",
                    color = Color.White,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.W100
                )
                Column {
                    Text("°$unit", color = Color.White, fontSize = 24.sp)
                    Icon(
                        getWeatherIcon(current.weather.firstOrNull()?.main),
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherExtraDetails(current: com.example.weatherapp.data.models.current.CurrentWeatherResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        color = Color.Black.copy(0.25f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailItem("Wind", "${current.wind.speed} km/h", Icons.Rounded.Air)
            DetailItem("Humidity", "${current.main.humidity}%", Icons.Rounded.WaterDrop)
            DetailItem("Pressure", "${current.main.pressure}", Icons.Rounded.Compress)
            DetailItem("Clouds", "${current.clouds.all}%", Icons.Rounded.Cloud)
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
fun HourlyForecastCard(list: List<com.example.weatherapp.data.models.forecast.ForecastItem>) {
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
                val time = LocalDateTime.parse(hour.dtTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(time.format(DateTimeFormatter.ofPattern("HH:mm")), color = Color.White, fontSize = 12.sp)
                    Icon(getWeatherIcon(hour.weather.firstOrNull()?.main), null, tint = Color.White, modifier = Modifier.size(26.dp))
                    Text("${hour.main.temp.toInt()}°", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailyForecastCard(list: List<com.example.weatherapp.data.models.forecast.ForecastItem>) {
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
                val date = LocalDateTime.parse(day.dtTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)),
                        color = Color.White.copy(0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(getWeatherIcon(day.weather.firstOrNull()?.main), null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${day.main.temp.toInt()}°", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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