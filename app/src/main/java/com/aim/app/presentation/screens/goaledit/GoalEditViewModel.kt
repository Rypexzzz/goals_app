package com.aim.app.presentation.screens.goaledit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.Goal
import com.aim.app.domain.usecase.goal.CreateGoalUseCase
import com.aim.app.domain.usecase.goal.ObserveGoalUseCase
import com.aim.app.domain.usecase.goal.UpdateGoalUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel(assistedFactory = GoalEditViewModel.Factory::class)
class GoalEditViewModel @AssistedInject constructor(
    @Assisted private val mode: GoalEditMode,
    private val createGoal: CreateGoalUseCase,
    private val updateGoal: UpdateGoalUseCase,
    private val observeGoal: ObserveGoalUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GoalEditUiState(isLoading = mode is GoalEditMode.Edit, isExisting = mode is GoalEditMode.Edit),
    )
    val uiState: StateFlow<GoalEditUiState> = _uiState.asStateFlow()

    private var editingGoal: Goal? = null

    init {
        if (mode is GoalEditMode.Edit) {
            viewModelScope.launch {
                val goal = observeGoal(mode.goalId).firstOrNull()
                if (goal != null) {
                    editingGoal = goal
                    _uiState.value = GoalEditUiState(
                        isLoading = false,
                        isExisting = true,
                        title = goal.title,
                        description = goal.description.orEmpty(),
                        emoji = goal.emoji,
                        deadline = goal.deadline,
                        canSave = goal.title.isNotBlank(),
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update {
        it.copy(title = value, canSave = value.trim().isNotEmpty())
    }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onEmojiChange(value: String?) = _uiState.update { it.copy(emoji = value) }
    fun onDeadlineChange(value: LocalDate?) = _uiState.update { it.copy(deadline = value) }

    fun onSave() {
        val current = _uiState.value
        if (!current.canSave) return
        viewModelScope.launch {
            when (val ctx = mode) {
                GoalEditMode.Create -> {
                    createGoal(
                        title = current.title,
                        description = current.description.takeIf { it.isNotBlank() },
                        emoji = current.emoji,
                        deadline = current.deadline,
                    )
                }
                is GoalEditMode.Edit -> {
                    val original = editingGoal ?: return@launch
                    updateGoal(
                        original.copy(
                            title = current.title,
                            description = current.description.takeIf { it.isNotBlank() },
                            emoji = current.emoji,
                            deadline = current.deadline,
                        ),
                    )
                }
            }
            _uiState.update { it.copy(saved = true) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(mode: GoalEditMode): GoalEditViewModel
    }
}
