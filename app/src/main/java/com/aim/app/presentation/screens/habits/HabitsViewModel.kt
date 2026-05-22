package com.aim.app.presentation.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.usecase.habit.CalculateStreakUseCase
import com.aim.app.domain.usecase.habit.ObserveHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitsViewModel @Inject constructor(
    observeHabits: ObserveHabitsUseCase,
    private val habitRepository: HabitRepository,
    private val calculateStreak: CalculateStreakUseCase,
) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = observeHabits()
        .flatMapLatest { habits ->
            if (habits.isEmpty()) {
                flowOf(HabitsUiState(isLoading = false, items = emptyList()))
            } else {
                // Для каждой привычки подтягиваем её check-ins, считаем streak и собираем
                // обратно в единый кортеж через `combine` всех ID-потоков.
                val streams = habits.map { habit ->
                    habitRepository.observeCheckInsForHabit(habit.id).map { checkIns ->
                        HabitRowItem(
                            habit = habit,
                            currentStreak = calculateStreak(habit.frequency, checkIns).current,
                        )
                    }
                }
                combine(streams) { rowsArray ->
                    HabitsUiState(
                        isLoading = false,
                        items = rowsArray.toList(),
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitsUiState(),
        )
}
