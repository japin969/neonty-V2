// Neont Messenger - ChatListScreen.kt
// Pixel-Perfect Reverse Engineering of Telegram Android Chat List
package app.neon.com.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.model.Chat
import app.neon.com.data.model.MessageStatus
import app.neon.com.data.model.Story
import app.neon.com.ui.components.TelegramAvatar
import app.neon.com.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chats: List<Chat>? = null,
    stories: List<Story>? = null,
    fileDb: FileDatabaseManager? = null,
    onChatSelected: (chatId: String) -> Unit = {},
    onChatClick: (chatId: String) -> Unit = onChatSelected,
    onStoryClick: (story: Story) -> Unit = {},
    onAddStoryClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNewMessageClick: () -> Unit = {},
    onTogglePin: (chatId: String) -> Unit = {},
    onToggleMute: (chatId: String) -> Unit = {},
    onDeleteChat: (chatId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var localChats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var localStories by remember { mutableStateOf<List<Story>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun triggerRefresh() {
        if (isRefreshing) return
        isRefreshing = true
        scope.launch {
            delay(600)
            if (fileDb != null) {
                localChats = fileDb.readChats()
                localStories = fileDb.readStories()
            }
            isRefreshing = false
        }
    }

    LaunchedEffect(fileDb, chats, stories) {
        if (chats != null) {
            localChats = chats
        } else if (fileDb != null) {
            localChats = fileDb.readChats()
        }

        if (stories != null) {
            localStories = stories
        } else if (fileDb != null) {
            localStories = fileDb.readStories()
        }
    }

    val displayChats = chats ?: localChats
    val displayStories = stories ?: localStories

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = {
                    Text(
                        text = "L neont",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewMessageClick,
                containerColor = TelegramPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New Message",
                    tint = Color.White
                )
            }
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Pull-To-Refresh animated indicator
            if (isRefreshing) {
                item(key = "refreshing_spinner") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TelegramPrimary,
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            }

            // Story Bar (Tinggi 90dp)
            item(key = "story_bar") {
                PixelPerfectStoryBar(
                    stories = displayStories,
                    onStoryClick = onStoryClick,
                    onAddStoryClick = onAddStoryClick
                )
            }

            // Chat Items with Swipe-to-dismiss Actions
            items(displayChats, key = { it.chatId }) { chat ->
                SwipeableChatItem(
                    chat = chat,
                    onClick = { onChatClick(chat.chatId) },
                    onTogglePin = { onTogglePin(chat.chatId) },
                    onToggleMute = { onToggleMute(chat.chatId) },
                    onDeleteChat = { onDeleteChat(chat.chatId) }
                )
                // Divider: 0.5dp thickness, color #101419, start 72dp
                Divider(
                    color = TelegramDivider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }
    }
}

/**
 * Story Bar: Tinggi 90dp. Avatar 56dp (lingkaran).
 * Jika ada story baru, beri border/ring warna TelegramPrimary (#2481CC) setebal 2dp.
 * Nama di bawah avatar ukuran 12sp.
 */
@Composable
fun PixelPerfectStoryBar(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit,
    onAddStoryClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(TelegramDarkBG)
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // First Item: "+ My Story"
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(62.dp)
                    .clickable { onAddStoryClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TelegramSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "My Story",
                        tint = TelegramPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "My Story",
                    color = TelegramTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        items(stories, key = { it.storyId }) { story ->
            val hasNewStory = !story.isViewed
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(62.dp)
                    .clickable { onStoryClick(story) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .then(
                            if (hasNewStory) {
                                Modifier.border(2.dp, TelegramPrimary, CircleShape)
                            } else {
                                Modifier.border(1.dp, TelegramTextSecondary.copy(alpha = 0.4f), CircleShape)
                            }
                        )
                        .padding(if (hasNewStory) 3.dp else 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TelegramAvatar(
                        name = story.userName,
                        avatarUrl = story.mediaUrl,
                        size = 50
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = story.userName.split(" ").firstOrNull() ?: story.userName,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Swipeable Chat Item:
 * Geser kanan (StartToEnd): Pin (biru #2481CC) dan Mute (oranye #FF9800).
 * Geser kiri (EndToStart): Delete (merah #E53935) dan Archive (abu #6B7B8C).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableChatItem(
    chat: Chat,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onDeleteChat: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onTogglePin()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteChat()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bg = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> TelegramPrimary
                SwipeToDismissBoxValue.EndToStart -> TelegramRed
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onTogglePin) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pin",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = onToggleMute) {
                            Icon(
                                imageVector = if (chat.isMuted) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = "Mute",
                                tint = TelegramOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archive",
                            tint = TelegramArchive,
                            modifier = Modifier.size(24.dp)
                        )
                        IconButton(onClick = onDeleteChat) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) {
        PixelPerfectChatItemRow(chat = chat, onClick = onClick)
    }
}

/**
 * Chat Item:
 * Padding kiri-kanan 12dp, atas-bawah 8dp.
 * Avatar 54dp bulat.
 * Nama chat 16sp (FontWeight.Medium, warna putih).
 * Preview pesan 14sp (warna TelegramTextSecondary #8B9DAE).
 * Timestamp 12sp di pojok kanan atas.
 */
@Composable
fun PixelPerfectChatItemRow(
    chat: Chat,
    onClick: () -> Unit
) {
    val timeFormatted = remember(chat.lastMessage?.timestamp) {
        chat.lastMessage?.timestamp?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
        } ?: ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TelegramDarkBG)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar: 54dp bulat
        TelegramAvatar(
            name = chat.title,
            avatarUrl = chat.avatarUrl,
            size = 54
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Baris atas: Nama chat (16sp, Medium, Putih) + Timestamp (12sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = TelegramTextSecondary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = timeFormatted,
                        color = TelegramTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Baris bawah: Preview pesan (14sp, #8B9DAE) + Status Checkmark / Unread Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.lastMessage?.senderId == "me") {
                        val icon = if (chat.lastMessage.status == MessageStatus.READ) {
                            Icons.Default.DoneAll
                        } else {
                            Icons.Default.Check
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TelegramCheckmark,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = chat.lastMessage?.text ?: "No messages yet",
                        color = TelegramTextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isMuted) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "Muted",
                            tint = TelegramTextSecondary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (chat.isMuted) Color(0xFF38434F) else TelegramBadge)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chat.unreadCount}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
