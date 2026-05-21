package com.aim.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.ui.graphics.vector.ImageVector
import com.aim.app.R
import kotlin.reflect.KClass

/**
 * Описание элемента нижней навигации: связка маршрута, локализованного лейбла и иконки.
 *
 * `routeClass` нужен для сравнения текущего пункта назначения через
 * `NavDestination.hasRoute(routeClass)` без хрупкого matching по строковому пути.
 */
data class BottomTab(
    val route: AimRoute,
    val routeClass: KClass<out AimRoute>,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

val BottomTabs: List<BottomTab> = listOf(
    BottomTab(
        route = AimRoute.Today,
        routeClass = AimRoute.Today::class,
        labelRes = R.string.bottom_today,
        icon = Icons.Outlined.Checklist,
    ),
    BottomTab(
        route = AimRoute.Goals,
        routeClass = AimRoute.Goals::class,
        labelRes = R.string.bottom_goals,
        icon = Icons.Outlined.Flag,
    ),
    BottomTab(
        route = AimRoute.Habits,
        routeClass = AimRoute.Habits::class,
        labelRes = R.string.bottom_habits,
        icon = Icons.Outlined.Loop,
    ),
    BottomTab(
        route = AimRoute.Dashboard,
        routeClass = AimRoute.Dashboard::class,
        labelRes = R.string.bottom_dashboard,
        icon = Icons.Outlined.Insights,
    ),
)
