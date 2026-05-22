package com.aim.app.presentation.screens.trash

import com.aim.app.domain.model.TrashItem

data class TrashUiState(
    val items: List<TrashItem> = emptyList(),
    val isLoading: Boolean = true,
    val confirmEmpty: Boolean = false,
    val pendingDelete: TrashItem? = null,
)
