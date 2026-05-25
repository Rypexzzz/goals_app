package com.aim.app.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Материализованный экземпляр регулярной задачи на конкретную дату.
 * Хранится только когда по дате есть действие (отметка) — отсутствие строки = «не отмечено».
 */
data class TaskOccurrence(
    val id: Long,
    val taskId: Long,
    val date: LocalDate,
    val status: TaskStatus,
    val completedAt: Instant?,
) {
    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED
}
