package com.aim.app.presentation.screens.habitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.usecase.goal.ObserveGoalUseCase
import com.aim.app.domain.usecase.habit.ArchiveHabitUseCase
import com.aim.app.domain.usecase.habit.CheckInHabitUseCase
import com.aim.app.domain.usecase.habit.GetHabitStatsUseCase
import com.aim.app.domain.usecase.habit.ObserveCheckInsUseCase
import com.aim.app.domain.usecase.habit.ObserveHabitUseCase
import com.aim.app.domain.usecase.habit.SoftDeleteHabitUseCase
import com.aim.app.domain.usecase.habit.UncheckHabitUseCase
import com.aim.app.presentation.navigation.AimRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeHabit: ObserveHabitUseCase,
    private val observeGoal: ObserveGoalUseCase,
    private val observeCheckIns: ObserveCheckInsUseCase,
    private val getHabitStats: GetHabitStatsUseCase,
    private val checkInHabit: CheckInHabitUseCase,
    private val uncheckHabit: UncheckHabitUseCase,
    private val archiveHabit: ArchiveHabitUseCase,
    private val softDeleteHabit: SoftDeleteHabitUseCase,
) : ViewModel() {

    private val route: AimRoute.HabitDetail = savedStateHandle.toRoute()
    val habitId: Long = route.habitId

    private val confirmDeleteFlow = MutableStateFlow(false)

    val uiState: StateFlow<HabitDetailUiState> = observeHabit(habitId)
        .flatMapLatest { habit ->
            if (habit == null) {
                flowOf(HabitDetailUiState(isLoading = false, finished = true))
            } else {
                val goalFlow = habit.goalId?.let { observeGoal(it) } ?: flowOf(null)
                combine(
                    goalFlow,
                    observeCheckIns(habit.id),
                    confirmDeleteFlow,
                ) { goal, checkIns, confirmDelete ->
                    val stats = getHabitStats(habit, goal, checkIns)
                    val byDate = checkIns.associate { it.date to it.status }
                    HabitDetailUiState(
                        isLoading = false,
                        habit = habit,
                        linkedGoal = goal,
                        stats = stats,
                        statusByDate = byDate,
                        confirmDelete = confirmDelete,
                        finished = habit.deletedAt != null,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitDetailUiState(),
        )

    /**
     * Циклическая отметка по дню: отсутствует → DONE → FAILED → отсутствует.
     */
    fun onDayTap(date: LocalDate) = viewModelScope.launch {
        when (uiState.value.statusByDate[date]) {
            null -> checkInHabit(habitId, date, CheckInStatus.DONE)
            CheckInStatus.DONE -> checkInHabit(habitId, date, CheckInStatus.FAILED)
            CheckInStatus.FAILED -> uncheckHabit(habitId, date)
        }
    }

    fun onMarkTodayDone() = viewModelScope.launch {
        checkInHabit(habitId, LocalDate.now(), CheckInStatus.DONE)
    }

    fun onMarkTodayFailed() = viewModelScope.launch {
        checkInHabit(habitId, LocalDate.now(), CheckInStatus.FAILED)
    }

    fun onArchive() = viewModelScope.launch { archiveHabit(habitId) }

    fun requestDelete() = confirmDeleteFlow.update { true }
    fun dismissDelete() = confirmDeleteFlow.update { false }
    fun confirmDelete() = viewModelScope.launch {
        confirmDeleteFlow.update { false }
        softDeleteHabit(habitId)
    }
}
