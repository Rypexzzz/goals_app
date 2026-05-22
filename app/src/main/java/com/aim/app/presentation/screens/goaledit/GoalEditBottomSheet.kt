package com.aim.app.presentation.screens.goaledit

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
import com.aim.app.presentation.components.AimDatePickerDialog
import com.aim.app.presentation.components.AimEmojiPicker
import com.aim.app.presentation.components.AimMarkdownEditor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun GoalEditBottomSheet(
    mode: GoalEditMode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GoalEditViewModel = hiltViewModel<GoalEditViewModel, GoalEditViewModel.Factory>(
        key = "goal-edit-${(mode as? GoalEditMode.Edit)?.goalId ?: "new"}",
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
        GoalEditContent(
            state = state,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onEmojiChange = viewModel::onEmojiChange,
            onDeadlineChange = viewModel::onDeadlineChange,
            onSave = viewModel::onSave,
            onCancel = onDismiss,
        )
    }
}

@Composable
private fun GoalEditContent(
    state: GoalEditUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onEmojiChange: (String?) -> Unit,
    onDeadlineChange: (LocalDate?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var emojiPickerOpen by remember { mutableStateOf(false) }
    var datePickerOpen by remember { mutableStateOf(false) }
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
            text = stringResource(if (state.isExisting) R.string.goal_edit_title_existing else R.string.goal_edit_title_new),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .width(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(onClick = { emojiPickerOpen = true }) {
                    Text(
                        text = state.emoji ?: "🎯",
                        fontSize = if (state.emoji != null) 28.sp else 22.sp,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.goal_edit_field_title)) },
                placeholder = { Text(stringResource(R.string.goal_edit_field_title_hint)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.goal_edit_field_deadline),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { datePickerOpen = true }) {
                    Text(
                        text = state.deadline?.let(dateFormatter::format)
                            ?: stringResource(R.string.goal_edit_field_no_deadline),
                    )
                }
                if (state.deadline != null) {
                    TextButton(onClick = { onDeadlineChange(null) }) {
                        Text(stringResource(R.string.goal_edit_action_clear_deadline))
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.goal_edit_field_description),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AimMarkdownEditor(
                value = state.description,
                onValueChange = onDescriptionChange,
                placeholder = stringResource(R.string.goal_edit_field_description_hint),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(onClick = onSave, enabled = state.canSave) {
                Text(stringResource(R.string.goal_edit_action_save))
            }
        }
    }

    if (emojiPickerOpen) {
        AimEmojiPicker(
            onPick = {
                onEmojiChange(it)
                emojiPickerOpen = false
            },
            onDismiss = { emojiPickerOpen = false },
        )
    }
    if (datePickerOpen) {
        AimDatePickerDialog(
            initialDate = state.deadline,
            onDateSelected = {
                onDeadlineChange(it)
                datePickerOpen = false
            },
            onDismiss = { datePickerOpen = false },
        )
    }
}
