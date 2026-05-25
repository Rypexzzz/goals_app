package com.aim.app.domain.usecase.task

import com.aim.app.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Перенос разовой задачи: snooze на завтра / +N дней / конкретная дата / снять из расписания.
 */
class RescheduleTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: Long, newDate: LocalDate?) {
        repository.rescheduleTask(taskId, newDate)
    }
}
