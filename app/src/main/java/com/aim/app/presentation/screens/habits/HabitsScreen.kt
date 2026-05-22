package com.aim.app.presentation.screens.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimFAB
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.screens.habitedit.HabitEditBottomSheet
import com.aim.app.presentation.screens.habitedit.HabitEditMode

@Composable
fun HabitsScreen(
    onSettingsClick: () -> Unit,
    onHabitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var createOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = stringResource(R.string.habits_title),
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
        floatingActionButton = {
            AimFAB(
                onClick = { createOpen = true },
                icon = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.habits_fab_create),
                text = stringResource(R.string.habits_fab_create),
            )
        },
    ) { padding ->
        if (state.items.isEmpty() && !state.isLoading) {
            AimEmptyState(
                modifier = Modifier.padding(padding),
                emoji = "🌱",
                title = stringResource(R.string.habits_empty_title),
                subtitle = stringResource(R.string.habits_empty_subtitle),
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = state.items, key = { it.habit.id }) { item ->
                    HabitCard(item = item, onClick = { onHabitClick(item.habit.id) })
                }
            }
        }
    }

    if (createOpen) {
        HabitEditBottomSheet(
            mode = HabitEditMode.Create(goalId = null),
            onDismiss = { createOpen = false },
        )
    }
}
