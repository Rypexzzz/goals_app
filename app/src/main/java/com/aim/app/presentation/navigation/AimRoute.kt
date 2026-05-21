package com.aim.app.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Корневая иерархия типизированных маршрутов приложения.
 *
 * В Sprint 1 определены только верхнеуровневые экраны. Детальные маршруты
 * (`GoalDetail(goalId)`, `TaskDetail(taskId)` и т.д.) добавляются по мере появления
 * экранов в последующих спринтах.
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
}
