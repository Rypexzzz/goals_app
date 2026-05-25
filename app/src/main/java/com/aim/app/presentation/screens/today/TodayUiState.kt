package com.aim.app.presentation.screens.today

import com.aim.app.domain.model.TodayItem
import com.aim.app.domain.model.Task
import java.time.LocalDate

data class TodayUiState(
    val isLoading: Boolean = true,
    val date: LocalDate = LocalDate.now(),
    val todo: List<TodayItem> = emptyList(),
    val doneToday: List<TodayItem> = emptyList(),
    val overdueTasks: List<Task> = emptyList(),
    val overdueExpanded: Boolean = false,
) {
    val totalCount: Int get() = todo.size + doneToday.size
    val doneCount: Int get() = doneToday.size
    val progress: Float get() = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount
    val isEmpty: Boolean get() = todo.isEmpty() && doneToday.isEmpty() && overdueTasks.isEmpty()
}
