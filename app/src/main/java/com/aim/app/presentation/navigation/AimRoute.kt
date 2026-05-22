package com.aim.app.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Корневая иерархия типизированных маршрутов приложения. В Sprint 2 добавлены
 * детальные экраны цели/задачи плюс корзина и архив.
 */
sealed interface AimRoute {

    @Serializable
    data object Today : AimRoute

    @Serializable
    data object Goals : AimRoute

    @Serializable
    data object Habits : AimRoute

    @Serializable
    data object Dashboard : AimRoute

    @Serializable
    data object Settings : AimRoute

    @Serializable
    data class GoalDetail(val goalId: Long) : AimRoute

    @Serializable
    data class TaskDetail(val taskId: Long) : AimRoute

    @Serializable
    data class HabitDetail(val habitId: Long) : AimRoute

    @Serializable
    data object Trash : AimRoute

    @Serializable
    data object Archive : AimRoute
}
