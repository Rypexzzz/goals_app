package com.aim.app.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.DashboardPeriod
import com.aim.app.domain.model.GoalProgress
import com.aim.app.domain.model.HabitHeatmap
import com.aim.app.domain.model.PeriodStats
import com.aim.app.domain.model.StreakEntry
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimHeatmap
import com.aim.app.presentation.components.AimProgressRing
import com.aim.app.presentation.components.AimSegmentedControl
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.components.aimSuccessColor

@Composable
fun DashboardScreen(
    onSettingsClick: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    onOpenGoal: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        modifier = modifier,
        state = state,
        onSettingsClick = onSettingsClick,
        onOpenHabit = onOpenHabit,
        onOpenGoal = onOpenGoal,
        onPeriodChange = viewModel::onPeriodChange,
    )
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onSettingsClick: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    onOpenGoal: (Long) -> Unit,
    onPeriodChange: (DashboardPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = stringResource(R.string.dashboard_title),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isEmpty && !state.isLoading) {
            AimEmptyState(
                modifier = Modifier.padding(padding),
                emoji = "📊",
                title = stringResource(R.string.dashboard_empty_title),
                subtitle = stringResource(R.string.dashboard_empty_subtitle),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            state.summary?.let { summary ->
                item(key = "summary") { SummarySection(summary.doneToday, summary.totalToday, summary.progress, summary.activeHabitsToday) }
            }
            if (state.streaks.isNotEmpty()) {
                item(key = "streaks") {
                    StreaksSection(streaks = state.streaks, onOpenHabit = onOpenHabit)
                }
            }
            if (state.heatmaps.isNotEmpty()) {
                item(key = "heatmaps") {
                    HeatmapsSection(heatmaps = state.heatmaps, onOpenHabit = onOpenHabit)
                }
            }
            if (state.goalProgress.isNotEmpty()) {
                item(key = "goals") {
                    GoalsProgressSection(goals = state.goalProgress, onOpenGoal = onOpenGoal)
                }
            }
            state.periodStats?.let { stats ->
                item(key = "period") {
                    PeriodSection(
                        period = state.period,
                        stats = stats,
                        onPeriodChange = onPeriodChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummarySection(done: Int, total: Int, progress: Float, activeHabits: Int) {
    AimCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AimProgressRing(progress = progress, centerLabel = "$done/$total", size = 84.dp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.today_summary_done, done, total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.dashboard_summary_habits, activeHabits),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StreaksSection(streaks: List<StreakEntry>, onOpenHabit: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.dashboard_section_streaks))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = streaks, key = { it.habit.id }) { entry ->
                AimCard(
                    modifier = Modifier.width(140.dp),
                    onClick = { onOpenHabit(entry.habit.id) },
                ) {
                    Text(text = entry.habit.emoji ?: "🌱", fontSize = 28.sp)
                    Text(
                        text = entry.habit.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = entry.currentStreak.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapsSection(heatmaps: List<HabitHeatmap>, onOpenHabit: (Long) -> Unit) {
    val success = aimSuccessColor()
    val failed = MaterialTheme.colorScheme.error
    val empty = MaterialTheme.colorScheme.surfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(stringResource(R.string.dashboard_section_heatmaps))
        heatmaps.forEach { heatmap ->
            AimCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenHabit(heatmap.habit.id) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!heatmap.habit.emoji.isNullOrEmpty()) Text(text = heatmap.habit.emoji, fontSize = 18.sp)
                    Text(
                        text = heatmap.habit.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                AimHeatmap(
                    weeks = 13,
                    colorForDate = { date ->
                        when (heatmap.statusByDate[date]) {
                            CheckInStatus.DONE -> success
                            CheckInStatus.FAILED -> failed
                            null -> empty
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GoalsProgressSection(goals: List<GoalProgress>, onOpenGoal: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.dashboard_section_goals))
        goals.forEach { gp ->
            AimCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenGoal(gp.goal.id) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!gp.goal.emoji.isNullOrEmpty()) Text(text = gp.goal.emoji, fontSize = 18.sp)
                    Text(
                        modifier = Modifier.weight(1f),
                        text = gp.goal.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${gp.doneFirstLevel}/${gp.totalFirstLevel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LinearProgressIndicator(
                    progress = { gp.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PeriodSection(
    period: DashboardPeriod,
    stats: PeriodStats,
    onPeriodChange: (DashboardPeriod) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(stringResource(R.string.dashboard_section_period))
        AimSegmentedControl(
            options = DashboardPeriod.entries,
            selected = period,
            onSelect = onPeriodChange,
            labelOf = { stringResource(it.labelRes) },
            modifier = Modifier.fillMaxWidth(),
        )
        AimCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCell(stats.tasksCompleted.toString(), stringResource(R.string.dashboard_stat_tasks))
                StatCell(stats.habitDone.toString(), stringResource(R.string.dashboard_stat_habit_done))
                StatCell(stats.habitFailed.toString(), stringResource(R.string.dashboard_stat_habit_failed))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCell(stats.bestDayCount.toString(), stringResource(R.string.dashboard_stat_best_day))
                StatCell(
                    value = String.format(java.util.Locale.US, "%.1f", stats.averagePerActiveDay),
                    label = stringResource(R.string.dashboard_stat_avg),
                )
                Spacer(Modifier.width(1.dp))
            }
            val maxCount = stats.productivityByDate.values.maxOrNull() ?: 0
            val primary = MaterialTheme.colorScheme.primary
            val emptyColor = MaterialTheme.colorScheme.surfaceVariant
            val weeks = when (period) {
                DashboardPeriod.WEEK -> 1
                DashboardPeriod.MONTH -> 5
                DashboardPeriod.YEAR -> 53
            }
            AimHeatmap(
                weeks = weeks,
                colorForDate = { date ->
                    val count = stats.productivityByDate[date] ?: 0
                    if (count == 0 || maxCount == 0) {
                        emptyColor
                    } else {
                        primary.copy(alpha = (0.25f + 0.75f * count / maxCount).coerceIn(0.25f, 1f))
                    }
                },
            )
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

private val DashboardPeriod.labelRes: Int
    get() = when (this) {
        DashboardPeriod.WEEK -> R.string.dashboard_period_week
        DashboardPeriod.MONTH -> R.string.dashboard_period_month
        DashboardPeriod.YEAR -> R.string.dashboard_period_year
    }
