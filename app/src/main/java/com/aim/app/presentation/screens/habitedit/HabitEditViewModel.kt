package com.aim.app.presentation.screens.habitedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitFrequency
import com.aim.app.domain.usecase.habit.CreateHabitUseCase
import com.aim.app.domain.usecase.habit.ObserveHabitUseCase
import com.aim.app.domain.usecase.habit.UpdateHabitUseCase
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

@HiltViewModel(assistedFactory = HabitEditViewModel.Factory::class)
class HabitEditViewModel @AssistedInject constructor(
    @Assisted private val mode: HabitEditMode,
    private val createHabit: CreateHabitUseCase,
    private val updateHabit: UpdateHabitUseCase,
    private val observeHabit: ObserveHabitUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HabitEditUiState(
            isLoading = mode is HabitEditMode.Edit,
            isExisting = mode is HabitEditMode.Edit,
            goalId = (mode as? HabitEditMode.Create)?.goalId,
        ),
    )
    val uiState: StateFlow<HabitEditUiState> = _uiState.asStateFlow()

    private var editingHabit: Habit? = null

    init {
        if (mode is HabitEditMode.Edit) {
            viewModelScope.launch {
                val habit = observeHabit(mode.habitId).firstOrNull()
                if (habit != null) {
                    editingHabit = habit
                    _uiState.value = HabitEditUiState(
                        isLoading = false,
                        isExisting = true,
                        title = habit.title,
                        description = habit.description.orEmpty(),
                        emoji = habit.emoji,
                        frequency = habit.frequency,
                        goalId = habit.goalId,
                        canSave = habit.title.isNotBlank(),
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
    fun onFrequencyChange(value: HabitFrequency) = _uiState.update { it.copy(frequency = value) }

    fun onSave() {
        val current = _uiState.value
        if (!current.canSave) return
        viewModelScope.launch {
            when (val ctx = mode) {
                is HabitEditMode.Create -> {
                    createHabit(
                        title = current.title,
                        description = current.description.takeIf { it.isNotBlank() },
                        emoji = current.emoji,
                        frequency = current.frequency,
                        goalId = current.goalId ?: ctx.goalId,
                    )
                }
                is HabitEditMode.Edit -> {
                    val original = editingHabit ?: return@launch
                    updateHabit(
                        original.copy(
                            title = current.title,
                            description = current.description.takeIf { it.isNotBlank() },
                            emoji = current.emoji,
                            frequency = current.frequency,
                        ),
                    )
                }
            }
            _uiState.update { it.copy(saved = true) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(mode: HabitEditMode): HabitEditViewModel
    }
}
