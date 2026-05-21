package com.aim.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.aim.app.presentation.theme.AimTheme

@Composable
fun AimFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    expanded: Boolean = true,
) {
    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            expanded = expanded,
            icon = { Icon(imageVector = icon, contentDescription = contentDescription) },
            text = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                )
            },
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

@PreviewLightDark
@Composable
private fun AimFabPreview() {
    AimTheme {
        AimFAB(
            onClick = {},
            icon = Icons.Outlined.Add,
            contentDescription = "Создать",
            text = "Новая цель",
        )
    }
}
