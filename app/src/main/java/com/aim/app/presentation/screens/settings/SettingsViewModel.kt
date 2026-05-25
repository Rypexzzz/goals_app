package com.aim.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.repository.AppPreferencesRepository
import com.aim.app.domain.usecase.backup.ExportDataUseCase
import com.aim.app.domain.usecase.backup.ImportDataUseCase
import com.aim.app.domain.usecase.theme.ObserveThemeModeUseCase
import com.aim.app.domain.usecase.theme.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
    private val appPreferences: AppPreferencesRepository,
    private val exportData: ExportDataUseCase,
    private val importData: ImportDataUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        observeThemeMode(),
        appPreferences.observeFirstDayOfWeek(),
    ) { theme, firstDay ->
        SettingsUiState(themeMode = theme, firstDayOfWeek = firstDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = SettingsUiState(),
    )

    fun onThemeModeSelected(mode: ThemeMode) {
        if (mode == uiState.value.themeMode) return
        viewModelScope.launch { setThemeMode(mode) }
    }

    fun onFirstDayOfWeekSelected(day: DayOfWeek) {
        viewModelScope.launch { appPreferences.setFirstDayOfWeek(day) }
    }

    /** Возвращает JSON-снимок для записи в выбранный файл. */
    suspend fun buildExportJson(): String = exportData()

    fun importFromJson(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = runCatching { importData(json) }.isSuccess
            onResult(success)
        }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
