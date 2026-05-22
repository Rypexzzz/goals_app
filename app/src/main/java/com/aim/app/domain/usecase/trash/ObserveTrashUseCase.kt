package com.aim.app.domain.usecase.trash

import com.aim.app.domain.model.TrashItem
import com.aim.app.domain.repository.GoalRepository
import com.aim.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Унифицированный поток для экрана корзины: удалённые цели + удалённые задачи
 * (для каждой задачи подтягиваем заголовок её цели для отображения).
 *
 * Сортировка — по `deletedAt` убыванию (свежие удаления сверху).
 */
class ObserveTrashUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(): Flow<List<TrashItem>> = combine(
        goalRepository.observeAllIncludingDeleted(),
        taskRepository.observeTrashedTasks(),
    ) { goals, tasks ->
        val trashedGoals = goals.filter { it.isInTrash }
        val goalTitleById = goals.associate { it.id to it.title }
        val goalItems = trashedGoals.map(TrashItem::GoalItem)
        val taskItems = tasks.map { task ->
            TrashItem.TaskItem(
                task = task,
                goalTitle = goalTitleById[task.goalId].orEmpty(),
            )
        }
        (goalItems + taskItems).sortedByDescending { it.deletedAt }
    }
}
