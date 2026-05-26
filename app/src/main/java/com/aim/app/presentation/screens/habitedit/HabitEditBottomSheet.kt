package com.aim.app.presentation.screens.habitedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.HabitFrequency
import com.aim.app.presentation.components.AimChip
import com.aim.app.presentation.components.AimEmojiPicker
import com.aim.app.presentation.components.AimMarkdownEditor
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HabitEditBottomSheet(
    mode: HabitEditMode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val key = when (mode) {
        is HabitEditMode.Create -> "habit-edit-new-${mode.goalId}"
        is HabitEditMode.Edit -> "habit-edit-${mode.habitId}"
    }
    val viewModel: HabitEditViewModel = hiltViewModel<HabitEditViewModel, HabitEditViewModel.Factory>(
        key = key,
        creationCallback = { factory -> factory.create(mode) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onDismiss()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        HabitEditContent(
            state = state,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onEmojiChange = viewModel::onEmojiChange,
            onFrequencyChange = viewModel::onFrequencyChange,
            onSave = viewModel::onSave,
            onCancel = onDismiss,
        )
    }
}

@Composable
private fun HabitEditContent(
    state: HabitEditUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onEmojiChange: (String?) -> Unit,
    onFrequencyChange: (HabitFrequency) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var emojiPickerOpen by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(
                if (state.isExisting) R.string.habit_edit_title_existing
                else R.string.habit_edit_title_new,
            ),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.height(56.dp).width(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(onClick = { emojiPickerOpen = true }) {
                    Text(
                        text = state.emoji ?: "🌱",
                        fontSize = if (state.emoji != null) 28.sp else 22.sp,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.habit_edit_field_title)) },
                placeholder = { Text(stringResource(R.string.habit_edit_field_title_hint)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
        }

        FrequencySection(
            current = state.frequency,
            onChange = onFrequencyChange,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.habit_edit_field_description),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AimMarkdownEditor(
                value = state.description,
                onValueChange = onDescriptionChange,
                placeholder = stringResource(R.string.habit_edit_field_description_hint),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        ) {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
            Button(onClick = onSave, enabled = state.canSave) {
                Text(stringResource(R.string.habit_edit_action_save))
            }
        }
    }

    if (emojiPickerOpen) {
        AimEmojiPicker(
            onPick = { onEmojiChange(it); emojiPickerOpen = false },
            onDismiss = { emojiPickerOpen = false },
        )
    }
}

@Composable
private fun FrequencySection(
    current: HabitFrequency,
    onChange: (HabitFrequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.habit_edit_field_frequency),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AimChip(
                selected = current == HabitFrequency.Daily,
                onClick = { onChange(HabitFrequency.Daily) },
                label = stringResource(R.string.habit_frequency_daily),
            )
            AimChip(
                selected = current is HabitFrequency.TimesPerWeek,
                onClick = { onChange(HabitFrequency.TimesPerWeek(times = 3)) },
                label = stringResource(R.string.habit_frequency_times_per_week),
            )
            AimChip(
                selected = current is HabitFrequency.TimesPerMonth,
                onClick = { onChange(HabitFrequency.TimesPerMonth(times = 10)) },
                label = stringResource(R.string.habit_frequency_times_per_month),
            )
            AimChip(
                selected = current is HabitFrequency.SpecificDays,
                onClick = {
                    onChange(
                        HabitFrequency.SpecificDays(
                            setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                        ),
                    )
                },
                label = stringResource(R.string.habit_frequency_specific_days),
            )
        }

        when (val freq = current) {
            is HabitFrequency.TimesPerWeek -> TimesPicker(
                value = freq.times,
                range = 1..7,
                onChange = { onChange(HabitFrequency.TimesPerWeek(it)) },
                label = pluralStringResource(R.plurals.habit_times_week, freq.times, freq.times),
            )
            is HabitFrequency.TimesPerMonth -> TimesPicker(
                value = freq.times,
                range = 1..31,
                onChange = { onChange(HabitFrequency.TimesPerMonth(it)) },
                label = pluralStringResource(R.plurals.habit_times_month, freq.times, freq.times),
            )
            is HabitFrequency.SpecificDays -> DaysPicker(
                selected = freq.days,
                onChange = { onChange(HabitFrequency.SpecificDays(it)) },
            )
            HabitFrequency.Daily -> Unit
        }
    }
}

@Composable
private fun TimesPicker(
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { if (value > range.first) onChange(value - 1) }) { Text("−") }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        TextButton(onClick = { if (value < range.last) onChange(value + 1) }) { Text("+") }
    }
}

@Composable
private fun DaysPicker(
    selected: Set<DayOfWeek>,
    onChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = remember { Locale("ru") }
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DayOfWeek.entries.forEach { day ->
            val isSelected = day in selected
            val label = day.getDisplayName(TextStyle.SHORT, locale)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable {
                        val next = if (isSelected) selected - day else selected + day
                        if (next.isNotEmpty()) onChange(next)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
