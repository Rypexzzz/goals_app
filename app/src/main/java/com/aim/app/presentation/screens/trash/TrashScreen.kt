package com.aim.app.presentation.screens.trash

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
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.TrashItem
import com.aim.app.presentation.components.AimAlertDialog
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimEmptyState
import com.aim.app.presentation.components.AimTopBar
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun TrashScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrashContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onRestore = viewModel::onRestore,
        onRequestDelete = viewModel::requestDelete,
        onDismissDelete = viewModel::dismissDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onRequestEmpty = viewModel::requestEmpty,
        onDismissEmpty = viewModel::dismissEmpty,
        onConfirmEmpty = viewModel::confirmEmpty,
    )
}

@Composable
private fun TrashContent(
    state: TrashUiState,
    onBack: () -> Unit,
    onRestore: (TrashItem) -> Unit,
    onRequestDelete: (TrashItem) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onRequestEmpty: () -> Unit,
    onDismissEmpty: () -> Unit,
    onConfirmEmpty: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = stringResource(R.string.trash_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (state.items.isNotEmpty()) {
                        TextButton(onClick = onRequestEmpty) {
                            Text(stringResource(R.string.trash_action_empty))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.items.isEmpty() && !state.isLoading) {
            AimEmptyState(
                modifier = Modifier.padding(padding),
                emoji = "🗑️",
                title = stringResource(R.string.trash_empty_title),
                subtitle = stringResource(R.string.trash_empty_subtitle),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = state.items, key = ::trashItemKey) { item ->
                    TrashRow(
                        item = item,
                        onRestore = { onRestore(item) },
                        onDelete = { onRequestDelete(item) },
                    )
                }
            }
        }
    }

    if (state.pendingDelete != null) {
        AimAlertDialog(
            title = stringResource(R.string.trash_delete_forever_title),
            text = stringResource(R.string.trash_delete_forever_message),
            confirmLabel = stringResource(R.string.action_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete,
        )
    }
    if (state.confirmEmpty) {
        AimAlertDialog(
            title = stringResource(R.string.trash_empty_confirm_title),
            text = stringResource(R.string.trash_empty_confirm_message),
            confirmLabel = stringResource(R.string.action_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = onConfirmEmpty,
            onDismiss = onDismissEmpty,
        )
    }
}

@Composable
private fun TrashRow(
    item: TrashItem,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale("ru"))
    }
    val deletedAtText = remember(item.deletedAt) {
        dateFormatter.format(item.deletedAt.atZone(ZoneId.systemDefault()).toLocalDate())
    }
    AimCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val emoji = when (item) {
                is TrashItem.GoalItem -> item.goal.emoji ?: "🎯"
                is TrashItem.TaskItem -> item.task.emoji ?: "📝"
            }
            Text(text = emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (item) {
                        is TrashItem.GoalItem -> item.goal.title
                        is TrashItem.TaskItem -> item.task.title
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val subtitle = when (item) {
                    is TrashItem.GoalItem -> stringResource(R.string.trash_type_goal)
                    is TrashItem.TaskItem -> stringResource(R.string.trash_in_goal, item.goalTitle)
                }
                Text(
                    text = "$subtitle • $deletedAtText",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.trash_action_delete_forever),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            TextButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(4.dp))
                Text(text = stringResource(R.string.trash_action_restore))
            }
        }
    }
}

private fun trashItemKey(item: TrashItem): String = when (item) {
    is TrashItem.GoalItem -> "g-${item.goal.id}"
    is TrashItem.TaskItem -> "t-${item.task.id}"
}
