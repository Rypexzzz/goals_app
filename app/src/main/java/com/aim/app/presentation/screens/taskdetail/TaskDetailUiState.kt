package com.aim.app.presentation.screens.taskdetail

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskNode

data class Breadcrumb(
    val taskId: Long?, // null = root (Goal)
    val title: String,
    val emoji: String?,
)

data class TaskDetailUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val goal: Goal? = null,
    val breadcrumbs: List<Breadcrumb> = emptyList(),
    val subtaskRoots: List<TaskNode> = emptyList(),
    val expandedTaskIds: Set<Long> = emptySet(),
    val confirmDelete: Boolean = false,
    val finished: Boolean = false,
)
