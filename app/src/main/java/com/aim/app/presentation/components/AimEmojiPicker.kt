package com.aim.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aim.app.R
import com.aim.app.presentation.theme.AimTheme

/**
 * Курированный набор эмодзи. См. ADR-0014: поиск отложен, ~120 эмодзи по 8 категориям
 * покрывают подавляющее большинство жизненных целей и привычек одного пользователя.
 */
private object AimEmojiCatalog {
    data class Category(val labelRes: Int, val emojis: List<String>)

    val categories: List<Category> = listOf(
        Category(R.string.emoji_category_goals, listOf("🎯", "🏆", "🥇", "🌟", "⭐", "💎", "🚀", "🧭", "🗺️", "🎓", "📚", "💡", "🔥")),
        Category(R.string.emoji_category_body, listOf("🏃", "🧘", "💪", "🚴", "🏋️", "⚽", "🏀", "🎾", "🏊", "🧗", "🤸", "🛌", "💧", "🥗", "🍎", "🥦")),
        Category(R.string.emoji_category_mind, listOf("📖", "✍️", "🧠", "📝", "📓", "🖋️", "📅", "⏰", "🎧", "🎵", "🎬", "🎨", "🧩", "♟️")),
        Category(R.string.emoji_category_work, listOf("💼", "💻", "📊", "📈", "📉", "🗂️", "🗓️", "📞", "✉️", "🛠️", "🔧", "⚙️", "🏗️")),
        Category(R.string.emoji_category_home, listOf("🏠", "🛏️", "🍳", "🧹", "🧺", "🛁", "🪴", "🌱", "🐶", "🐱", "🪞")),
        Category(R.string.emoji_category_money, listOf("💰", "💸", "💳", "🏦", "📈", "💵", "🪙")),
        Category(R.string.emoji_category_lifestyle, listOf("☕", "🍵", "🍷", "🚭", "🚫", "🧘", "🌅", "🌙", "🌳", "🌊", "🏞️", "✈️", "🎒")),
        Category(R.string.emoji_category_relations, listOf("❤️", "👨‍👩‍👧", "💬", "🤝", "🎁", "📷", "🍽️", "🥂", "🌹")),
    )
}

@Composable
fun AimEmojiPicker(
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        EmojiPickerContent(
            onPick = onPick,
            onClose = onDismiss,
        )
    }
}

@Composable
private fun EmojiPickerContent(
    onPick: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    val category = AimEmojiCatalog.categories[selectedCategoryIndex]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.emoji_picker_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.action_close),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(category.emojis) { emoji ->
                EmojiCell(emoji = emoji, onClick = { onPick(emoji) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AimEmojiCatalog.categories.forEachIndexed { index, cat ->
                val selected = index == selectedCategoryIndex
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { selectedCategoryIndex = index }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    text = stringResource(cat.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmojiCell(
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 22.sp, textAlign = TextAlign.Center)
    }
}

@PreviewLightDark
@Composable
private fun EmojiPickerContentPreview() {
    AimTheme {
        EmojiPickerContent(onPick = {}, onClose = {})
    }
}
