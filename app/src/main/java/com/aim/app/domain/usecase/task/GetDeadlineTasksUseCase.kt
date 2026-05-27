package com.aim.app.domain.usecase.task

import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Плоский список задач с дедлайном (из активных целей) без иерархии, отсортированный по близости
 * срока. Выполненные исключаются; просроченные оказываются вверху естественно (самые ранние даты
 * идут первыми).
 */
class GetDeadlineTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(): Flow<List<Task>> =
        taskRepository.observeTasksWithDeadline().map { tasks ->
            tasks
                .filter { it.status != TaskStatus.COMPLETED }
                .sortedBy { it.deadline }
        }
}
