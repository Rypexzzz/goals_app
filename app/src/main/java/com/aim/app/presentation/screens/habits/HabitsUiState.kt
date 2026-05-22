package com.aim.app.presentation.screens.habits

import com.aim.app.domain.model.Habit

data class HabitRowItem(
    val habit: Habit,
    val currentStreak: Int,
)

data class HabitsUiState(
    val isLoading: Boolean = true,
    val items: List<HabitRowItem> = emptyList(),
)
