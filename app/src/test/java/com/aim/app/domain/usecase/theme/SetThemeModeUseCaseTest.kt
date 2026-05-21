package com.aim.app.domain.usecase.theme

import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.repository.ThemeRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SetThemeModeUseCaseTest {

    private val repository: ThemeRepository = mockk(relaxed = true)
    private val sut = SetThemeModeUseCase(repository)

    @ParameterizedTest
    @EnumSource(ThemeMode::class)
    fun `forwards every theme mode to repository`(mode: ThemeMode) = runTest {
        sut(mode)
        coVerify(exactly = 1) { repository.setThemeMode(mode) }
    }

    @Test
    fun `each invocation triggers exactly one write`() = runTest {
        sut(ThemeMode.DARK)
        sut(ThemeMode.LIGHT)

        coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.DARK) }
        coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.LIGHT) }
    }
}
