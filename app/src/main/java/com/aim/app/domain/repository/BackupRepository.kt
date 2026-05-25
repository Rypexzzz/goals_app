package com.aim.app.domain.repository

/**
 * Экспорт/импорт всей пользовательской БД как JSON (README §6.8).
 * Домен работает с непрозрачной строкой; формат — деталь реализации data-слоя.
 */
interface BackupRepository {

    suspend fun exportToJson(): String

    /** Полностью заменяет текущие данные содержимым [json]. Бросает при некорректном формате. */
    suspend fun importFromJson(json: String)
}
