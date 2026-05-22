package com.aim.app.presentation.screens.goals

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalFilter

data class GoalsUiState(
    val filter: GoalFilter = GoalFilter.ACTIVE,
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
)
