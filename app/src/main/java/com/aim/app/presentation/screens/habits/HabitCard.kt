package com.aim.app.presentation.screens.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitFrequency
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.theme.AimTheme
import java.time.Instant

@Composable
fun HabitCard(
    item: HabitRowItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!item.habit.emoji.isNullOrEmpty()) {
                Text(text = item.habit.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.habit.frequency.toDisplayString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StreakBadge(streak = item.currentStreak)
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = Icons.Outlined.LocalFireDepartment,
            contentDescription = null,
            tint = if (streak > 0) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(0.dp),
        )
        Text(
            text = streak.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (streak > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HabitFrequency.toDisplayString(): String = when (this) {
    HabitFrequency.Daily -> "Каждый день"
    is HabitFrequency.TimesPerWeek -> "${times}× в неделю"
    is HabitFrequency.TimesPerMonth -> "${times}× в месяц"
    is HabitFrequency.SpecificDays -> {
        val names = days.sortedBy { it.value }.joinToString(", ") {
            it.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("ru"))
        }
        names
    }
}

@PreviewLightDark
@Composable
private fun HabitCardPreview() {
    AimTheme {
        HabitCard(
            item = HabitRowItem(
                habit = Habit(
                    id = 1,
                    goalId = null,
                    title = "Не курить",
                    description = null,
                    emoji = "🚭",
                    frequency = HabitFrequency.Daily,
                    orderIndex = 0,
                    createdAt = Instant.EPOCH,
                    archivedAt = null,
                    deletedAt = null,
                ),
                currentStreak = 21,
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
