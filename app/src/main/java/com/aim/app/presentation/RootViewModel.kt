package com.aim.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.repository.AppPreferencesRepository
import com.aim.app.domain.usecase.theme.ObserveThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Состояние уровня приложения: тема и завершённость онбординга. */
@HiltViewModel
class RootViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase,
    appPreferences: AppPreferencesRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = observeThemeMode().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.LIGHT,
    )

    /** null — ещё не загружено (показываем сплэш-заглушку, чтобы не мигал онбординг). */
    val onboardingCompleted: StateFlow<Boolean?> = appPreferences.observeOnboardingCompleted().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )
}
