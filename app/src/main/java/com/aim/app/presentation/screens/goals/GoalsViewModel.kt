package com.aim.app.presentation.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.GoalFilter
import com.aim.app.domain.usecase.goal.ArchiveGoalUseCase
import com.aim.app.domain.usecase.goal.CompleteGoalUseCase
import com.aim.app.domain.usecase.goal.ObserveGoalsUseCase
import com.aim.app.domain.usecase.goal.ReorderGoalsUseCase
import com.aim.app.domain.usecase.goal.SoftDeleteGoalUseCase
import com.aim.app.domain.usecase.goal.UncompleteGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val observeGoals: ObserveGoalsUseCase,
    private val softDeleteGoal: SoftDeleteGoalUseCase,
    private val archiveGoal: ArchiveGoalUseCase,
    private val completeGoal: CompleteGoalUseCase,
    private val uncompleteGoal: UncompleteGoalUseCase,
    private val reorderGoals: ReorderGoalsUseCase,
) : ViewModel() {

    private val filterFlow = MutableStateFlow(GoalFilter.ACTIVE)

    val uiState: StateFlow<GoalsUiState> = filterFlow
        .flatMapLatest { filter ->
            observeGoals(filter).map { goals ->
                GoalsUiState(filter = filter, goals = goals, isLoading = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GoalsUiState(),
        )

    fun onFilterChange(filter: GoalFilter) {
        filterFlow.value = filter
    }

    fun onDelete(goalId: Long) = viewModelScope.launch { softDeleteGoal(goalId) }
    fun onArchive(goalId: Long) = viewModelScope.launch { archiveGoal(goalId) }
    fun onMarkCompleted(goalId: Long) = viewModelScope.launch { completeGoal(goalId) }
    fun onMarkInProgress(goalId: Long) = viewModelScope.launch { uncompleteGoal(goalId) }
    fun onReorder(orderedIds: List<Long>) = viewModelScope.launch { reorderGoals(orderedIds) }
}
