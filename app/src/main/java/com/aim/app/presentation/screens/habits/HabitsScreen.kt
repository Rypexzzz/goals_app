package com.aim.app.presentation.screens.habits

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.aim.app.R
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimFAB
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.theme.AimTheme

@Composable
fun HabitsScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCreateHabit: () -> Unit = {},
) {
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
                onClick = onCreateHabit,
                icon = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.habits_fab_create),
                text = stringResource(R.string.habits_fab_create),
            )
        },
    ) { padding ->
        AimEmptyState(
            modifier = Modifier.padding(padding),
            emoji = "🌱",
            title = stringResource(R.string.habits_empty_title),
            subtitle = stringResource(R.string.habits_empty_subtitle),
        )
    }
}

@PreviewLightDark
@Composable
private fun HabitsScreenPreview() {
    AimTheme {
        HabitsScreen(onSettingsClick = {})
    }
}
