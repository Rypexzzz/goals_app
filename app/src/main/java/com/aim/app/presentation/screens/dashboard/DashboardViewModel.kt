package com.aim.app.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.DashboardPeriod
import com.aim.app.domain.usecase.dashboard.GetActiveStreaksUseCase
import com.aim.app.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.aim.app.domain.usecase.dashboard.GetGoalProgressUseCase
import com.aim.app.domain.usecase.dashboard.GetHabitHeatmapsUseCase
import com.aim.app.domain.usecase.dashboard.GetPeriodStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardSummary: GetDashboardSummaryUseCase,
    getActiveStreaks: GetActiveStreaksUseCase,
    getHabitHeatmaps: GetHabitHeatmapsUseCase,
    getGoalProgress: GetGoalProgressUseCase,
    getPeriodStats: GetPeriodStatsUseCase,
) : ViewModel() {

    private val periodFlow = MutableStateFlow(DashboardPeriod.WEEK)

    private val periodStatsFlow = periodFlow.flatMapLatest { getPeriodStats(it) }

    val uiState: StateFlow<DashboardUiState> = combine(
        getDashboardSummary(),
        getActiveStreaks(),
        getHabitHeatmaps(),
        getGoalProgress(),
        periodStatsFlow,
    ) { summary, streaks, heatmaps, goalProgress, periodStats ->
        DashboardUiState(
            isLoading = false,
            summary = summary,
            streaks = streaks,
            heatmaps = heatmaps,
            goalProgress = goalProgress,
            periodStats = periodStats,
            period = periodStats.period,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    fun onPeriodChange(period: DashboardPeriod) {
        periodFlow.value = period
    }
}
