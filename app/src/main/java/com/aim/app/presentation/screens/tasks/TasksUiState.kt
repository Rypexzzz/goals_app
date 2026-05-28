package com.aim.app.presentation.screens.tasks

import com.aim.app.domain.model.Task

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
)
