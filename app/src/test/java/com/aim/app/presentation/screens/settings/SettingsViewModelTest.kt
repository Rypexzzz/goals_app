package com.aim.app.presentation.screens.settings

import app.cash.turbine.test
import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.repository.ThemeRepository
import com.aim.app.domain.usecase.theme.ObserveThemeModeUseCase
import com.aim.app.domain.usecase.theme.SetThemeModeUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: ThemeRepository = mockk(relaxed = true)
    private val observeThemeMode = ObserveThemeModeUseCase(repository)
    private val setThemeMode = SetThemeModeUseCase(repository)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState reflects repository emissions`() = runTest(dispatcher) {
        val source = MutableStateFlow(ThemeMode.LIGHT)
        every { repository.observeThemeMode() } returns source

        val viewModel = SettingsViewModel(observeThemeMode, setThemeMode)

        viewModel.uiState.test {
            assertEquals(SettingsUiState(ThemeMode.LIGHT), awaitItem())
            source.value = ThemeMode.DARK
            assertEquals(SettingsUiState(ThemeMode.DARK), awaitItem())
            source.value = ThemeMode.SYSTEM
            assertEquals(SettingsUiState(ThemeMode.SYSTEM), awaitItem())
        }
    }

    @Test
    fun `onThemeModeSelected writes through use case`() = runTest(dispatcher) {
        every { repository.observeThemeMode() } returns MutableStateFlow(ThemeMode.LIGHT)
        val viewModel = SettingsViewModel(observeThemeMode, setThemeMode)

        viewModel.onThemeModeSelected(ThemeMode.DARK)

        coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `onThemeModeSelected skips writing when selection matches current state`() = runTest(dispatcher) {
        every { repository.observeThemeMode() } returns MutableStateFlow(ThemeMode.LIGHT)
        val viewModel = SettingsViewModel(observeThemeMode, setThemeMode)

        viewModel.uiState.test {
            assertEquals(SettingsUiState(ThemeMode.LIGHT), awaitItem())
            viewModel.onThemeModeSelected(ThemeMode.LIGHT)
            expectNoEvents()
        }

        coVerify(exactly = 0) { repository.setThemeMode(any()) }
    }
}
