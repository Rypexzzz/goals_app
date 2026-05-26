package com.aim.app.presentation.screens.goals

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalFilter
import com.aim.app.domain.model.GoalStatus
import com.aim.app.presentation.components.AimChip
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimFAB
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.screens.goaledit.GoalEditBottomSheet
import com.aim.app.presentation.screens.goaledit.GoalEditMode
import com.aim.app.presentation.theme.AimTheme
import java.time.Instant
import java.time.LocalDate

@Composable
fun GoalsScreen(
    onSettingsClick: () -> Unit,
    onGoalClick: (Long) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var createSheetOpen by remember { mutableStateOf(false) }

    GoalsScreenContent(
        modifier = modifier,
        state = state,
        onFilterChange = viewModel::onFilterChange,
        onSettingsClick = onSettingsClick,
        onGoalClick = onGoalClick,
        onCreateGoal = { createSheetOpen = true },
        onOpenTrash = onOpenTrash,
        onOpenArchive = onOpenArchive,
    )

    if (createSheetOpen) {
        GoalEditBottomSheet(
            mode = GoalEditMode.Create,
            onDismiss = { createSheetOpen = false },
        )
    }
}

@Composable
private fun GoalsScreenContent(
    state: GoalsUiState,
    onFilterChange: (GoalFilter) -> Unit,
    onSettingsClick: () -> Unit,
    onGoalClick: (Long) -> Unit,
    onCreateGoal: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = stringResource(R.string.goals_title),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreHoriz,
                            contentDescription = stringResource(R.string.action_more),
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                        )
                    }
                    GoalsOverflowMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        onOpenTrash = { menuExpanded = false; onOpenTrash() },
                        onOpenArchive = { menuExpanded = false; onOpenArchive() },
                    )
                },
            )
        },
        floatingActionButton = {
            AimFAB(
                onClick = onCreateGoal,
                icon = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.goals_fab_create),
                text = stringResource(R.string.goals_fab_create),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            FilterRow(
                current = state.filter,
                onFilterChange = onFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (state.goals.isEmpty() && !state.isLoading) {
                AimEmptyState(
                    emoji = state.filter.emoji,
                    title = stringResource(state.filter.emptyTitleRes),
                    subtitle = stringResource(state.filter.emptySubtitleRes),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = state.goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    current: GoalFilter,
    onFilterChange: (GoalFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoalFilter.entries.forEach { filter ->
            AimChip(
                selected = current == filter,
                onClick = { onFilterChange(filter) },
                label = stringResource(filter.labelRes),
            )
        }
    }
}

@Composable
private fun GoalsOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenArchive: () -> Unit,
) {
    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        androidx.compose.material3.DropdownMenuItem(
            text = { androidx.compose.material3.Text(stringResource(R.string.goals_menu_archive)) },
            onClick = onOpenArchive,
        )
        androidx.compose.material3.DropdownMenuItem(
            text = { androidx.compose.material3.Text(stringResource(R.string.goals_menu_trash)) },
            onClick = onOpenTrash,
        )
    }
}

@get:StringRes
private val GoalFilter.labelRes: Int
    get() = when (this) {
        GoalFilter.ALL -> R.string.goals_filter_all
        GoalFilter.ACTIVE -> R.string.goals_filter_active
        GoalFilter.COMPLETED -> R.string.goals_filter_completed
        GoalFilter.ARCHIVED -> R.string.goals_filter_archived
    }

@get:StringRes
private val GoalFilter.emptyTitleRes: Int
    get() = when (this) {
        GoalFilter.ALL -> R.string.goals_empty_all_title
        GoalFilter.ACTIVE -> R.string.goals_empty_active_title
        GoalFilter.COMPLETED -> R.string.goals_empty_completed_title
        GoalFilter.ARCHIVED -> R.string.goals_empty_archived_title
    }

@get:StringRes
private val GoalFilter.emptySubtitleRes: Int
    get() = when (this) {
        GoalFilter.ALL -> R.string.goals_empty_all_subtitle
        GoalFilter.ACTIVE -> R.string.goals_empty_active_subtitle
        GoalFilter.COMPLETED -> R.string.goals_empty_completed_subtitle
        GoalFilter.ARCHIVED -> R.string.goals_empty_archived_subtitle
    }

private val GoalFilter.emoji: String
    get() = when (this) {
        GoalFilter.ALL -> "🎯"
        GoalFilter.ACTIVE -> "🎯"
        GoalFilter.COMPLETED -> "🏆"
        GoalFilter.ARCHIVED -> "📦"
    }

@PreviewLightDark
@Composable
private fun GoalsScreenPreview() {
    AimTheme {
        GoalsScreenContent(
            state = GoalsUiState(
                filter = GoalFilter.ACTIVE,
                isLoading = false,
                goals = listOf(
                    Goal(
                        id = 1,
                        title = "Стать сильнее физически",
                        description = "Тренировки 3 раза в неделю.",
                        emoji = "💪",
                        deadline = LocalDate.now().plusDays(45),
                        status = GoalStatus.IN_PROGRESS,
                        orderIndex = 0,
                        createdAt = Instant.EPOCH,
                        completedAt = null,
                        archivedAt = null,
                        deletedAt = null,
                    ),
                    Goal(
                        id = 2,
                        title = "Прочитать 12 книг за год",
                        description = null,
                        emoji = "📚",
                        deadline = LocalDate.now().plusDays(220),
                        status = GoalStatus.IN_PROGRESS,
                        orderIndex = 1,
                        createdAt = Instant.EPOCH,
                        completedAt = null,
                        archivedAt = null,
                        deletedAt = null,
                    ),
                ),
            ),
            onFilterChange = {},
            onSettingsClick = {},
            onGoalClick = {},
            onCreateGoal = {},
            onOpenTrash = {},
            onOpenArchive = {},
        )
    }
}
