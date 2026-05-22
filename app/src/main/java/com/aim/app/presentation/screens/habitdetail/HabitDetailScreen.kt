package com.aim.app.presentation.screens.habitdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitStats
import com.aim.app.presentation.components.AimAlertDialog
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimHabitCalendar
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.components.aimSuccessColor
import com.aim.app.presentation.screens.habitedit.HabitEditBottomSheet
import com.aim.app.presentation.screens.habitedit.HabitEditMode

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onBack()
    }

    HabitDetailContent(
        modifier = modifier,
        state = state,
        habitId = viewModel.habitId,
        onBack = onBack,
        onOpenGoal = onOpenGoal,
        onDayTap = viewModel::onDayTap,
        onMarkTodayDone = viewModel::onMarkTodayDone,
        onMarkTodayFailed = viewModel::onMarkTodayFailed,
        onArchive = viewModel::onArchive,
        onRequestDelete = viewModel::requestDelete,
        onDismissDelete = viewModel::dismissDelete,
        onConfirmDelete = viewModel::confirmDelete,
    )
}

@Composable
private fun HabitDetailContent(
    state: HabitDetailUiState,
    habitId: Long,
    onBack: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    onDayTap: (java.time.LocalDate) -> Unit,
    onMarkTodayDone: () -> Unit,
    onMarkTodayFailed: () -> Unit,
    onArchive: () -> Unit,
    onRequestDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var editOpen by remember { mutableStateOf(false) }

    val habit = state.habit

    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = habit?.title.orEmpty(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (habit != null) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.action_more),
                            )
                        }
                        ActionsMenu(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            onEdit = { menuExpanded = false; editOpen = true },
                            onArchive = { menuExpanded = false; onArchive() },
                            onDelete = { menuExpanded = false; onRequestDelete() },
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (habit == null || state.stats == null) {
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HabitHeader(
                habit = habit,
                stats = state.stats,
                linkedGoalTitle = state.linkedGoal?.title,
                onOpenGoal = { state.linkedGoal?.id?.let(onOpenGoal) },
            )
            QuickActionButtons(
                onMarkDone = onMarkTodayDone,
                onMarkFailed = onMarkTodayFailed,
            )
            AimCard(modifier = Modifier.fillMaxWidth()) {
                AimHabitCalendar(
                    statusByDate = state.statusByDate,
                    onDayTap = onDayTap,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (state.confirmDelete) {
        AimAlertDialog(
            title = stringResource(R.string.habit_detail_delete_title),
            text = stringResource(R.string.habit_detail_delete_message),
            confirmLabel = stringResource(R.string.action_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }

    if (editOpen) {
        HabitEditBottomSheet(
            mode = HabitEditMode.Edit(habitId = habitId),
            onDismiss = { editOpen = false },
        )
    }
}

@Composable
private fun HabitHeader(
    habit: Habit,
    stats: HabitStats,
    linkedGoalTitle: String?,
    onOpenGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!habit.emoji.isNullOrEmpty()) {
                Text(text = habit.emoji, fontSize = 40.sp)
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (linkedGoalTitle != null) {
                    TextButton(
                        onClick = onOpenGoal,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Text(
                            text = "→ $linkedGoalTitle",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        StatsRow(stats = stats)
    }
}

@Composable
private fun StatsRow(stats: HabitStats, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatPill(
            label = stringResource(R.string.habit_stat_current_streak),
            value = stats.currentStreak.toString(),
            highlightColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Outlined.LocalFireDepartment,
            modifier = Modifier.weight(1f),
        )
        StatPill(
            label = stringResource(R.string.habit_stat_best_streak),
            value = stats.bestStreak.toString(),
            highlightColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
        if (stats.completionPercent != null) {
            StatPill(
                label = stringResource(R.string.habit_stat_completion),
                value = "${stats.completionPercent.toInt()}%",
                highlightColor = aimSuccessColor(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Column(
        modifier = modifier
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = highlightColor)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = highlightColor,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickActionButtons(
    onMarkDone: () -> Unit,
    onMarkFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onMarkDone,
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = aimSuccessColor())
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.habit_action_done_today))
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onMarkFailed,
        ) {
            Icon(Icons.Outlined.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.habit_action_failed_today))
        }
    }
}

@Composable
private fun ActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.habit_action_edit)) },
            onClick = onEdit,
            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.habit_action_archive)) },
            onClick = onArchive,
            leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.habit_action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            },
            onClick = onDelete,
            leadingIcon = {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
        )
    }
}
