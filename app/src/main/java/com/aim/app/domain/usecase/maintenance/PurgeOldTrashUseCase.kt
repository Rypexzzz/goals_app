package com.aim.app.domain.usecase.maintenance

import com.aim.app.domain.repository.GoalRepository
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.repository.TaskRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Окончательно удаляет из корзины записи, удалённые раньше, чем [retentionDays] дней назад
 * (README §5.3 — авто-чистка корзины старше 30 дней). Возвращает суммарное число удалённых.
 */
class PurgeOldTrashUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val clock: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(retentionDays: Long = DEFAULT_RETENTION_DAYS): Int {
        val threshold = clock().minus(retentionDays, ChronoUnit.DAYS)
        var purged = 0
        purged += goalRepository.purgeDeletedBefore(threshold)
        purged += taskRepository.purgeDeletedBefore(threshold)
        purged += habitRepository.purgeDeletedBefore(threshold)
        return purged
    }

    companion object {
        const val DEFAULT_RETENTION_DAYS = 30L
    }
}
