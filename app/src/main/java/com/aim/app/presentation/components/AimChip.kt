package com.aim.app.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.aim.app.presentation.theme.AimTheme

@Composable
fun AimChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@PreviewLightDark
@Composable
private fun AimChipPreview() {
    AimTheme {
        AimChip(selected = true, onClick = {}, label = "Активные")
    }
}
