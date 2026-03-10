package com.example.weatherapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InitialSetupScreen(
    viewModel: WeatherViewModel
) {
    var selectedTempUnit by remember { mutableStateOf("C") }
    var selectedTimeFormat by remember { mutableStateOf("24") }
    var selectedWindUnit by remember { mutableStateOf("km/h") }
    var showWindDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF424242)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SETTING",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingRow(
                        icon = Icons.Default.WbSunny,
                        title = "Temperature",
                        options = listOf("F", "C"),
                        initialIndex = 1,
                        onOptionSelected = { selectedTempUnit = it }
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

                    SettingRow(
                        icon = Icons.Default.Schedule,
                        title = "Time format",
                        options = listOf("12", "24"),
                        initialIndex = 1,
                        onOptionSelected = { selectedTimeFormat = it }
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWindDialog = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Flag, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Wind speed", color = Color.White, fontSize = 16.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = selectedWindUnit, color = Color.Gray, fontSize = 14.sp)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
                        }
                    }
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

                    SwitchRow(icon = Icons.Default.Notifications, title = "Notification")
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    SwitchRow(icon = Icons.Default.Thermostat, title = "Status bar")
                }

                Button(
                    onClick = {
                        viewModel.onSetupDoneClicked(
                            tempUnit = selectedTempUnit,
                            timeFormat = selectedTimeFormat,
                            windUnit = selectedWindUnit
                        )
                    },
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .width(160.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("DONE", color = Color.White)
                }
            }
        }

        if (showWindDialog) {
            AlertDialog(
                onDismissRequest = { showWindDialog = false },
                title = { Text("Choose Wind Speed unit", fontWeight = FontWeight.Bold) },
                text = {
                    val units = listOf("mph", "km/h", "m/s", "knots", "ft/s")
                    Column {
                        units.forEach { unit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (unit == selectedWindUnit),
                                        onClick = { selectedWindUnit = unit; showWindDialog = false }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (unit == selectedWindUnit), onClick = null)
                                Text(text = unit, modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showWindDialog = false }) {
                        Text("CANCEL", color = Color(0xFF00ACC1))
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    options: List<String>,
    initialIndex: Int,
    onOptionSelected: (String) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(initialIndex) }

    LaunchedEffect(Unit) {
        onOptionSelected(options[initialIndex])
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = Color.White, fontSize = 16.sp)
        }

        Row(
            modifier = Modifier
                .background(Color(0xFF121212), RoundedCornerShape(4.dp))
                .padding(2.dp)
        ) {
            options.forEachIndexed { index, text ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) Color.White else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            selectedIndex = index
                            onOptionSelected(text)
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = if (isSelected) Color(0xFF00ACC1) else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, title: String) {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = Color.White, fontSize = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.Gray,
                uncheckedThumbColor = Color.DarkGray
            )
        )
    }
}