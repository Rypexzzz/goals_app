package com.aim.app.domain.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek

/**
 * Сериализатор `java.time.DayOfWeek` для kotlinx.serialization — пишет ISO-номер дня (1..7).
 * Используется в [com.aim.app.domain.model.HabitFrequency.SpecificDays].
 */
object DayOfWeekSerializer : KSerializer<DayOfWeek> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DayOfWeek", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: DayOfWeek) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): DayOfWeek =
        DayOfWeek.of(decoder.decodeInt())
}
