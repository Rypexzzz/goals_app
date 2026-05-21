package com.aim.app.domain.usecase.theme

import app.cash.turbine.test
import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.repository.ThemeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveThemeModeUseCaseTest {

    private val repository: ThemeRepository = mockk()
    private val sut = ObserveThemeModeUseCase(repository)

    @Test
    fun `emits sequence from repository`() = runTest {
        every { repository.observeThemeMode() } returns flowOf(
            ThemeMode.LIGHT,
            ThemeMode.DARK,
            ThemeMode.SYSTEM,
        )

        sut().test {
            assertEquals(ThemeMode.LIGHT, awaitItem())
            assertEquals(ThemeMode.DARK, awaitItem())
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            awaitComplete()
        }
    }
}
