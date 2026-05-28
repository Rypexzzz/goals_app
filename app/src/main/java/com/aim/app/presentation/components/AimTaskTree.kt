package com.aim.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aim.app.R
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskNode
import com.aim.app.domain.model.TaskStatus
import com.aim.app.presentation.theme.AimTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.Instant

/**
 * Колбэки дерева задач. Вынесены в отдельный класс, чтобы ViewModel передавала их одним объектом.
 */
data class TaskTreeCallbacks(
    val onTap: (Task) -> Unit,
    val onToggleCompletion: (Task) -> Unit,
    val onToggleExpand: (Long) -> Unit,
    val onAddSubtask: (Task) -> Unit,
    val onEdit: (Task) -> Unit,
    val onDelete: (Task) -> Unit,
    /** Вызывается с упорядоченным списком id братьев одного уровня — для коммита `orderIndex` в БД. */
    val onReorderSiblings: (parentTaskId: Long?, orderedIds: List<Long>) -> Unit,
)

@Composable
fun AimTaskTree(
    roots: List<TaskNode>,
    expandedIds: Set<Long>,
    callbacks: TaskTreeCallbacks,
    modifier: Modifier = Modifier,
    indentPerLevel: Dp = 8.dp,
) {
    val sourceItems = remember(roots, expandedIds) { flattenVisible(roots, expandedIds) }
    var items by remember(sourceItems) { mutableStateOf(sourceItems) }
    // Глубина «корня» показанного дерева. На экране цели это 0, на экране задачи — depth открытой
    // задачи + 1. Отступ считаем относительно этого корня: прямые потомки без отступа, внуки и
    // глубже — с отступом по indentPerLevel при раскрытии через ▾.
    val rootBaseDepth = roots.firstOrNull()?.task?.depth ?: 0

    LaunchedEffect(sourceItems) { items = sourceItems }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = from.index
        val toIndex = to.index
        val src = items.getOrNull(fromIndex) ?: return@rememberReorderableLazyListState
        val dst = items.getOrNull(toIndex) ?: return@rememberReorderableLazyListState
        if (src.task.parentTaskId != dst.task.parentTaskId) {
            // Перетаскивание разрешено только между братьями одного уровня.
            return@rememberReorderableLazyListState
        }
        items = items.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        val siblingIds = items
            .filter { it.task.parentTaskId == src.task.parentTaskId && it.task.goalId == src.task.goalId }
            .map { it.task.id }
        callbacks.onReorderSiblings(src.task.parentTaskId, siblingIds)
    }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(items = items, key = { it.task.id }) { node ->
            ReorderableItem(state = reorderableState, key = node.task.id) { isDragging ->
                TaskRow(
                    node = node,
                    isExpanded = expandedIds.contains(node.task.id),
                    isDragging = isDragging,
                    callbacks = callbacks,
                    dragHandleModifier = Modifier.draggableHandle(),
                    indentPerLevel = indentPerLevel,
                    rootBaseDepth = rootBaseDepth,
                )
            }
        }
    }
}

@Composable
private fun TaskRow(
    node: TaskNode,
    isExpanded: Boolean,
    isDragging: Boolean,
    callbacks: TaskTreeCallbacks,
    dragHandleModifier: Modifier,
    indentPerLevel: Dp = 0.dp,
    rootBaseDepth: Int = 0,
    modifier: Modifier = Modifier,
) {
    val task = node.task
    val completed = task.status == TaskStatus.COMPLETED
    val containerColor = if (isDragging) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    // Прямые «корни» показанного дерева идут без отступа (relativeDepth = 0),
    // глубже — по indentPerLevel на уровень.
    val relativeDepth = (task.depth - rootBaseDepth).coerceAtLeast(0)
    val indentDp = indentPerLevel * relativeDepth

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .padding(start = indentDp)
                .clip(MaterialTheme.shapes.medium)
                .clickable { callbacks.onTap(task) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExpandToggle(
                visible = node.hasChildren,
                expanded = isExpanded,
                onClick = { callbacks.onToggleExpand(task.id) },
            )
            CompletionToggle(
                completed = completed,
                onClick = { callbacks.onToggleCompletion(task) },
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (!task.emoji.isNullOrEmpty()) {
                Text(text = task.emoji, modifier = Modifier.padding(end = 8.dp))
            }
            Text(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (completed) 0.6f else 1f),
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (completed) TextDecoration.LineThrough else null,
            )
            Box {
                var menuOpen by remember { mutableStateOf(false) }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.task_action_more),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    if (task.canHaveSubtasks) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.task_action_add_subtask)) },
                            onClick = { menuOpen = false; callbacks.onAddSubtask(task) },
                            leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.task_action_edit)) },
                        onClick = { menuOpen = false; callbacks.onEdit(task) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.task_action_delete),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = { menuOpen = false; callbacks.onDelete(task) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                    )
                }
            }
            Icon(
                modifier = dragHandleModifier.padding(horizontal = 2.dp).size(18.dp),
                imageVector = Icons.Outlined.DragIndicator,
                contentDescription = stringResource(R.string.task_action_reorder),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExpandToggle(
    visible: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(28.dp), contentAlignment = Alignment.Center) {
        if (visible) {
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "ExpandRotation",
            )
            IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotation),
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CompletionToggle(
    completed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (completed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "CheckScale",
    )
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    color = if (completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (scale > 0f) {
                Icon(
                    modifier = Modifier
                        .size((14 * scale).dp.coerceAtLeast(0.dp)),
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

private fun flattenVisible(roots: List<TaskNode>, expandedIds: Set<Long>): List<TaskNode> {
    val out = mutableListOf<TaskNode>()
    fun walk(nodes: List<TaskNode>) {
        nodes.forEach { node ->
            out += node
            if (expandedIds.contains(node.task.id)) walk(node.children)
        }
    }
    walk(roots)
    return out
}

@PreviewLightDark
@Composable
private fun TaskRowPreview() {
    AimTheme {
        Column {
            TaskRow(
                node = TaskNode(
                    task = Task(
                        id = 1, goalId = 1, parentTaskId = null,
                        title = "Подготовить план тренировок",
                        description = null, emoji = "🏋️",
                        deadline = null, scheduledFor = null, scheduledTime = null,
                        status = TaskStatus.IN_PROGRESS,
                        depth = 0, orderIndex = 0, recurrence = null,
                        createdAt = Instant.EPOCH, completedAt = null, deletedAt = null,
                    ),
                    children = emptyList(),
                ),
                isExpanded = false,
                isDragging = false,
                callbacks = TaskTreeCallbacks(
                    onTap = {}, onToggleCompletion = {}, onToggleExpand = {},
                    onAddSubtask = {}, onEdit = {}, onDelete = {}, onReorderSiblings = { _, _ -> },
                ),
                dragHandleModifier = Modifier,
            )
            Spacer(Modifier.height(8.dp))
            TaskRow(
                node = TaskNode(
                    task = Task(
                        id = 2, goalId = 1, parentTaskId = null,
                        title = "Купить экипировку (выполнено)",
                        description = null, emoji = null,
                        deadline = null, scheduledFor = null, scheduledTime = null,
                        status = TaskStatus.COMPLETED,
                        depth = 0, orderIndex = 1, recurrence = null,
                        createdAt = Instant.EPOCH, completedAt = Instant.EPOCH, deletedAt = null,
                    ),
                    children = emptyList(),
                ),
                isExpanded = false,
                isDragging = false,
                callbacks = TaskTreeCallbacks(
                    onTap = {}, onToggleCompletion = {}, onToggleExpand = {},
                    onAddSubtask = {}, onEdit = {}, onDelete = {}, onReorderSiblings = { _, _ -> },
                ),
                dragHandleModifier = Modifier,
            )
        }
    }
}
