package com.aim.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aim.app.presentation.theme.AimTheme

/**
 * Круговой индикатор прогресса с анимацией заполнения и подписью в центре.
 *
 * @param progress 0f..1f
 */
@Composable
fun AimProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 8.dp,
    centerLabel: String? = null,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "ProgressRing",
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2
            val arcSize = androidx.compose.ui.geometry.Size(
                width = this.size.width - strokeWidth.toPx(),
                height = this.size.height - strokeWidth.toPx(),
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        if (centerLabel != null) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AimProgressRingPreview() {
    AimTheme {
        AimProgressRing(
            progress = 0.66f,
            centerLabel = "4/6",
            modifier = Modifier.size(72.dp),
        )
    }
}
