package com.aim.app.presentation.screens.goaldetail

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskNode

data class GoalDetailUiState(
    val isLoading: Boolean = true,
    val goal: Goal? = null,
    val taskRoots: List<TaskNode> = emptyList(),
    val expandedTaskIds: Set<Long> = emptySet(),
    val linkedHabits: List<Habit> = emptyList(),
    /** Незавершённые задачи в дереве — для подсказки при завершении цели. */
    val pendingTasks: List<Task> = emptyList(),
    val confirmDelete: Boolean = false,
    val confirmCompleteWithChildren: Boolean = false,
    val finished: Boolean = false,
)
