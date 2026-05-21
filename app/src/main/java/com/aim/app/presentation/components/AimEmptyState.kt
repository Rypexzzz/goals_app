package com.aim.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aim.app.presentation.theme.AimTheme

@Composable
fun AimEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (emoji != null) {
            Text(
                text = emoji,
                fontSize = 72.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 320.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 320.dp),
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

@PreviewLightDark
@Composable
private fun AimEmptyStatePreview() {
    AimTheme {
        AimEmptyState(
            emoji = "🌅",
            title = "Сегодня — чистый лист",
            subtitle = "Запланируй задачу или отметь привычку, и она появится здесь.",
        )
    }
}
