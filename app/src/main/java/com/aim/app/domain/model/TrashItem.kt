package com.aim.app.domain.model

import java.time.Instant

/**
 * Унифицированный элемент корзины. Тащит исходную сущность плюс мета-инфо,
 * необходимую для отрисовки строки списка и для авто-чистки.
 */
sealed class TrashItem {
    abstract val deletedAt: Instant

    data class GoalItem(
        val goal: Goal,
    ) : TrashItem() {
        override val deletedAt: Instant = goal.deletedAt
            ?: error("Goal in trash must have deletedAt")
    }

    data class TaskItem(
        val task: Task,
        val goalTitle: String,
    ) : TrashItem() {
        override val deletedAt: Instant = task.deletedAt
            ?: error("Task in trash must have deletedAt")
    }
}
