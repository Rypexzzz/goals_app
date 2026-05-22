package com.aim.app.presentation.screens.habitedit

import com.aim.app.domain.model.HabitFrequency

data class HabitEditUiState(
    val isLoading: Boolean = false,
    val isExisting: Boolean = false,
    val title: String = "",
    val description: String = "",
    val emoji: String? = null,
    val frequency: HabitFrequency = HabitFrequency.Daily,
    val goalId: Long? = null,
    val canSave: Boolean = false,
    val saved: Boolean = false,
)
