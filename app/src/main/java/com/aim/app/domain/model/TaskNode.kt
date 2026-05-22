package com.aim.app.domain.model

/**
 * Узел дерева задач для презентационного слоя. Дерево строится из плоского `List<Task>`
 * один раз во ViewModel (см. `GoalDetailViewModel`) и потом пуш-обновляется через StateFlow.
 */
data class TaskNode(
    val task: Task,
    val children: List<TaskNode>,
) {
    val depth: Int get() = task.depth
    val hasChildren: Boolean get() = children.isNotEmpty()

    /** Все потомки в pre-order (без самого узла). */
    fun descendants(): Sequence<Task> = sequence {
        children.forEach { child ->
            yield(child.task)
            yieldAll(child.descendants())
        }
    }
}
