package com.aim.app.presentation.screens.archive

import com.aim.app.domain.model.Goal

data class ArchiveUiState(
    val isLoading: Boolean = true,
    val goals: List<Goal> = emptyList(),
)
