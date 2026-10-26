package app.neon.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.ui.theme.TelegramPrimary
import app.neon.com.ui.theme.TelegramSurface
import coil.compose.AsyncImage

val COMMON_EMOJIS = listOf(
    "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
    "😊", "😇", "🙂", "😉", "😌", "😍", "🥰", "😘",
    "😋", "😛", "😜", "🤪", "🤨", "🧐", "🤓", "😎",
    "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁",
    "😢", "😭", "😤", "😠", "😡", "🤬", "🤯", "😳",
    "👍", "👎", "👏", "🙌", "🔥", "❤️", "✨", "🎉"
)

val POPULAR_STICKERS = listOf(
    "https://raw.githubusercontent.com/TelegramMessenger/Telegram-iOS/master/submodules/LegacyComponents/LegacyComponentsResources/Stickers/01.webp",
    "https://raw.githubusercontent.com/TelegramMessenger/Telegram-iOS/master/submodules/LegacyComponents/LegacyComponentsResources/Stickers/02.webp",
    "https://raw.githubusercontent.com/TelegramMessenger/Telegram-iOS/master/submodules/LegacyComponents/LegacyComponentsResources/Stickers/03.webp",
    "https://raw.githubusercontent.com/TelegramMessenger/Telegram-iOS/master/submodules/LegacyComponents/LegacyComponentsResources/Stickers/04.webp",
    "https://raw.githubusercontent.com/TelegramMessenger/Telegram-iOS/master/submodules/LegacyComponents/LegacyComponentsResources/Stickers/05.webp",
    "https://raw.githubusercontent.com/TelegramMessenger/Telegram-iOS/master/submodules/LegacyComponents/LegacyComponentsResources/Stickers/06.webp"
)

val POPULAR_GIFS = listOf(
    "https://media.giphy.com/media/26AHONQ79FdWZhAI0/giphy.gif",
    "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif",
    "https://media.giphy.com/media/3o7TKMt1VVNkHV2PaE/giphy.gif",
    "https://media.giphy.com/media/xT9IgG50Fb7Mi0prBC/giphy.gif"
)

enum class KeyboardTab { EMOJI, STICKER, GIF }

@Composable
fun EmojiKeyboard(
    onEmojiSelected: (String) -> Unit,
    onStickerSelected: (String) -> Unit = {},
    onGifSelected: (String) -> Unit = {},
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(KeyboardTab.EMOJI) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(TelegramSurface)
            .padding(6.dp)
    ) {
        // Tab Selector (Emoji, Sticker, GIF)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TabButton(
                title = "Emoji",
                icon = Icons.Default.Mood,
                isSelected = selectedTab == KeyboardTab.EMOJI,
                onClick = { selectedTab = KeyboardTab.EMOJI }
            )
            Spacer(Modifier.width(12.dp))
            TabButton(
                title = "Stiker",
                icon = Icons.Default.Star,
                isSelected = selectedTab == KeyboardTab.STICKER,
                onClick = { selectedTab = KeyboardTab.STICKER }
            )
            Spacer(Modifier.width(12.dp))
            TabButton(
                title = "GIF",
                icon = Icons.Default.Gif,
                isSelected = selectedTab == KeyboardTab.GIF,
                onClick = { selectedTab = KeyboardTab.GIF }
            )
        }

        // Tab Content
        when (selectedTab) {
            KeyboardTab.EMOJI -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier.weight(1f)
                ) {
                    items(COMMON_EMOJIS) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onBackspaceClick) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Backspace",
                            tint = Color.LightGray
                        )
                    }
                }
            }
            KeyboardTab.STICKER -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(POPULAR_STICKERS) { stickerUrl ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onStickerSelected(stickerUrl) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = stickerUrl,
                                contentDescription = "Sticker",
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }
                }
            }
            KeyboardTab.GIF -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(POPULAR_GIFS) { gifUrl ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onGifSelected(gifUrl) },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = gifUrl,
                                contentDescription = "GIF",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) TelegramPrimary.copy(alpha = 0.25f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) TelegramPrimary else Color.LightGray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = title,
            color = if (isSelected) TelegramPrimary else Color.LightGray,
            fontSize = 12.sp
        )
    }
}
