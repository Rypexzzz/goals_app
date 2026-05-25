package com.aim.app.data.repository

import com.aim.app.data.local.db.HabitDao
import com.aim.app.data.local.entity.HabitCheckInEntity
import com.aim.app.data.mapper.toDomain
import com.aim.app.data.mapper.toEntity
import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
    private val clock: () -> Instant = Instant::now,
) : HabitRepository {

    override fun observeActiveHabits(): Flow<List<Habit>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeArchivedHabits(): Flow<List<Habit>> =
        dao.observeArchived().map { list -> list.map { it.toDomain() } }

    override fun observeTrashedHabits(): Flow<List<Habit>> =
        dao.observeTrashed().map { list -> list.map { it.toDomain() } }

    override fun observeAllIncludingDeleted(): Flow<List<Habit>> =
        dao.observeEverything().map { list -> list.map { it.toDomain() } }

    override fun observeHabit(id: Long): Flow<Habit?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeHabitsForGoal(goalId: Long): Flow<List<Habit>> =
        dao.observeForGoal(goalId).map { list -> list.map { it.toDomain() } }

    override fun observeCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>> =
        dao.observeCheckIns(habitId).map { list -> list.map { it.toDomain() } }

    override fun observeCheckInsForHabitInRange(
        habitId: Long,
        startInclusive: LocalDate,
        endInclusive: LocalDate,
    ): Flow<List<HabitCheckIn>> =
        dao.observeCheckInsInRange(habitId, startInclusive.toString(), endInclusive.toString())
            .map { list -> list.map { it.toDomain() } }

    override fun observeAllCheckIns(): Flow<List<HabitCheckIn>> =
        dao.observeAllCheckIns().map { list -> list.map { it.toDomain() } }

    override suspend fun createHabit(habit: Habit): Long {
        val nextOrder = dao.maxOrderIndex() + 1
        val entity = habit.copy(orderIndex = nextOrder).toEntity().copy(id = 0)
        return dao.insert(entity)
    }

    override suspend fun updateHabit(habit: Habit) {
        dao.update(habit.toEntity())
    }

    override suspend fun softDelete(habitId: Long) {
        dao.markDeleted(habitId, clock().toEpochMilli())
    }

    override suspend fun restoreFromTrash(habitId: Long) {
        dao.clearDeleted(habitId)
    }

    override suspend fun archive(habitId: Long) {
        dao.markArchived(habitId, clock().toEpochMilli())
    }

    override suspend fun unarchive(habitId: Long) {
        dao.clearArchived(habitId)
    }

    override suspend fun permanentlyDelete(habitId: Long) {
        dao.deleteById(habitId)
    }

    override suspend fun reorder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> dao.updateOrderIndex(id, index) }
    }

    override suspend fun upsertCheckIn(habitId: Long, date: LocalDate, status: CheckInStatus) {
        val now = clock().toEpochMilli()
        val existing = dao.getCheckIn(habitId, date.toString())
        val entity = HabitCheckInEntity(
            id = existing?.id ?: 0,
            habitId = habitId,
            date = date.toString(),
            status = status.name,
            checkedAt = now,
        )
        dao.upsertCheckIn(entity)
    }

    override suspend fun deleteCheckIn(habitId: Long, date: LocalDate) {
        dao.deleteCheckIn(habitId, date.toString())
    }

    override suspend fun getCheckInStatus(
        habitId: Long,
        date: LocalDate,
    ): com.aim.app.domain.model.CheckInStatus? {
        val raw = dao.getCheckIn(habitId, date.toString())?.status ?: return null
        return runCatching { com.aim.app.domain.model.CheckInStatus.valueOf(raw) }.getOrNull()
    }

    override suspend fun purgeDeletedBefore(threshold: java.time.Instant): Int =
        dao.purgeDeletedBefore(threshold.toEpochMilli())
}
