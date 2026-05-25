package com.aim.app.presentation.screens.dashboard

import com.aim.app.domain.model.DashboardPeriod
import com.aim.app.domain.model.DashboardSummary
import com.aim.app.domain.model.GoalProgress
import com.aim.app.domain.model.HabitHeatmap
import com.aim.app.domain.model.PeriodStats
import com.aim.app.domain.model.StreakEntry

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val streaks: List<StreakEntry> = emptyList(),
    val heatmaps: List<HabitHeatmap> = emptyList(),
    val goalProgress: List<GoalProgress> = emptyList(),
    val periodStats: PeriodStats? = null,
    val period: DashboardPeriod = DashboardPeriod.WEEK,
) {
    val isEmpty: Boolean
        get() = streaks.isEmpty() && heatmaps.isEmpty() && goalProgress.isEmpty() &&
            (summary?.totalToday ?: 0) == 0
}
