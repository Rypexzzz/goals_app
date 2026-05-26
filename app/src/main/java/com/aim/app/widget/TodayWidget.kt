package com.aim.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.aim.app.MainActivity
import com.aim.app.domain.model.TodayItem
import com.aim.app.domain.model.TodaySnapshot
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Большой виджет «Сегодня» (README §9). Показывает дату, сводку и список задач/привычек
 * с чекбоксами; тап по чекбоксу отмечает выполнение без открытия приложения.
 */
class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        val snapshot = entryPoint.getTodayItemsUseCase().invoke().first()

        provideContent {
            GlanceTheme {
                WidgetBody(snapshot)
            }
        }
    }

    @Composable
    private fun WidgetBody(snapshot: TodaySnapshot) {
        val items = (snapshot.todo + snapshot.doneToday).take(MAX_ROWS)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(20.dp)
                .padding(16.dp),
        ) {
            HeaderRow(snapshot.doneCount, snapshot.totalCount)
            Spacer(GlanceModifier.height(8.dp))
            if (items.isEmpty()) {
                Text(
                    text = "Сегодня — чистый лист 🌅",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                )
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(items, itemId = { it.stableKey.hashCode().toLong() }) { item ->
                        WidgetRow(item)
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderRow(done: Int, total: Int) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatDate(LocalDate.now()),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onBackground,
                ),
                modifier = GlanceModifier.defaultWeight(),
            )
            Text(
                text = "$done/$total",
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.primary,
                ),
            )
        }
    }

    @Composable
    private fun WidgetRow(item: TodayItem) {
        val context = LocalContext.current
        // Без вложенных clickable: чекбокс отмечает выполнение, тап по тексту открывает приложение.
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckBox(
                checked = item.isDone,
                onCheckedChange = actionRunCallback<ToggleWidgetItemAction>(
                    actionParametersOf(ToggleWidgetItemAction.itemKeyParam to item.stableKey),
                ),
            )
            Spacer(GlanceModifier.width(6.dp))
            val prefix = if (item.emoji != null) "${item.emoji} " else ""
            Text(
                text = prefix + item.title,
                maxLines = 2,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                ),
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            )
        }
    }

    private fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru")))
            .replaceFirstChar { it.uppercase() }

    private companion object {
        const val MAX_ROWS = 10
    }
}
