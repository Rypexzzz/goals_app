package com.aim.app.presentation.screens.goaldetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalStatus
import com.aim.app.domain.model.Task
import com.aim.app.presentation.components.AimAlertDialog
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimFAB
import com.aim.app.presentation.components.AimMarkdownText
import com.aim.app.presentation.components.AimTaskTree
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.components.TaskTreeCallbacks
import com.aim.app.presentation.screens.goaledit.GoalEditBottomSheet
import com.aim.app.presentation.screens.goaledit.GoalEditMode
import com.aim.app.presentation.screens.taskedit.TaskEditBottomSheet
import com.aim.app.presentation.screens.taskedit.TaskEditMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    onOpenTask: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onBack()
    }

    GoalDetailContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onOpenTask = onOpenTask,
        onToggleExpand = viewModel::onToggleExpand,
        onToggleTaskCompletion = viewModel::onToggleTaskCompletion,
        onDeleteTask = viewModel::onDeleteTask,
        onReorderSiblings = viewModel::onReorderSiblings,
        onArchive = viewModel::onArchiveGoal,
        onComplete = viewModel::requestCompleteGoal,
        onUncomplete = viewModel::onUncompleteGoal,
        onDelete = viewModel::requestDeleteGoal,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onConfirmDelete = viewModel::confirmDeleteGoal,
        onDismissCompleteDialog = viewModel::dismissCompleteDialog,
        onCompleteOnly = viewModel::completeGoalOnly,
        onCompleteAll = viewModel::completeGoalAndPendingTasks,
        goalId = viewModel.goalId,
    )
}

@Composable
private fun GoalDetailContent(
    state: GoalDetailUiState,
    onBack: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onToggleExpand: (Long) -> Unit,
    onToggleTaskCompletion: (Task) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onReorderSiblings: (Long?, List<Long>) -> Unit,
    onArchive: () -> Unit,
    onComplete: () -> Unit,
    onUncomplete: () -> Unit,
    onDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissCompleteDialog: () -> Unit,
    onCompleteOnly: () -> Unit,
    onCompleteAll: () -> Unit,
    goalId: Long,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var editGoalOpen by remember { mutableStateOf(false) }
    var addTaskParent by remember { mutableStateOf<Long?>(null) }
    var addTaskOpen by remember { mutableStateOf(false) }
    var editTaskId by remember { mutableStateOf<Long?>(null) }
    var taskMenuFor by remember { mutableStateOf<Task?>(null) }

    val goal = state.goal

    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = goal?.title.orEmpty(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (goal != null) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.action_more),
                            )
                        }
                        GoalActionsMenu(
                            expanded = menuExpanded,
                            goal = goal,
                            onDismiss = { menuExpanded = false },
                            onEdit = { menuExpanded = false; editGoalOpen = true },
                            onComplete = { menuExpanded = false; onComplete() },
                            onUncomplete = { menuExpanded = false; onUncomplete() },
                            onArchive = { menuExpanded = false; onArchive() },
                            onDelete = { menuExpanded = false; onDelete() },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (goal != null) {
                AimFAB(
                    onClick = { addTaskParent = null; addTaskOpen = true },
                    icon = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.goal_detail_add_task),
                    text = stringResource(R.string.goal_detail_add_task),
                )
            }
        },
    ) { padding ->
        if (goal == null) {
            if (!state.isLoading) {
                AimEmptyState(
                    modifier = Modifier.padding(padding),
                    emoji = "🗂️",
                    title = stringResource(R.string.goals_empty_active_title),
                    subtitle = stringResource(R.string.goals_empty_active_subtitle),
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            GoalHeader(
                goal = goal,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Spacer(Modifier.padding(top = 8.dp))
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.goal_detail_section_tasks),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (state.taskRoots.isEmpty()) {
                AimEmptyState(
                    emoji = "📝",
                    title = stringResource(R.string.goal_detail_no_tasks_title),
                    subtitle = stringResource(R.string.goal_detail_no_tasks_subtitle),
                )
            } else {
                AimTaskTree(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    roots = state.taskRoots,
                    expandedIds = state.expandedTaskIds,
                    callbacks = TaskTreeCallbacks(
                        onTap = { task -> onOpenTask(task.id) },
                        onToggleCompletion = onToggleTaskCompletion,
                        onToggleExpand = onToggleExpand,
                        onAddSubtask = { task ->
                            addTaskParent = task.id
                            addTaskOpen = true
                        },
                        onMore = { task -> taskMenuFor = task },
                        onReorderSiblings = onReorderSiblings,
                    ),
                )
            }
        }
    }

    if (state.confirmDelete) {
        AimAlertDialog(
            title = stringResource(R.string.goal_detail_delete_title),
            text = stringResource(R.string.goal_detail_delete_message),
            confirmLabel = stringResource(R.string.action_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDeleteDialog,
        )
    }
    if (state.confirmCompleteWithChildren) {
        AimAlertDialog(
            title = stringResource(R.string.goal_detail_complete_with_children_title),
            text = stringResource(R.string.goal_detail_complete_with_children_message),
            confirmLabel = stringResource(R.string.goal_detail_complete_all),
            dismissLabel = stringResource(R.string.goal_detail_complete_only),
            onConfirm = onCompleteAll,
            onDismiss = {
                onCompleteOnly()
                onDismissCompleteDialog()
            },
        )
    }

    if (editGoalOpen) {
        GoalEditBottomSheet(
            mode = GoalEditMode.Edit(goalId = goalId),
            onDismiss = { editGoalOpen = false },
        )
    }
    if (addTaskOpen) {
        TaskEditBottomSheet(
            mode = TaskEditMode.Create(goalId = goalId, parentTaskId = addTaskParent),
            onDismiss = { addTaskOpen = false },
        )
    }
    editTaskId?.let { id ->
        TaskEditBottomSheet(
            mode = TaskEditMode.Edit(taskId = id),
            onDismiss = { editTaskId = null },
        )
    }
    taskMenuFor?.let { task ->
        TaskActionsDialog(
            task = task,
            onDismiss = { taskMenuFor = null },
            onEdit = {
                editTaskId = task.id
                taskMenuFor = null
            },
            onDelete = {
                onDeleteTask(task.id)
                taskMenuFor = null
            },
        )
    }
}

@Composable
private fun GoalHeader(goal: Goal, modifier: Modifier = Modifier) {
    AimCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!goal.emoji.isNullOrEmpty()) {
                Text(text = goal.emoji, fontSize = 36.sp)
                Spacer(Modifier.width(12.dp))
            }
            Text(
                modifier = Modifier.weight(1f),
                text = goal.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (goal.deadline != null) {
            DeadlineRow(deadline = goal.deadline)
        }
        if (!goal.description.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.goal_detail_section_description),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AimMarkdownText(text = goal.description)
        }
    }
}

@Composable
private fun DeadlineRow(deadline: LocalDate) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale("ru"))
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.goal_detail_deadline_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatter.format(deadline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun GoalActionsMenu(
    expanded: Boolean,
    goal: Goal,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
    onUncomplete: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.goal_detail_action_edit)) },
            onClick = onEdit,
            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
        )
        if (goal.status == GoalStatus.IN_PROGRESS) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.goal_detail_action_complete)) },
                onClick = onComplete,
                leadingIcon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.goal_detail_action_uncomplete)) },
                onClick = onUncomplete,
                leadingIcon = { Icon(Icons.Outlined.Replay, contentDescription = null) },
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.goal_detail_action_archive)) },
            onClick = onArchive,
            leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.goal_detail_action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            },
            onClick = onDelete,
            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        )
    }
}

@Composable
private fun TaskActionsDialog(
    task: Task,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(text = task.title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.task_action_edit)) },
                    onClick = onEdit,
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.task_action_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = onDelete,
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}
