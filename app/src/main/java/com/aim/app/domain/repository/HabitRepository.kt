package com.aim.app.domain.repository

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitCheckIn
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {

    fun observeActiveHabits(): Flow<List<Habit>>

    fun observeArchivedHabits(): Flow<List<Habit>>

    fun observeTrashedHabits(): Flow<List<Habit>>

    fun observeAllIncludingDeleted(): Flow<List<Habit>>

    fun observeHabit(id: Long): Flow<Habit?>

    fun observeHabitsForGoal(goalId: Long): Flow<List<Habit>>

    fun observeCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>>

    fun observeCheckInsForHabitInRange(
        habitId: Long,
        startInclusive: LocalDate,
        endInclusive: LocalDate,
    ): Flow<List<HabitCheckIn>>

    /** Все отметки всех привычек (для экрана «Сегодня»). */
    fun observeAllCheckIns(): Flow<List<HabitCheckIn>>

    suspend fun createHabit(habit: Habit): Long

    suspend fun updateHabit(habit: Habit)

    suspend fun softDelete(habitId: Long)

    suspend fun restoreFromTrash(habitId: Long)

    suspend fun archive(habitId: Long)

    suspend fun unarchive(habitId: Long)

    suspend fun permanentlyDelete(habitId: Long)

    suspend fun reorder(orderedIds: List<Long>)

    /**
     * Проставить отметку. Если на дату уже есть запись — она заменяется.
     */
    suspend fun upsertCheckIn(habitId: Long, date: LocalDate, status: CheckInStatus)

    /** Удалить отметку за дату (вернуть в состояние «не отмечено»). */
    suspend fun deleteCheckIn(habitId: Long, date: LocalDate)
}
