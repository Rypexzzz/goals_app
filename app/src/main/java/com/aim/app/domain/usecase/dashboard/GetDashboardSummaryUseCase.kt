package com.aim.app.domain.usecase.dashboard

import com.aim.app.domain.model.DashboardSummary
import com.aim.app.domain.model.TodayItem
import com.aim.app.domain.usecase.today.GetTodayItemsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val getTodayItems: GetTodayItemsUseCase,
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<DashboardSummary> =
        getTodayItems(today).map { snapshot ->
            val habitsToday = snapshot.todo.count { it is TodayItem.HabitItem } +
                snapshot.doneToday.count { it is TodayItem.HabitItem }
            DashboardSummary(
                date = today,
                doneToday = snapshot.doneCount,
                totalToday = snapshot.totalCount,
                activeHabitsToday = habitsToday,
            )
        }
}
