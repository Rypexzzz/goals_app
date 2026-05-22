package com.aim.app.domain.usecase.trash

import com.aim.app.domain.model.TrashItem
import com.aim.app.domain.repository.GoalRepository
import com.aim.app.domain.repository.TaskRepository
import javax.inject.Inject

class EmptyTrashUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(items: List<TrashItem>) {
        items.forEach { item ->
            when (item) {
                is TrashItem.GoalItem -> goalRepository.permanentlyDelete(item.goal.id)
                is TrashItem.TaskItem -> taskRepository.permanentlyDelete(item.task.id)
            }
        }
    }
}
