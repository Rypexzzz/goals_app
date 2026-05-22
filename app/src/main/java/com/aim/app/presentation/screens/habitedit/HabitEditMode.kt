package com.aim.app.presentation.screens.habitedit

sealed interface HabitEditMode {
    /** Новая привычка, опц. сразу привязана к цели. */
    data class Create(val goalId: Long?) : HabitEditMode

    /** Редактирование существующей. */
    data class Edit(val habitId: Long) : HabitEditMode
}
