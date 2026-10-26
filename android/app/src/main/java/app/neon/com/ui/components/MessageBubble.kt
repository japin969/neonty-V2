package app.neon.com.ui.components

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.Message
import app.neon.com.data.model.MessageStatus
import app.neon.com.data.model.MessageType
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (Message) -> Unit = {},
    onSwipeReply: (Message) -> Unit = {},
    onMediaClick: (url: String, type: MessageType) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val bubbleBg = if (isSelected) {
        Color(0xFF2B5278)
    } else if (isOutgoing) {
        app.neon.com.ui.theme.TelegramBubbleOut
    } else {
        app.neon.com.ui.theme.TelegramBubbleIn
    }
    val timeColor = if (isOutgoing) app.neon.com.ui.theme.TelegramTimeOut else app.neon.com.ui.theme.TelegramTimeIn
    val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    // Swipe to reply offset
    var offsetX by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        if (delta > 0 || offsetX > 0) {
            offsetX = (offsetX + delta).coerceIn(0f, 120f)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (offsetX > 60f) {
                        onSwipeReply(message)
                    }
                    offsetX = 0f
                }
            ),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection checkbox on the left for incoming
        if (isSelectionMode && !isOutgoing) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF5288C1) else Color.Transparent)
                    .border(2.dp, if (isSelected) Color(0xFF5288C1) else Color.Gray, CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        // Main Bubble Box
        Box(
            modifier = Modifier
                .widthIn(min = 80.dp, max = 320.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) onClick() else onClick()
                    },
                    onLongClick = { onLongClick(message) }
                )
                .padding(8.dp)
        ) {
            Column {
                // Reply Quote Preview
                if (!message.replyToText.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .background(Color(0x22FFFFFF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(3.dp).height(24.dp).background(Color(0xFF64B5F6)))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(message.replyToSender ?: "Pesan", color = Color(0xFF64B5F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(message.replyToText, color = Color.White, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }

                // Forwarded from header
                if (!message.forwardFrom.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "↪ Teruskan dari ${message.forwardFrom}",
                            color = Color(0xFF64B5F6),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Message Content per MessageType
                when (message.type) {
                    MessageType.IMAGE -> {
                        val url = if (!message.mediaUrl.isNullOrBlank()) message.mediaUrl else message.text
                        AsyncImage(
                            model = url, contentDescription = "Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onMediaClick(url, MessageType.IMAGE) },
                            contentScale = ContentScale.Crop
                        )
                    }
                    MessageType.VIDEO -> {
                        val url = if (!message.mediaUrl.isNullOrBlank()) message.mediaUrl else message.text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onMediaClick(url, MessageType.VIDEO) }
                        ) {
                            AsyncImage(
                                model = url, contentDescription = "Video Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                    MessageType.VOICE -> {
                        VoiceNotePlayer(message = message)
                    }
                    MessageType.FILE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    val url = message.mediaUrl ?: ""
                                    if (url.startsWith("http")) uriHandler.openUri(url)
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF3B82F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = "File", tint = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(text = message.mediaUrl?.substringAfterLast("/") ?: "Dokumen", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Unduh File", color = Color(0xFF64B5F6), fontSize = 11.sp)
                            }
                        }
                    }
                    MessageType.LOCATION -> {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(message.text)}"))
                                    context.startActivity(mapIntent)
                                }
                                .padding(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Red)
                                Spacer(Modifier.width(6.dp))
                                Text("Lokasi Dibagikan", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(message.text, color = Color(0xFF64B5F6), fontSize = 12.sp)
                        }
                    }
                    MessageType.CONTACT -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Contact", tint = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(message.text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Kartu Kontak", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                    MessageType.STICKER -> {
                        val url = if (!message.mediaUrl.isNullOrBlank()) message.mediaUrl else message.text
                        AsyncImage(
                            model = url,
                            contentDescription = "Sticker",
                            modifier = Modifier.size(140.dp).clickable { onMediaClick(url, MessageType.STICKER) }
                        )
                    }
                    MessageType.GIF -> {
                        val url = if (!message.mediaUrl.isNullOrBlank()) message.mediaUrl else message.text
                        AsyncImage(
                            model = url,
                            contentDescription = "GIF",
                            modifier = Modifier.size(180.dp).clip(RoundedCornerShape(12.dp)).clickable { onMediaClick(url, MessageType.GIF) }
                        )
                    }
                    MessageType.VIDEO_NOTE -> {
                        val url = if (!message.mediaUrl.isNullOrBlank()) message.mediaUrl else message.text
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color(0xFF5288C1), CircleShape)
                                .clickable { onMediaClick(url, MessageType.VIDEO_NOTE) }
                        ) {
                            AsyncImage(
                                model = url, contentDescription = "Video Note",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    else -> {
                        // TEXT with link preview detection
                        Text(text = message.text, color = Color.White, fontSize = 16.sp, lineHeight = 21.sp)

                        // Link preview
                        if (message.text.contains("http://") || message.text.contains("https://")) {
                            val url = message.text.split(" ", "\n").firstOrNull { it.startsWith("http://") || it.startsWith("https://") } ?: ""
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.25f))
                                    .clickable { uriHandler.openUri(url) }
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width(3.dp).height(32.dp).background(Color(0xFF64B5F6)))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(text = url.removePrefix("https://").removePrefix("http://").substringBefore("/"), color = Color(0xFF64B5F6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = url, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Reaction bar
                if (!message.reactions.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0xFF2B5278), RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(message.reactions.values.joinToString(" "), fontSize = 12.sp)
                    }
                }

                // Footer: Time, Self Destruct Timer, Saved star, Sent/Read checkmark
                Row(
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.selfDestructSec != null && message.selfDestructSec > 0) {
                        Icon(Icons.Default.LocalFireDepartment, "Self Destruct", tint = Color(0xFFFF5722), modifier = Modifier.size(12.dp))
                        Text("${message.selfDestructSec}s", color = Color(0xFFFF5722), fontSize = 10.sp)
                        Spacer(Modifier.width(4.dp))
                    }
                    if (message.isSaved) {
                        Icon(Icons.Default.Bookmark, "Saved", tint = Color(0xFF64B5F6), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                    }
                    Text(text = timeFormatted, color = timeColor, fontSize = 11.sp)
                    if (isOutgoing) {
                        Spacer(Modifier.width(3.dp))
                        Icon(
                            imageVector = if (message.status == MessageStatus.READ) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (message.status == MessageStatus.READ) Color(0xFF64B5F6) else timeColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Selection checkbox on the right for outgoing
        if (isSelectionMode && isOutgoing) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF5288C1) else Color.Transparent)
                    .border(2.dp, if (isSelected) Color(0xFF5288C1) else Color.Gray, CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun VoiceNotePlayer(message: Message) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val url = if (!message.mediaUrl.isNullOrBlank()) message.mediaUrl else message.text

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release(); mediaPlayer = null }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                if (isPlaying) { mediaPlayer?.pause(); isPlaying = false }
                else {
                    if (mediaPlayer == null) {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(url); prepare(); setOnCompletionListener { isPlaying = false }
                        }
                    }
                    mediaPlayer?.start(); isPlaying = true
                }
            }
            .padding(8.dp)
    ) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", tint = Color.Black)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = if (isPlaying) "Memutar Audio..." else "Pesan Suara", color = Color.White, fontSize = 14.sp)
            Text(text = "0:14", color = Color.LightGray, fontSize = 11.sp)
        }
    }
}
