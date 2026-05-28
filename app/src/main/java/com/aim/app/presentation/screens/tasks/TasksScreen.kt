package com.aim.app.presentation.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskStatus
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.theme.AimTheme
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun TasksScreen(
    onSettingsClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TasksScreenContent(
        modifier = modifier,
        state = state,
        onSettingsClick = onSettingsClick,
        onTaskClick = onTaskClick,
        onComplete = viewModel::onComplete,
    )
}

@Composable
private fun TasksScreenContent(
    state: TasksUiState,
    onSettingsClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onComplete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = stringResource(R.string.tasks_title),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.tasks.isEmpty() && !state.isLoading) {
            AimEmptyState(
                modifier = Modifier.padding(padding),
                emoji = "🗓️",
                title = stringResource(R.string.tasks_empty_title),
                subtitle = stringResource(R.string.tasks_empty_subtitle),
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = state.tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onClick = { onTaskClick(task.id) },
                        onComplete = { onComplete(task.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.status == TaskStatus.COMPLETED,
                onCheckedChange = { onComplete() },
            )
            if (!task.emoji.isNullOrEmpty()) {
                Text(text = task.emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                task.deadline?.let { DeadlineChip(deadline = it) }
            }
        }
    }
}

@Composable
private fun DeadlineChip(deadline: LocalDate, modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    val daysUntil = ChronoUnit.DAYS.between(today, deadline)
    val overdue = daysUntil < 0
    val color: Color = when {
        overdue -> MaterialTheme.colorScheme.error
        daysUntil < 3 -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
        daysUntil < 14 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale("ru"))
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            modifier = Modifier.height(14.dp),
            tint = color,
        )
        Text(
            text = if (overdue) {
                "${stringResource(R.string.tasks_overdue)} · ${formatter.format(deadline)}"
            } else {
                formatter.format(deadline)
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (overdue) FontWeight.SemiBold else FontWeight.Normal,
            color = color,
        )
    }
}

@PreviewLightDark
@Composable
private fun TasksScreenPreview() {
    AimTheme {
        TasksScreenContent(
            state = TasksUiState(
                isLoading = false,
                tasks = listOf(
                    Task(
                        id = 1, goalId = 1, parentTaskId = null,
                        title = "Записаться на дерматоскопию",
                        description = null, emoji = "🩺",
                        deadline = LocalDate.now().minusDays(2), scheduledFor = null, scheduledTime = null,
                        status = TaskStatus.IN_PROGRESS, depth = 0, orderIndex = 0, recurrence = null,
                        createdAt = Instant.EPOCH, completedAt = null, deletedAt = null,
                    ),
                    Task(
                        id = 2, goalId = 1, parentTaskId = null,
                        title = "Купить экипировку для зала",
                        description = null, emoji = "🏋️",
                        deadline = LocalDate.now().plusDays(9), scheduledFor = null, scheduledTime = null,
                        status = TaskStatus.IN_PROGRESS, depth = 0, orderIndex = 1, recurrence = null,
                        createdAt = Instant.EPOCH, completedAt = null, deletedAt = null,
                    ),
                ),
            ),
            onSettingsClick = {},
            onTaskClick = {},
            onComplete = {},
        )
    }
}
