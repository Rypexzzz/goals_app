package com.aim.app.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aim.app.domain.model.NotificationSettings
import com.aim.app.domain.model.NotificationType
import com.aim.app.domain.model.NotificationTypeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранит [NotificationSettings] одной JSON-строкой в DataStore. Время кодируется
 * минутой дня (0..1439), что компактно и не требует кастомных сериализаторов LocalTime.
 */
@Singleton
class NotificationPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val key = stringPreferencesKey("notification_settings_json")
    private val json = Json { ignoreUnknownKeys = true }

    val settings: Flow<NotificationSettings> = dataStore.data.map { prefs ->
        prefs[key]?.let { decode(it) } ?: NotificationSettings()
    }

    suspend fun update(transform: (NotificationSettings) -> NotificationSettings) {
        dataStore.edit { prefs ->
            val current = prefs[key]?.let { decode(it) } ?: NotificationSettings()
            prefs[key] = encode(transform(current))
        }
    }

    private fun encode(settings: NotificationSettings): String {
        val dto = SettingsDto(
            masterEnabled = settings.masterEnabled,
            perType = settings.perType.mapKeys { it.key.name }.mapValues { (_, v) ->
                TypeDto(v.enabled, v.time?.toSecondOfDay()?.div(60), v.withSound, v.withVibration)
            },
            dndStartMinute = settings.doNotDisturbStart?.toSecondOfDay()?.div(60),
            dndEndMinute = settings.doNotDisturbEnd?.toSecondOfDay()?.div(60),
        )
        return json.encodeToString(SettingsDto.serializer(), dto)
    }

    private fun decode(raw: String): NotificationSettings {
        val dto = runCatching { json.decodeFromString(SettingsDto.serializer(), raw) }
            .getOrNull() ?: return NotificationSettings()
        val perType = NotificationType.entries.associateWith { type ->
            dto.perType[type.name]?.let { t ->
                NotificationTypeSettings(
                    enabled = t.enabled,
                    time = t.timeMinute?.let { LocalTime.ofSecondOfDay(it.toLong() * 60) },
                    withSound = t.withSound,
                    withVibration = t.withVibration,
                )
            } ?: NotificationSettings.defaultFor(type)
        }
        return NotificationSettings(
            masterEnabled = dto.masterEnabled,
            perType = perType,
            doNotDisturbStart = dto.dndStartMinute?.let { LocalTime.ofSecondOfDay(it.toLong() * 60) },
            doNotDisturbEnd = dto.dndEndMinute?.let { LocalTime.ofSecondOfDay(it.toLong() * 60) },
        )
    }

    @Serializable
    private data class SettingsDto(
        val masterEnabled: Boolean = true,
        val perType: Map<String, TypeDto> = emptyMap(),
        val dndStartMinute: Int? = null,
        val dndEndMinute: Int? = null,
    )

    @Serializable
    private data class TypeDto(
        val enabled: Boolean,
        val timeMinute: Int?,
        val withSound: Boolean = true,
        val withVibration: Boolean = true,
    )
}
