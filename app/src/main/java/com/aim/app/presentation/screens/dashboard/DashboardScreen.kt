package com.aim.app.presentation.screens.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.theme.AimTheme

@Composable
fun DashboardScreen(
    onSettingsClick: () -> Unit,
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
        AimEmptyState(
            modifier = Modifier.padding(padding),
            emoji = "📊",
            title = stringResource(R.string.dashboard_empty_title),
            subtitle = stringResource(R.string.dashboard_empty_subtitle),
        )
    }
}

@PreviewLightDark
@Composable
private fun DashboardScreenPreview() {
    AimTheme {
        DashboardScreen(onSettingsClick = {})
    }
}
