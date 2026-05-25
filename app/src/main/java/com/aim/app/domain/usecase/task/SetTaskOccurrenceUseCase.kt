package com.aim.app.domain.usecase.task

import com.aim.app.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Отметить/снять выполнение экземпляра регулярной задачи на конкретную дату.
 */
class SetTaskOccurrenceUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: Long, date: LocalDate, completed: Boolean) {
        repository.setOccurrenceCompleted(taskId, date, completed)
    }
}
