package com.aim.app.presentation.screens.habitdetail

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitStats
import java.time.LocalDate

data class HabitDetailUiState(
    val isLoading: Boolean = true,
    val habit: Habit? = null,
    val linkedGoal: Goal? = null,
    val stats: HabitStats? = null,
    val statusByDate: Map<LocalDate, CheckInStatus> = emptyMap(),
    val confirmDelete: Boolean = false,
    val finished: Boolean = false,
)
