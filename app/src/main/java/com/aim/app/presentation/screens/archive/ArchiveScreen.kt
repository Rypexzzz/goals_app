package com.aim.app.presentation.screens.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.Goal
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimTopBar

@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ArchiveContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onOpenGoal = onOpenGoal,
        onUnarchive = viewModel::onUnarchive,
        onDelete = viewModel::onDelete,
    )
}

@Composable
private fun ArchiveContent(
    state: ArchiveUiState,
    onBack: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    onUnarchive: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = stringResource(R.string.archive_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.goals.isEmpty() && !state.isLoading) {
            AimEmptyState(
                modifier = Modifier.padding(padding),
                emoji = "📦",
                title = stringResource(R.string.archive_empty_title),
                subtitle = stringResource(R.string.archive_empty_subtitle),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = state.goals, key = { it.id }) { goal ->
                    ArchiveRow(
                        goal = goal,
                        onOpen = { onOpenGoal(goal.id) },
                        onUnarchive = { onUnarchive(goal.id) },
                        onDelete = { onDelete(goal.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveRow(
    goal: Goal,
    onOpen: () -> Unit,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onOpen,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!goal.emoji.isNullOrEmpty()) {
                Text(text = goal.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!goal.description.isNullOrBlank()) {
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.archive_action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            TextButton(onClick = onUnarchive) {
                Icon(
                    imageVector = Icons.Outlined.Unarchive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(4.dp))
                Text(text = stringResource(R.string.archive_action_unarchive))
            }
        }
    }
}
