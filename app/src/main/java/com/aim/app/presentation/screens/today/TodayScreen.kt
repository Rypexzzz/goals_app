package com.aim.app.presentation.screens.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TodayItem
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimDatePickerDialog
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimProgressRing
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.components.aimSuccessColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayContent(
        modifier = modifier,
        state = state,
        onSettingsClick = onSettingsClick,
        onToggle = viewModel::onToggle,
        onMarkHabitFailed = viewModel::onMarkHabitFailed,
        onToggleOverdue = viewModel::toggleOverdueExpanded,
        onSnoozeTomorrow = viewModel::onSnoozeToTomorrow,
        onSnoozePlus3 = { viewModel.onSnoozePlusDays(it, 3) },
        onReschedule = viewModel::onReschedule,
        onMoveOverdueToToday = viewModel::onMoveOverdueToToday,
        onMoveOverdueToTomorrow = viewModel::onSnoozeToTomorrow,
        onDeleteTask = viewModel::onDeleteTask,
    )
}

@Composable
private fun TodayContent(
    state: TodayUiState,
    onSettingsClick: () -> Unit,
    onToggle: (TodayItem) -> Unit,
    onMarkHabitFailed: (TodayItem.HabitItem) -> Unit,
    onToggleOverdue: () -> Unit,
    onSnoozeTomorrow: (Long) -> Unit,
    onSnoozePlus3: (Long) -> Unit,
    onReschedule: (Long, LocalDate?) -> Unit,
    onMoveOverdueToToday: (Long) -> Unit,
    onMoveOverdueToTomorrow: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var datePickerForTask by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = formatDateTitle(state.date),
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
        if (state.isEmpty && !state.isLoading) {
            AimEmptyState(
                modifier = Modifier.padding(padding),
                emoji = "🌅",
                title = stringResource(R.string.today_empty_title),
                subtitle = stringResource(R.string.today_empty_subtitle),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "summary") {
                SummaryCard(doneCount = state.doneCount, total = state.totalCount, progress = state.progress)
            }

            if (state.overdueTasks.isNotEmpty()) {
                item(key = "overdue") {
                    OverdueBanner(
                        count = state.overdueTasks.size,
                        expanded = state.overdueExpanded,
                        tasks = state.overdueTasks,
                        onToggle = onToggleOverdue,
                        onMoveToday = onMoveOverdueToToday,
                        onMoveTomorrow = onMoveOverdueToTomorrow,
                        onDelete = onDeleteTask,
                    )
                }
            }

            if (state.todo.isNotEmpty()) {
                item(key = "todo-header") { SectionHeader(stringResource(R.string.today_section_todo)) }
                items(items = state.todo, key = { it.stableKey }) { item ->
                    TodayItemRow(
                        item = item,
                        onToggle = { onToggle(item) },
                        onMarkFailed = { (item as? TodayItem.HabitItem)?.let(onMarkHabitFailed) },
                        onSnoozeTomorrow = onSnoozeTomorrow,
                        onSnoozePlus3 = onSnoozePlus3,
                        onPickDate = { datePickerForTask = it },
                        onDelete = onDeleteTask,
                    )
                }
            }

            if (state.doneToday.isNotEmpty()) {
                item(key = "done-header") { SectionHeader(stringResource(R.string.today_section_done)) }
                items(items = state.doneToday, key = { it.stableKey }) { item ->
                    TodayItemRow(
                        item = item,
                        onToggle = { onToggle(item) },
                        onMarkFailed = {},
                        onSnoozeTomorrow = onSnoozeTomorrow,
                        onSnoozePlus3 = onSnoozePlus3,
                        onPickDate = { datePickerForTask = it },
                        onDelete = onDeleteTask,
                    )
                }
            }
        }
    }

    datePickerForTask?.let { taskId ->
        AimDatePickerDialog(
            initialDate = state.date,
            onDateSelected = { date ->
                onReschedule(taskId, date)
                datePickerForTask = null
            },
            onDismiss = { datePickerForTask = null },
        )
    }
}

@Composable
private fun SummaryCard(doneCount: Int, total: Int, progress: Float, modifier: Modifier = Modifier) {
    AimCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AimProgressRing(
                progress = progress,
                centerLabel = "$doneCount/$total",
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.today_summary_done, doneCount, total),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun OverdueBanner(
    count: Int,
    expanded: Boolean,
    tasks: List<Task>,
    onToggle: () -> Unit,
    onMoveToday: (Long) -> Unit,
    onMoveTomorrow: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onToggle)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = pluralStringResource(R.plurals.today_overdue_banner, count, count),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "OverdueChevron")
            Icon(
                modifier = Modifier.rotate(rotation),
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tasks.forEach { task ->
                    Column {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { onMoveToday(task.id) }) {
                                Text(stringResource(R.string.today_overdue_move_today))
                            }
                            TextButton(onClick = { onMoveTomorrow(task.id) }) {
                                Text(stringResource(R.string.today_overdue_move_tomorrow))
                            }
                            TextButton(onClick = { onDelete(task.id) }) {
                                Text(
                                    text = stringResource(R.string.today_action_delete),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(top = 8.dp, start = 4.dp),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TodayItemRow(
    item: TodayItem,
    onToggle: () -> Unit,
    onMarkFailed: () -> Unit,
    onSnoozeTomorrow: (Long) -> Unit,
    onSnoozePlus3: (Long) -> Unit,
    onPickDate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val isHabit = item is TodayItem.HabitItem
    val isFailed = (item as? TodayItem.HabitItem)?.isFailed == true
    val emoji = item.emoji

    AimCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompletionToggle(done = item.isDone, failed = isFailed, onClick = onToggle)
            Spacer(Modifier.width(12.dp))
            if (!emoji.isNullOrEmpty()) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (item.isDone) 0.6f else 1f),
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
            )
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.today_item_menu),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ItemMenu(
                    expanded = menuOpen,
                    isHabit = isHabit,
                    onDismiss = { menuOpen = false },
                    onSnoozeTomorrow = {
                        menuOpen = false
                        (item as? TodayItem.TaskItem)?.takeIf { !it.isRecurringInstance }
                            ?.let { onSnoozeTomorrow(it.task.id) }
                    },
                    onSnoozePlus3 = {
                        menuOpen = false
                        (item as? TodayItem.TaskItem)?.takeIf { !it.isRecurringInstance }
                            ?.let { onSnoozePlus3(it.task.id) }
                    },
                    onPickDate = {
                        menuOpen = false
                        (item as? TodayItem.TaskItem)?.takeIf { !it.isRecurringInstance }
                            ?.let { onPickDate(it.task.id) }
                    },
                    onMarkFailed = {
                        menuOpen = false
                        onMarkFailed()
                    },
                    onDelete = {
                        menuOpen = false
                        (item as? TodayItem.TaskItem)?.let { onDelete(it.task.id) }
                    },
                )
            }
        }
    }
}

@Composable
private fun ItemMenu(
    expanded: Boolean,
    isHabit: Boolean,
    onDismiss: () -> Unit,
    onSnoozeTomorrow: () -> Unit,
    onSnoozePlus3: () -> Unit,
    onPickDate: () -> Unit,
    onMarkFailed: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        if (isHabit) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.today_habit_failed)) },
                onClick = onMarkFailed,
                leadingIcon = {
                    Icon(Icons.Outlined.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.today_action_snooze_tomorrow)) },
                onClick = onSnoozeTomorrow,
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.today_action_snooze_3days)) },
                onClick = onSnoozePlus3,
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.today_action_pick_date)) },
                onClick = onPickDate,
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.today_action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun CompletionToggle(
    done: Boolean,
    failed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "TodayCheck",
    )
    val fillColor: Color = when {
        done -> aimSuccessColor()
        failed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(fillColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            done -> Icon(
                modifier = Modifier.size((18 * scale).dp.coerceAtLeast(1.dp)),
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
            failed -> Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Outlined.Cancel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
            )
        }
    }
}

private fun formatDateTitle(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))
    return date.format(formatter).replaceFirstChar { it.uppercase() }
}
