package com.aim.app.presentation.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.TodayItem
import com.aim.app.domain.usecase.habit.CheckInHabitUseCase
import com.aim.app.domain.usecase.habit.UncheckHabitUseCase
import com.aim.app.domain.usecase.task.CompleteTaskUseCase
import com.aim.app.domain.usecase.task.RescheduleTaskUseCase
import com.aim.app.domain.usecase.task.SetTaskOccurrenceUseCase
import com.aim.app.domain.usecase.task.SoftDeleteTaskUseCase
import com.aim.app.domain.usecase.task.UncompleteTaskUseCase
import com.aim.app.domain.usecase.today.GetTodayItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    getTodayItems: GetTodayItemsUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val uncompleteTask: UncompleteTaskUseCase,
    private val setTaskOccurrence: SetTaskOccurrenceUseCase,
    private val checkInHabit: CheckInHabitUseCase,
    private val uncheckHabit: UncheckHabitUseCase,
    private val rescheduleTask: RescheduleTaskUseCase,
    private val softDeleteTask: SoftDeleteTaskUseCase,
) : ViewModel() {

    private val today = LocalDate.now()
    private val overdueExpanded = MutableStateFlow(false)

    val uiState: StateFlow<TodayUiState> = combine(
        getTodayItems(today),
        overdueExpanded,
    ) { snapshot, expanded ->
        TodayUiState(
            isLoading = false,
            date = today,
            todo = snapshot.todo,
            doneToday = snapshot.doneToday,
            overdueTasks = snapshot.overdueTasks,
            overdueExpanded = expanded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
    )

    fun onToggle(item: TodayItem) = viewModelScope.launch {
        when (item) {
            is TodayItem.TaskItem -> {
                if (item.isRecurringInstance) {
                    setTaskOccurrence(item.task.id, today, completed = !item.isDone)
                } else {
                    if (item.isDone) uncompleteTask(item.task.id) else completeTask(item.task.id)
                }
            }
            is TodayItem.HabitItem -> {
                if (item.status == CheckInStatus.DONE) {
                    uncheckHabit(item.habit.id, today)
                } else {
                    checkInHabit(item.habit.id, today, CheckInStatus.DONE)
                }
            }
        }
    }

    fun onMarkHabitFailed(item: TodayItem.HabitItem) = viewModelScope.launch {
        checkInHabit(item.habit.id, today, CheckInStatus.FAILED)
    }

    fun onSnoozeToTomorrow(taskId: Long) = viewModelScope.launch {
        rescheduleTask(taskId, today.plusDays(1))
    }

    fun onSnoozePlusDays(taskId: Long, days: Long) = viewModelScope.launch {
        rescheduleTask(taskId, today.plusDays(days))
    }

    fun onReschedule(taskId: Long, date: LocalDate?) = viewModelScope.launch {
        rescheduleTask(taskId, date)
    }

    fun onMoveOverdueToToday(taskId: Long) = viewModelScope.launch {
        rescheduleTask(taskId, today)
    }

    fun onDeleteTask(taskId: Long) = viewModelScope.launch { softDeleteTask(taskId) }

    fun toggleOverdueExpanded() = overdueExpanded.update { !it }
}
