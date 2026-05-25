package com.aim.app.data.local.db.projection

/** Проекция: число задач первого уровня и сколько из них выполнено, на цель. */
data class GoalTaskCount(
    val goalId: Long,
    val total: Int,
    val done: Int,
)
