package com.aim.app.presentation.screens.taskedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.Recurrence
import com.aim.app.presentation.components.AimChip
import com.aim.app.presentation.components.AimDatePickerDialog
import com.aim.app.presentation.components.AimEmojiPicker
import com.aim.app.presentation.components.AimMarkdownEditor
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun TaskEditBottomSheet(
    mode: TaskEditMode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val key = when (mode) {
        is TaskEditMode.Create -> "task-edit-new-${mode.goalId}-${mode.parentTaskId}"
        is TaskEditMode.Edit -> "task-edit-${mode.taskId}"
    }
    val viewModel: TaskEditViewModel = hiltViewModel<TaskEditViewModel, TaskEditViewModel.Factory>(
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
        TaskEditContent(
            state = state,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onEmojiChange = viewModel::onEmojiChange,
            onScheduledForChange = viewModel::onScheduledForChange,
            onScheduledTimeChange = viewModel::onScheduledTimeChange,
            onDeadlineChange = viewModel::onDeadlineChange,
            onRecurrenceChange = viewModel::onRecurrenceChange,
            onSave = viewModel::onSave,
            onCancel = onDismiss,
        )
    }
}

@Composable
private fun TaskEditContent(
    state: TaskEditUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onEmojiChange: (String?) -> Unit,
    onScheduledForChange: (LocalDate?) -> Unit,
    onScheduledTimeChange: (LocalTime?) -> Unit,
    onDeadlineChange: (LocalDate?) -> Unit,
    onRecurrenceChange: (Recurrence?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var emojiPickerOpen by remember { mutableStateOf(false) }
    var scheduledDateOpen by remember { mutableStateOf(false) }
    var deadlineOpen by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val dateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale("ru"))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(if (state.isExisting) R.string.task_edit_title_existing else R.string.task_edit_title_new),
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
                        text = state.emoji ?: "✏️",
                        fontSize = if (state.emoji != null) 28.sp else 20.sp,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.task_edit_field_title)) },
                placeholder = { Text(stringResource(R.string.task_edit_field_title_hint)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.task_edit_field_scheduled_for),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { scheduledDateOpen = true }) {
                    Text(state.scheduledFor?.let(dateFormatter::format) ?: stringResource(R.string.goal_edit_field_no_deadline))
                }
                if (state.scheduledFor != null) {
                    TextButton(onClick = { onScheduledForChange(null) }) {
                        Text(stringResource(R.string.goal_edit_action_clear_deadline))
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.task_edit_field_deadline),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { deadlineOpen = true }) {
                    Text(state.deadline?.let(dateFormatter::format) ?: stringResource(R.string.goal_edit_field_no_deadline))
                }
                if (state.deadline != null) {
                    TextButton(onClick = { onDeadlineChange(null) }) {
                        Text(stringResource(R.string.goal_edit_action_clear_deadline))
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.task_edit_field_recurrence),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AimChip(
                    selected = state.recurrence == null,
                    onClick = { onRecurrenceChange(null) },
                    label = stringResource(R.string.task_edit_recurrence_none),
                )
                AimChip(
                    selected = state.recurrence == Recurrence.Daily,
                    onClick = { onRecurrenceChange(Recurrence.Daily) },
                    label = stringResource(R.string.task_edit_recurrence_daily),
                )
                AimChip(
                    selected = state.recurrence == Recurrence.Weekly,
                    onClick = { onRecurrenceChange(Recurrence.Weekly) },
                    label = stringResource(R.string.task_edit_recurrence_weekly),
                )
                AimChip(
                    selected = state.recurrence == Recurrence.Monthly,
                    onClick = { onRecurrenceChange(Recurrence.Monthly) },
                    label = stringResource(R.string.task_edit_recurrence_monthly),
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.task_edit_field_description),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AimMarkdownEditor(
                value = state.description,
                onValueChange = onDescriptionChange,
                placeholder = stringResource(R.string.task_edit_field_description_hint),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        ) {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
            Button(onClick = onSave, enabled = state.canSave) {
                Text(stringResource(R.string.task_edit_action_save))
            }
        }
    }

    if (emojiPickerOpen) {
        AimEmojiPicker(
            onPick = { onEmojiChange(it); emojiPickerOpen = false },
            onDismiss = { emojiPickerOpen = false },
        )
    }
    if (scheduledDateOpen) {
        AimDatePickerDialog(
            initialDate = state.scheduledFor,
            onDateSelected = { onScheduledForChange(it); scheduledDateOpen = false },
            onDismiss = { scheduledDateOpen = false },
        )
    }
    if (deadlineOpen) {
        AimDatePickerDialog(
            initialDate = state.deadline,
            onDateSelected = { onDeadlineChange(it); deadlineOpen = false },
            onDismiss = { deadlineOpen = false },
        )
    }
}
