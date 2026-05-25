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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aim.app.R
import com.aim.app.presentation.theme.AimTheme

/**
 * Лёгкий read-only рендер Markdown без внешних зависимостей (ADR-0011).
 * Поддерживает: заголовки (#/##/###), **жирный**, *курсив*, маркированные списки (-, *),
 * абзацы. Прочий синтаксис отображается как обычный текст.
 */
@Composable
fun AimMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> Text(
                    text = block.text,
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                )
                is MarkdownBlock.Bullet -> Row {
                    Text(
                        text = "•  ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = renderInline(block.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                is MarkdownBlock.Paragraph -> Text(
                    text = renderInline(block.text),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * Поле ввода markdown с переключателем «Редактировать / Превью».
 */
@Composable
fun AimMarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    minHeight: Dp = 120.dp,
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
                    text = if (previewMode) stringResource(R.string.markdown_edit)
                    else stringResource(R.string.markdown_preview),
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

private sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Bullet(val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
}

private fun parseMarkdownBlocks(raw: String): List<MarkdownBlock> =
    raw.lines()
        .map { it.trimEnd() }
        .filter { it.isNotBlank() }
        .map { line ->
            when {
                line.startsWith("### ") -> MarkdownBlock.Heading(3, line.removePrefix("### "))
                line.startsWith("## ") -> MarkdownBlock.Heading(2, line.removePrefix("## "))
                line.startsWith("# ") -> MarkdownBlock.Heading(1, line.removePrefix("# "))
                line.startsWith("- ") -> MarkdownBlock.Bullet(line.removePrefix("- "))
                line.startsWith("* ") -> MarkdownBlock.Bullet(line.removePrefix("* "))
                else -> MarkdownBlock.Paragraph(line)
            }
        }

// Простая обработка жирного (**текст**) и курсива (*текст* / _текст_) внутри строки.
private fun renderInline(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end > i) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(text[i]); i++
                }
            }
            text[i] == '*' || text[i] == '_' -> {
                val marker = text[i]
                val end = text.indexOf(marker, i + 1)
                if (end > i) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i]); i++
                }
            }
            else -> {
                append(text[i]); i++
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

                Поддерживаются **жирный** и *курсив*.

                - Первый пункт
                - Второй пункт
            """.trimIndent(),
            modifier = Modifier.padding(16.dp),
        )
    }
}
