package com.aim.app.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.aim.app.R
import com.aim.app.presentation.theme.AimTheme
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText

/** Read-only markdown с темизацией Material 3. */
@Composable
fun AimMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val content by rememberUpdatedState(text)
    RichText(modifier = modifier) {
        Markdown(content = content)
    }
}

/**
 * Поле ввода markdown с переключателем «Редактировать / Превью».
 * Markdown сохраняется как обычная строка; рендер — через [AimMarkdownText].
 */
@Composable
fun AimMarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    minHeight: androidx.compose.ui.unit.Dp = 120.dp,
) {
    var previewMode by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { previewMode = !previewMode }) {
                Text(
                    text = if (previewMode) stringResource(R.string.markdown_edit) else stringResource(R.string.markdown_preview),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        AnimatedContent(targetState = previewMode, label = "MarkdownEditorMode") { isPreview ->
            if (isPreview) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    AimMarkdownText(
                        text = value.ifBlank { stringResource(R.string.markdown_empty_preview) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = minHeight)
                            .padding(vertical = 12.dp),
                    )
                }
            } else {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight),
                    placeholder = placeholder?.let {
                        @Composable { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AimMarkdownTextPreview() {
    AimTheme {
        AimMarkdownText(
            text = """
                # Заголовок

                Поддерживаются **жирный**, *курсив* и [ссылки](https://example.org).

                - Списки
                - Тоже работают
            """.trimIndent(),
            modifier = Modifier.padding(16.dp),
        )
    }
}
