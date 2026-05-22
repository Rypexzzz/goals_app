package com.aim.app.presentation.screens.goaledit

/** Контекст вызова формы цели: создание новой или редактирование существующей. */
sealed interface GoalEditMode {
    data object Create : GoalEditMode
    data class Edit(val goalId: Long) : GoalEditMode
}
