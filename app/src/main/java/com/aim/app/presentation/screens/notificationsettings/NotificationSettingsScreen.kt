package com.aim.app.presentation.screens.notificationsettings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.NotificationSettings
import com.aim.app.domain.model.NotificationType
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimTimePickerDialog
import com.aim.app.presentation.components.AimTopBar
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* результат не критичен: если отказано — уведомления просто не появятся */ }

    NotificationSettingsContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onMasterToggle = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            viewModel.onMasterToggle(enabled)
        },
        onTypeToggle = viewModel::onTypeToggle,
        onTypeTime = viewModel::onTypeTime,
        onDndChange = viewModel::onDoNotDisturbChange,
    )
}

@Composable
private fun NotificationSettingsContent(
    state: NotificationSettingsUiState,
    onBack: () -> Unit,
    onMasterToggle: (Boolean) -> Unit,
    onTypeToggle: (NotificationType, Boolean) -> Unit,
    onTypeTime: (NotificationType, LocalTime) -> Unit,
    onDndChange: (LocalTime?, LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var timePickerFor by remember { mutableStateOf<NotificationType?>(null) }
    var dndStartPicker by remember { mutableStateOf(false) }
    var dndEndPicker by remember { mutableStateOf(false) }
    val settings = state.settings

    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = stringResource(R.string.notif_settings_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "master") {
                AimCard {
                    ToggleRow(
                        title = stringResource(R.string.notif_master_title),
                        checked = settings.masterEnabled,
                        onCheckedChange = onMasterToggle,
                    )
                }
            }
            items(items = NotificationType.entries.toList(), key = { it.name }) { type ->
                NotificationTypeCard(
                    type = type,
                    settings = settings,
                    enabled = settings.masterEnabled,
                    onToggle = { onTypeToggle(type, it) },
                    onTimeClick = { timePickerFor = type },
                )
            }
            item(key = "dnd") {
                DoNotDisturbCard(
                    settings = settings,
                    onStartClick = { dndStartPicker = true },
                    onEndClick = { dndEndPicker = true },
                    onClear = { onDndChange(null, null) },
                )
            }
        }
    }

    timePickerFor?.let { type ->
        AimTimePickerDialog(
            initialTime = settings.settingsFor(type).time ?: LocalTime.of(9, 0),
            onTimeSelected = { time ->
                onTypeTime(type, time)
                timePickerFor = null
            },
            onDismiss = { timePickerFor = null },
        )
    }
    if (dndStartPicker) {
        AimTimePickerDialog(
            initialTime = settings.doNotDisturbStart ?: LocalTime.of(22, 0),
            onTimeSelected = {
                onDndChange(it, settings.doNotDisturbEnd ?: LocalTime.of(7, 0))
                dndStartPicker = false
            },
            onDismiss = { dndStartPicker = false },
        )
    }
    if (dndEndPicker) {
        AimTimePickerDialog(
            initialTime = settings.doNotDisturbEnd ?: LocalTime.of(7, 0),
            onTimeSelected = {
                onDndChange(settings.doNotDisturbStart ?: LocalTime.of(22, 0), it)
                dndEndPicker = false
            },
            onDismiss = { dndEndPicker = false },
        )
    }
}

@Composable
private fun NotificationTypeCard(
    type: NotificationType,
    settings: NotificationSettings,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeSettings = settings.settingsFor(type)
    AimCard(modifier = modifier) {
        ToggleRow(
            title = stringResource(type.titleRes),
            subtitle = stringResource(type.descriptionRes),
            checked = typeSettings.enabled && enabled,
            enabled = enabled,
            onCheckedChange = onToggle,
        )
        if (type.isTimed && typeSettings.time != null && typeSettings.enabled && enabled) {
            TextButton(onClick = onTimeClick) {
                Text(
                    text = stringResource(R.string.notif_time_label, typeSettings.time.format(timeFormatter)),
                )
            }
        }
    }
}

@Composable
private fun DoNotDisturbCard(
    settings: NotificationSettings,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.notif_dnd_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onStartClick) {
                Text(settings.doNotDisturbStart?.format(timeFormatter) ?: stringResource(R.string.notif_dnd_start))
            }
            Text("—")
            TextButton(onClick = onEndClick) {
                Text(settings.doNotDisturbEnd?.format(timeFormatter) ?: stringResource(R.string.notif_dnd_end))
            }
            Spacer(Modifier.width(8.dp))
            if (settings.doNotDisturbStart != null || settings.doNotDisturbEnd != null) {
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@get:StringRes
private val NotificationType.titleRes: Int
    get() = when (this) {
        NotificationType.MORNING_BRIEF -> R.string.notif_type_morning
        NotificationType.FIRST_THING -> R.string.notif_type_first_thing
        NotificationType.EVENING_CHECKIN -> R.string.notif_type_evening
        NotificationType.STREAK_WARNING -> R.string.notif_type_streak
        NotificationType.WEEKLY_SUMMARY -> R.string.notif_type_weekly
        NotificationType.DEADLINE_APPROACHING -> R.string.notif_type_deadline
    }

@get:StringRes
private val NotificationType.descriptionRes: Int
    get() = when (this) {
        NotificationType.MORNING_BRIEF -> R.string.notif_type_morning_desc
        NotificationType.FIRST_THING -> R.string.notif_type_first_thing_desc
        NotificationType.EVENING_CHECKIN -> R.string.notif_type_evening_desc
        NotificationType.STREAK_WARNING -> R.string.notif_type_streak_desc
        NotificationType.WEEKLY_SUMMARY -> R.string.notif_type_weekly_desc
        NotificationType.DEADLINE_APPROACHING -> R.string.notif_type_deadline_desc
    }
