package com.aim.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Тепловая карта «GitHub contributions»: недели — колонки, дни недели — строки.
 * Цвет ячейки задаётся [colorForDate]; будущие даты и до начала диапазона не рисуются.
 */
@Composable
fun AimHeatmap(
    weeks: Int,
    colorForDate: (LocalDate) -> Color,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    emptyColor: Color = Color.Transparent,
    cellGap: Dp = 3.dp,
    onCellTap: ((LocalDate) -> Unit)? = null,
) {
    val start = remember(today, weeks, firstDayOfWeek) {
        today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).minusWeeks((weeks - 1).toLong())
    }
    val rowHeight = 16.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight * 7 + cellGap * 6)
            .then(
                if (onCellTap != null) {
                    Modifier.pointerInput(start, weeks) {
                        detectTapGestures { offset ->
                            val gapPx = cellGap.toPx()
                            val cell = (size.width + gapPx) / weeks
                            val col = (offset.x / cell).toInt().coerceIn(0, weeks - 1)
                            val row = (offset.y / cell).toInt().coerceIn(0, 6)
                            val date = start.plusDays((col * 7 + row).toLong())
                            if (!date.isAfter(today)) onCellTap(date)
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        val gapPx = cellGap.toPx()
        val cellSize = (size.width - gapPx * (weeks - 1)) / weeks
        for (col in 0 until weeks) {
            for (row in 0 until 7) {
                val date = start.plusDays((col * 7 + row).toLong())
                val color = if (date.isAfter(today)) emptyColor else colorForDate(date)
                val x = col * (cellSize + gapPx)
                val y = row * (cellSize + gapPx)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cellSize * 0.25f),
                )
            }
        }
    }
}

/** Высота для одной строки (привычки) — удобно для расчётов в списке. */
val HeatmapRowApproxHeight: Dp = 16.dp * 7 + 3.dp * 6

/** Количество дней между началом тепловой карты и сегодня (включительно). */
internal fun heatmapDays(weeks: Int): Long = weeks.toLong() * 7

@Suppress("unused")
internal fun daysBetweenInclusive(start: LocalDate, end: LocalDate): Long =
    ChronoUnit.DAYS.between(start, end) + 1
