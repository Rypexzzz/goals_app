package com.aim.app.presentation.screens.goaledit

import java.time.LocalDate

data class GoalEditUiState(
    val isLoading: Boolean = false,
    val isExisting: Boolean = false,
    val title: String = "",
    val description: String = "",
    val emoji: String? = null,
    val deadline: LocalDate? = null,
    val canSave: Boolean = false,
    val saved: Boolean = false,
)
