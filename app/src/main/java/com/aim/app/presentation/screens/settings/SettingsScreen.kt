package com.aim.app.presentation.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.R
import com.aim.app.domain.model.ThemeMode
import com.aim.app.presentation.components.AimCard
import com.aim.app.presentation.components.AimTopBar
import com.aim.app.presentation.theme.AimTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenTrash: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onOpenArchive = onOpenArchive,
        onOpenTrash = onOpenTrash,
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onOpenArchive: () -> Unit,
    onOpenTrash: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AimTopBar(
                title = stringResource(R.string.settings_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionLabel(text = stringResource(R.string.settings_section_appearance))
            ThemeSettingsCard(
                currentMode = state.themeMode,
                onModeChange = onThemeModeSelected,
            )
            SectionLabel(text = stringResource(R.string.settings_section_data))
            DataSettingsCard(
                onOpenArchive = onOpenArchive,
                onOpenTrash = onOpenTrash,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(horizontal = 4.dp),
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ThemeSettingsCard(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ThemeMode.entries.forEach { mode ->
                ThemeOptionRow(
                    label = stringResource(mode.labelRes),
                    selected = currentMode == mode,
                    onSelect = { onModeChange(mode) },
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.outline,
            ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DataSettingsCard(
    onOpenArchive: () -> Unit,
    onOpenTrash: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AimCard(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
    ) {
        DataActionRow(
            icon = Icons.Outlined.Archive,
            label = stringResource(R.string.settings_open_archive),
            onClick = onOpenArchive,
        )
        DataActionRow(
            icon = Icons.Outlined.Delete,
            label = stringResource(R.string.settings_open_trash),
            onClick = onOpenTrash,
        )
    }
}

@Composable
private fun DataActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@get:StringRes
private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    AimTheme {
        SettingsScreenContent(
            state = SettingsUiState(themeMode = ThemeMode.LIGHT),
            onBack = {},
            onThemeModeSelected = {},
            onOpenArchive = {},
            onOpenTrash = {},
        )
    }
}
