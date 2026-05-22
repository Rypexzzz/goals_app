package com.aim.app.presentation.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.GoalFilter
import com.aim.app.domain.usecase.goal.ObserveGoalsUseCase
import com.aim.app.domain.usecase.goal.SoftDeleteGoalUseCase
import com.aim.app.domain.usecase.goal.UnarchiveGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    observeGoals: ObserveGoalsUseCase,
    private val unarchive: UnarchiveGoalUseCase,
    private val softDelete: SoftDeleteGoalUseCase,
) : ViewModel() {

    val uiState: StateFlow<ArchiveUiState> = observeGoals(GoalFilter.ARCHIVED)
        .map { ArchiveUiState(isLoading = false, goals = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArchiveUiState(),
        )

    fun onUnarchive(goalId: Long) = viewModelScope.launch { unarchive(goalId) }
    fun onDelete(goalId: Long) = viewModelScope.launch { softDelete(goalId) }
}
