package com.aim.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.usecase.theme.ObserveThemeModeUseCase
import com.aim.app.domain.usecase.theme.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = observeThemeMode()
        .map { SettingsUiState(themeMode = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = SettingsUiState(),
        )

    fun onThemeModeSelected(mode: ThemeMode) {
        if (mode == uiState.value.themeMode) return
        viewModelScope.launch { setThemeMode(mode) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
