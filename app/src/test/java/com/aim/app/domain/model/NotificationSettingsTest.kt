package com.aim.app.domain.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalTime

class NotificationSettingsTest {

    @Test
    fun `isActive requires both master and type enabled`() {
        val settings = NotificationSettings(
            masterEnabled = true,
            perType = mapOf(
                NotificationType.MORNING_BRIEF to NotificationTypeSettings(enabled = true, time = LocalTime.of(8, 0)),
            ),
        )
        assertTrue(settings.isActive(NotificationType.MORNING_BRIEF))

        val masterOff = settings.copy(masterEnabled = false)
        assertFalse(masterOff.isActive(NotificationType.MORNING_BRIEF))
    }

    @Test
    fun `dnd window within same day`() {
        val settings = NotificationSettings(
            doNotDisturbStart = LocalTime.of(13, 0),
            doNotDisturbEnd = LocalTime.of(14, 0),
        )
        assertTrue(settings.isWithinDoNotDisturb(LocalTime.of(13, 30)))
        assertFalse(settings.isWithinDoNotDisturb(LocalTime.of(12, 30)))
        assertFalse(settings.isWithinDoNotDisturb(LocalTime.of(14, 30)))
    }

    @Test
    fun `dnd window across midnight`() {
        val settings = NotificationSettings(
            doNotDisturbStart = LocalTime.of(22, 0),
            doNotDisturbEnd = LocalTime.of(7, 0),
        )
        assertTrue(settings.isWithinDoNotDisturb(LocalTime.of(23, 0)))
        assertTrue(settings.isWithinDoNotDisturb(LocalTime.of(2, 0)))
        assertFalse(settings.isWithinDoNotDisturb(LocalTime.of(12, 0)))
    }

    @Test
    fun `no dnd window means never suppressed`() {
        val settings = NotificationSettings()
        assertFalse(settings.isWithinDoNotDisturb(LocalTime.of(3, 0)))
    }
}
