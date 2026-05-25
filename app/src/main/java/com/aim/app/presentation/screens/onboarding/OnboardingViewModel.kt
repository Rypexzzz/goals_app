package com.aim.app.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.repository.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferences: AppPreferencesRepository,
) : ViewModel() {

    fun completeOnboarding() = viewModelScope.launch {
        appPreferences.setOnboardingCompleted(true)
    }
}
