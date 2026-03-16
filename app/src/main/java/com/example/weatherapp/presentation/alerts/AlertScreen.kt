package com.example.weatherapp.presentation.alerts

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.weatherapp.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scheduleAlert()
        } else {
            Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AlertsViewModel.AlertsUiEvent.ShowToast -> {
                    Toast.makeText(context, context.getString(event.messageId), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val startTimePickerState = rememberTimePickerState(
        initialHour = state.startHour,
        initialMinute = state.startMinute,
        is24Hour = false
    )

    val endTimePickerState = rememberTimePickerState(
        initialHour = state.endHour,
        initialMinute = state.endMinute,
        is24Hour = false
    )

    if (showStartTimePicker) {
        TimePickerDialog(
            onCancel = { showStartTimePicker = false },
            onConfirm = {
                viewModel.onStartTimeChanged(startTimePickerState.hour, startTimePickerState.minute)
                showStartTimePicker = false
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onCancel = { showEndTimePicker = false },
            onConfirm = {
                viewModel.onEndTimeChanged(endTimePickerState.hour, endTimePickerState.minute)
                showEndTimePicker = false
            }
        ) {
            TimePicker(state = endTimePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.set_alert_time),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // From Time
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStartTimePicker = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "From", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = String.format("%02d:%02d", state.startHour, state.startMinute),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // To Time
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEndTimePicker = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "To", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = String.format("%02d:%02d", state.endHour, state.endMinute),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = state.selectedType == "NOTIFICATION",
                onClick = { viewModel.onTypeChanged("NOTIFICATION") }
            )
            Text(text = stringResource(R.string.label_notification))

            Spacer(modifier = Modifier.width(24.dp))

            RadioButton(
                selected = state.selectedType == "ALARM",
                onClick = { viewModel.onTypeChanged("ALARM") }
            )
            Text(text = stringResource(R.string.label_alarm))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.scheduleAlert()
                }
            }
        ) {
            Text(text = stringResource(R.string.btn_save_alert))
        }
    }
}

@Composable
fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        text = {
            content()
        }
    )
}
