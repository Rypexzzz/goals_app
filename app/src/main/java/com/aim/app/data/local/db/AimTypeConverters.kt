package com.aim.app.data.local.db

import androidx.room.TypeConverter
import com.aim.app.domain.model.Recurrence
import kotlinx.serialization.json.Json

class AimTypeConverters {

    @TypeConverter
    fun recurrenceToJson(recurrence: Recurrence?): String? =
        recurrence?.let { Json.encodeToString(Recurrence.serializer(), it) }

    @TypeConverter
    fun recurrenceFromJson(value: String?): Recurrence? =
        value?.let { Json.decodeFromString(Recurrence.serializer(), it) }
}
