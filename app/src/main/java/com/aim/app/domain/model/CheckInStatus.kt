package com.aim.app.domain.model

/**
 * Статус отметки привычки на конкретный день.
 *
 * Все привычки негативные (воздержание): `DONE` означает «удержался сегодня»,
 * `FAILED` — «сорвался». Отсутствие отметки не равно `FAILED` (см. README §6.3).
 */
enum class CheckInStatus {
    DONE,
    FAILED,
}
