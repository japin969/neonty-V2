// Neont Messenger - ChannelScreen.kt
// Layar Channel Telegram Broadcast: Post, Subscriber count, View count, dan Mute button di bawah
package app.neon.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.Chat
import app.neon.com.data.model.Message
import app.neon.com.ui.theme.TelegramDarkBackground
import app.neon.com.ui.theme.TelegramDarkHeader
import app.neon.com.ui.theme.TelegramLightBlue
import app.neon.com.ui.components.TelegramAvatar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    channel: Chat,
    messages: List<Message>,
    onBackClick: () -> Unit
) {
    var isMuted by remember { mutableStateOf(channel.isMuted) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TelegramAvatar(name = channel.title, avatarUrl = channel.avatarUrl, size = 40)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(channel.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text("14,250 subscribers", color = Color(0xFF7F92A2), fontSize = 12.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "More", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramDarkHeader)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { isMuted = !isMuted },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isMuted) Color(0xFF38434F) else TelegramLightBlue),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(if (isMuted) "UNMUTE" else "MUTE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(messages, key = { it.msgId }) { msg ->
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF182533))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(channel.title, color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(msg.text, color = Color.White, fontSize = 15.sp, lineHeight = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color(0x99FFFFFF), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("1.2K", color = Color(0x99FFFFFF), fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(time, color = Color(0x99FFFFFF), fontSize = 11.sp)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Share, contentDescription = "Forward", tint = Color(0x99FFFFFF), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
