package com.example.weatherapp.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.presentation.home.WeatherViewModel

@Composable
fun DrawerMenuContent(
    viewModel: WeatherViewModel,
    currentPage: Int,
    onItemClick: (Int) -> Unit,
    onUnitSettingsClick: () -> Unit,
    onLanguageClick: () -> Unit
) {
    val notifEnabled by viewModel.notificationsEnabled.collectAsState()
    val statusBarEnabled by viewModel.statusBarEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF212121))
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "SETTING",
            modifier = Modifier.padding(16.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        DrawerMenuItem(Icons.Default.Home, "Home", currentPage == 0) { onItemClick(0) }
        DrawerMenuItem(Icons.Default.LocationOn, "Manage location", currentPage == 1) { onItemClick(1) }
        DrawerMenuItem(Icons.Default.Notifications, "Alerts", currentPage == 2) { onItemClick(2) }

        HorizontalDivider(color = Color.Gray.copy(0.2f), modifier = Modifier.padding(vertical = 8.dp))

        DrawerSwitchItem(Icons.Default.NotificationsActive, "Notification", notifEnabled) {
            viewModel.toggleNotifications(it)
        }
        DrawerSwitchItem(Icons.Default.Thermostat, "Status bar", statusBarEnabled) {
            viewModel.toggleStatusBar(it)
        }

        HorizontalDivider(color = Color.Gray.copy(0.2f), modifier = Modifier.padding(vertical = 8.dp))

        DrawerMenuItem(Icons.Default.Settings, "Unit setting", false) { onUnitSettingsClick() }
        DrawerMenuItem(Icons.Default.Language, "Languages", false) { onLanguageClick() }

        Spacer(modifier = Modifier.weight(1f))

        Text("DEVELOPER", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 12.sp)
        DrawerMenuItem(Icons.Default.Shield, "Privacy Policy", false) {}
    }
}

@Composable
fun UnitSettingsDialog(viewModel: WeatherViewModel, onDismiss: () -> Unit) {
    val tempUnit by viewModel.tempUnit.collectAsState()
    val timeFormat by viewModel.timeFormat.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    val pressureUnit by viewModel.pressureUnit.collectAsState()
    val precipUnit by viewModel.precipUnit.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2B2B),
        shape = RoundedCornerShape(8.dp),
        title = {
            Text("UNIT SETTING", color = Color.White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                UnitToggleRow("Temperature", Icons.Rounded.WbSunny, listOf("F", "C"), tempUnit) {
                    viewModel.updateSettings(temp = it)
                }
                HorizontalDivider(color = Color.Gray.copy(0.1f), modifier = Modifier.padding(vertical = 4.dp))

                val selectedTimeDisplay = if (timeFormat.contains("12")) "12" else "24"
                UnitToggleRow("Time format", Icons.Rounded.Schedule, listOf("12", "24"), selectedTimeDisplay) {
                    viewModel.updateSettings(time = it)
                }
                HorizontalDivider(color = Color.Gray.copy(0.1f), modifier = Modifier.padding(vertical = 4.dp))

                UnitDropdownRow("Wind speed", Icons.Rounded.Air, listOf("km/h", "mph", "m/s", "knots", "ft/s"), windUnit) {
                    viewModel.updateSettings(wind = it)
                }
                HorizontalDivider(color = Color.Gray.copy(0.1f), modifier = Modifier.padding(vertical = 4.dp))

                UnitDropdownRow("Pressure", Icons.Rounded.Compress, listOf("hPa", "mbar", "mmHg", "inHg"), pressureUnit) {
                    viewModel.updateSettings(pressure = it)
                }
                HorizontalDivider(color = Color.Gray.copy(0.1f), modifier = Modifier.padding(vertical = 4.dp))

                UnitToggleRow("Precipitation", Icons.Rounded.InvertColors, listOf("mm", "in"), precipUnit) {
                    viewModel.updateSettings(precipitation = it)
                }
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)), shape = RoundedCornerShape(4.dp)) {
                    Text("DONE", color = Color.White)
                }
            }
        }
    )
}

@Composable
fun DrawerMenuItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.background(if (isSelected) Color.White.copy(0.1f) else Color.Transparent).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (isSelected) Color(0xFF00ACC1) else Color.LightGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(32.dp))
        Text(label, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun DrawerSwitchItem(icon: ImageVector, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(32.dp))
            Text(label, color = Color.White, fontSize = 16.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF00ACC1)))
    }
}

@Composable
fun UnitToggleRow(label: String, icon: ImageVector, options: List<String>, selected: String, onOptionSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 14.sp)
        }
        Row(modifier = Modifier.background(Color(0xFF1A1A1A), RoundedCornerShape(4.dp)).padding(2.dp)) {
            options.forEach { opt ->
                val isSelected = opt == selected
                Box(modifier = Modifier
                    .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(4.dp))
                    .clickable { onOptionSelected(opt) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(opt, color = if (isSelected) Color(0xFF00ACC1) else Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun UnitDropdownRow(label: String, icon: ImageVector, options: List<String>, current: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 14.sp)
        }
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(current, color = Color.Gray, fontSize = 14.sp)
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color(0xFF333333))) {
                options.forEach { opt ->
                    DropdownMenuItem(text = { Text(opt, color = Color.White) }, onClick = {
                        onOptionSelected(opt)
                        expanded = false
                    })
                }
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit) {
    val languages = listOf("English" to "en", "العربية" to "ar")
    var selected by remember { mutableStateOf("en") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2B2B),
        shape = RoundedCornerShape(8.dp),
        title = { Text("Select Language", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        text = {
            Column {
                languages.forEach { (name, code) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().selectable(selected == code, onClick = { selected = code }).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected == code, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00ACC1), unselectedColor = Color.Gray))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(name, color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("DONE", color = Color(0xFF00ACC1)) } }
    )
}
