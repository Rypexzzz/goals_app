package com.aim.app.presentation.screens.taskedit

/** Контекст вызова формы задачи. */
sealed interface TaskEditMode {
    /** Новая задача под цель `goalId` и опционально родительскую задачу `parentTaskId`. */
    data class Create(val goalId: Long, val parentTaskId: Long?) : TaskEditMode

    /** Редактирование существующей. */
    data class Edit(val taskId: Long) : TaskEditMode
}
