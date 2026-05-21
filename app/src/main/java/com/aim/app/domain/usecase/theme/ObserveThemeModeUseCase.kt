package com.aim.app.domain.usecase.theme

import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThemeModeUseCase @Inject constructor(
    private val repository: ThemeRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = repository.observeThemeMode()
}
