package com.aim.app.presentation.screens.taskedit

import com.aim.app.domain.model.Recurrence
import java.time.LocalDate
import java.time.LocalTime

data class TaskEditUiState(
    val isLoading: Boolean = false,
    val isExisting: Boolean = false,
    val title: String = "",
    val description: String = "",
    val emoji: String? = null,
    val scheduledFor: LocalDate? = null,
    val scheduledTime: LocalTime? = null,
    val deadline: LocalDate? = null,
    val recurrence: Recurrence? = null,
    val canSave: Boolean = false,
    val saved: Boolean = false,
)
