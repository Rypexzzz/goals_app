package com.aim.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aim.app.domain.model.CheckInStatus
import com.aim.app.presentation.theme.AimTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Месячный календарь привычки. Каждая дата — кружок:
 * - зелёный (success) — `DONE`
 * - коралловый (error) — `FAILED`
 * - прозрачный — нет отметки
 * - сегодняшний день — обводка primary
 *
 * Переключение месяцев — HorizontalPager. Текущий месяц — стартовая страница.
 */
@Composable
fun AimHabitCalendar(
    statusByDate: Map<LocalDate, CheckInStatus>,
    onDayTap: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    monthsBefore: Int = 24,
    monthsAfter: Int = 0,
) {
    val totalPages = monthsBefore + 1 + monthsAfter
    val initialPage = monthsBefore
    val pagerState = rememberPagerState(initialPage = initialPage) { totalPages }
    val coroutineScope = rememberCoroutineScope()

    val anchorMonth = remember(today) { YearMonth.from(today) }

    fun monthForPage(page: Int): YearMonth = anchorMonth.plusMonths((page - initialPage).toLong())

    Column(modifier = modifier.fillMaxWidth()) {
        MonthHeader(
            month = monthForPage(pagerState.currentPage),
            onPrev = {
                coroutineScope.launch {
                    if (pagerState.currentPage > 0) {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            onNext = {
                coroutineScope.launch {
                    if (pagerState.currentPage < totalPages - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
        )
        WeekdayLabelsRow(firstDayOfWeek = firstDayOfWeek)
        HorizontalPager(state = pagerState) { page ->
            MonthGrid(
                month = monthForPage(page),
                statusByDate = statusByDate,
                today = today,
                firstDayOfWeek = firstDayOfWeek,
                onDayTap = onDayTap,
            )
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = "Назад",
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = month.format(formatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Вперёд",
            )
        }
    }
}

@Composable
private fun WeekdayLabelsRow(firstDayOfWeek: DayOfWeek) {
    val labels = remember(firstDayOfWeek) {
        (0..6).map { offset ->
            DayOfWeek.of(((firstDayOfWeek.value - 1 + offset) % 7) + 1)
                .getDisplayName(TextStyle.SHORT, Locale("ru"))
                .uppercase(Locale("ru"))
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)) {
        labels.forEach { label ->
            Text(
                modifier = Modifier.weight(1f),
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    statusByDate: Map<LocalDate, CheckInStatus>,
    today: LocalDate,
    firstDayOfWeek: DayOfWeek,
    onDayTap: (LocalDate) -> Unit,
) {
    val firstOfMonth = month.atDay(1)
    val leadingBlanks = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7)
    val totalDays = month.lengthOfMonth()
    val cells = leadingBlanks + totalDays
    val rows = (cells + 6) / 7

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        for (rowIndex in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = rowIndex * 7 + col
                    val dayNumber = cellIndex - leadingBlanks + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayNumber in 1..totalDays) {
                            val date = month.atDay(dayNumber)
                            CalendarDayCell(
                                date = date,
                                status = statusByDate[date],
                                isToday = date == today,
                                onClick = { onDayTap(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    status: CheckInStatus?,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val successColor = aimSuccessColor()
    val failedColor = MaterialTheme.colorScheme.error
    val baseColor: Color = when (status) {
        CheckInStatus.DONE -> successColor
        CheckInStatus.FAILED -> failedColor
        null -> MaterialTheme.colorScheme.surfaceVariant
    }
    val ringWidth by animateDpAsState(
        targetValue = if (isToday) 2.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "TodayRing",
    )
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(baseColor)
            .border(width = ringWidth, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = if (status != null) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
        )
    }
}

/** Спокойный зелёный из README §7.2 — для отметок «воздержался». */
@Composable
internal fun aimSuccessColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFF8FCD8F) else Color(0xFF4F8A4F)

@PreviewLightDark
@Composable
private fun AimHabitCalendarPreview() {
    AimTheme {
        val today = LocalDate.of(2026, 5, 22)
        val data = mapOf(
            today.minusDays(1) to CheckInStatus.DONE,
            today.minusDays(2) to CheckInStatus.DONE,
            today.minusDays(3) to CheckInStatus.FAILED,
            today.minusDays(4) to CheckInStatus.DONE,
            today.minusDays(8) to CheckInStatus.DONE,
        )
        AimHabitCalendar(
            statusByDate = data,
            onDayTap = {},
            today = today,
            modifier = Modifier.padding(16.dp),
        )
    }
}
