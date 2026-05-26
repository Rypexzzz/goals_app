package com.aim.app.presentation.screens.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aim.app.R
import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalStatus
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.theme.AimTheme
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!goal.emoji.isNullOrEmpty()) {
                Text(text = goal.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(14.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                when {
                    goal.status == GoalStatus.COMPLETED -> Text(
                        text = stringResource(R.string.goals_card_completed),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    goal.deadline != null -> DeadlineLabel(deadline = goal.deadline)
                }
            }
        }
    }
}

@Composable
private fun DeadlineLabel(deadline: LocalDate, modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    val daysUntil = ChronoUnit.DAYS.between(today, deadline)
    val color: Color = when {
        daysUntil < 0 -> MaterialTheme.colorScheme.error
        daysUntil < 3 -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        daysUntil < 14 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale("ru"))
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            modifier = Modifier.height(14.dp),
            tint = color,
        )
        Text(
            text = formatter.format(deadline),
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@PreviewLightDark
@Composable
private fun GoalCardPreview() {
    AimTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GoalCard(
                goal = Goal(
                    id = 1,
                    title = "Стать сильнее физически",
                    description = "Тренировки 3 раза в неделю, прогресс по силе и выносливости.",
                    emoji = "💪",
                    deadline = LocalDate.now().plusDays(45),
                    status = GoalStatus.IN_PROGRESS,
                    orderIndex = 0,
                    createdAt = Instant.EPOCH,
                    completedAt = null,
                    archivedAt = null,
                    deletedAt = null,
                ),
                onClick = {},
            )
        }
    }
}
