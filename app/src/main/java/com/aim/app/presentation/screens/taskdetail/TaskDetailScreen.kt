package com.aim.app.presentation.screens.taskdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskStatus
import com.aim.app.presentation.components.AimAlertDialog
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimFAB
import com.aim.app.presentation.components.AimMarkdownText
import com.aim.app.presentation.components.AimTaskTree
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.components.TaskTreeCallbacks
import com.aim.app.presentation.screens.taskedit.TaskEditBottomSheet
import com.aim.app.presentation.screens.taskedit.TaskEditMode

@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    onOpenBreadcrumb: (Long?) -> Unit,
    onOpenSubtask: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onBack()
    }

    TaskDetailContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onOpenBreadcrumb = onOpenBreadcrumb,
        onOpenSubtask = onOpenSubtask,
        onToggleCompletion = viewModel::onToggleCompletion,
        onToggleExpand = viewModel::onToggleExpand,
        onToggleChildCompletion = viewModel::onToggleChildCompletion,
        onDeleteChild = viewModel::onDeleteChild,
        onReorderSiblings = viewModel::onReorderSiblings,
        onRequestDelete = viewModel::requestDelete,
        onDismissDelete = viewModel::dismissDelete,
        onConfirmDelete = viewModel::confirmDelete,
        taskId = viewModel.taskId,
    )
}

@Composable
private fun TaskDetailContent(
    state: TaskDetailUiState,
    onBack: () -> Unit,
    onOpenBreadcrumb: (Long?) -> Unit,
    onOpenSubtask: (Long) -> Unit,
    onToggleCompletion: () -> Unit,
    onToggleExpand: (Long) -> Unit,
    onToggleChildCompletion: (Task) -> Unit,
    onDeleteChild: (Long) -> Unit,
    onReorderSiblings: (Long?, List<Long>) -> Unit,
    onRequestDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    taskId: Long,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var editOpen by remember { mutableStateOf(false) }
    var addSubtaskParent by remember { mutableStateOf<Long?>(null) }
    var addSubtaskOpen by remember { mutableStateOf(false) }
    var editChildId by remember { mutableStateOf<Long?>(null) }
    var childPendingDelete by remember { mutableStateOf<Task?>(null) }

    val task = state.task

    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = task?.title.orEmpty(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (task != null) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.action_more),
                            )
                        }
                        TaskActionsMenu(
                            expanded = menuExpanded,
                            task = task,
                            onDismiss = { menuExpanded = false },
                            onEdit = { menuExpanded = false; editOpen = true },
                            onToggleComplete = { menuExpanded = false; onToggleCompletion() },
                            onDelete = { menuExpanded = false; onRequestDelete() },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (task != null && task.canHaveSubtasks) {
                AimFAB(
                    onClick = {
                        addSubtaskParent = task.id
                        addSubtaskOpen = true
                    },
                    icon = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.task_detail_add_subtask),
                    text = stringResource(R.string.task_detail_add_subtask),
                )
            }
        },
    ) { padding ->
        if (task == null) {
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BreadcrumbBar(
                breadcrumbs = state.breadcrumbs,
                onClick = { onOpenBreadcrumb(it) },
            )
            TaskHeader(task = task, onToggle = onToggleCompletion)
            if (!task.description.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.task_detail_section_description),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AimMarkdownText(text = task.description)
            }
            Text(
                text = stringResource(R.string.task_detail_section_subtasks),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!task.canHaveSubtasks) {
                Text(
                    text = stringResource(R.string.task_detail_max_depth_reached),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (state.subtaskRoots.isEmpty()) {
                AimEmptyState(
                    emoji = "🪜",
                    title = stringResource(R.string.task_detail_no_subtasks_title),
                    subtitle = stringResource(R.string.task_detail_no_subtasks_subtitle),
                )
            } else {
                AimTaskTree(
                    roots = state.subtaskRoots,
                    expandedIds = state.expandedTaskIds,
                    callbacks = TaskTreeCallbacks(
                        onTap = { onOpenSubtask(it.id) },
                        onToggleCompletion = onToggleChildCompletion,
                        onToggleExpand = onToggleExpand,
                        onAddSubtask = { tapped ->
                            addSubtaskParent = tapped.id
                            addSubtaskOpen = true
                        },
                        onEdit = { editChildId = it.id },
                        onDelete = { childPendingDelete = it },
                        onReorderSiblings = onReorderSiblings,
                    ),
                )
            }
        }
    }

    if (state.confirmDelete) {
        AimAlertDialog(
            title = stringResource(R.string.trash_delete_forever_title),
            text = stringResource(R.string.trash_delete_forever_message),
            confirmLabel = stringResource(R.string.action_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }

    if (editOpen) {
        TaskEditBottomSheet(
            mode = TaskEditMode.Edit(taskId = taskId),
            onDismiss = { editOpen = false },
        )
    }
    if (addSubtaskOpen && task != null) {
        TaskEditBottomSheet(
            mode = TaskEditMode.Create(
                goalId = task.goalId,
                parentTaskId = addSubtaskParent ?: task.id,
            ),
            onDismiss = { addSubtaskOpen = false },
        )
    }
    editChildId?.let { id ->
        TaskEditBottomSheet(
            mode = TaskEditMode.Edit(taskId = id),
            onDismiss = { editChildId = null },
        )
    }
    childPendingDelete?.let { child ->
        AimAlertDialog(
            title = stringResource(R.string.task_delete_confirm_title),
            text = stringResource(R.string.task_delete_confirm_message),
            confirmLabel = stringResource(R.string.action_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = {
                onDeleteChild(child.id)
                childPendingDelete = null
            },
            onDismiss = { childPendingDelete = null },
        )
    }
}

@Composable
private fun BreadcrumbBar(
    breadcrumbs: List<Breadcrumb>,
    onClick: (taskId: Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        breadcrumbs.forEachIndexed { index, crumb ->
            val isLast = index == breadcrumbs.lastIndex
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(enabled = !isLast) { onClick(crumb.taskId) }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                if (!crumb.emoji.isNullOrEmpty()) {
                    Text(text = crumb.emoji, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = crumb.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isLast) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.primary,
                )
            }
            if (!isLast) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TaskHeader(task: Task, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val completed = task.status == TaskStatus.COMPLETED
    AimCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (completed) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                if (completed) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            if (!task.emoji.isNullOrEmpty()) {
                Text(text = task.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                modifier = Modifier.weight(1f),
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (completed) TextDecoration.LineThrough else null,
            )
        }
    }
}

@Composable
private fun TaskActionsMenu(
    expanded: Boolean,
    task: Task,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.task_detail_action_edit)) },
            onClick = onEdit,
            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
        )
        if (task.status == TaskStatus.IN_PROGRESS) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.task_detail_action_complete)) },
                onClick = onToggleComplete,
                leadingIcon = { Icon(Icons.Outlined.Check, contentDescription = null) },
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.task_detail_action_uncomplete)) },
                onClick = onToggleComplete,
                leadingIcon = { Icon(Icons.Outlined.Replay, contentDescription = null) },
            )
        }
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.task_detail_action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            },
            onClick = onDelete,
            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        )
    }
}
