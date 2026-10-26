// Neont Messenger - GroupInfoScreen.kt
// Layar Info & Detail Grup Telegram Android: Avatar, Header, Member count, Add member & Member list
package app.neon.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import app.neon.com.data.model.User
import app.neon.com.ui.theme.TelegramDarkBackground
import app.neon.com.ui.theme.TelegramDarkDivider
import app.neon.com.ui.theme.TelegramDarkHeader
import app.neon.com.ui.theme.TelegramLightBlue
import app.neon.com.ui.components.TelegramAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    group: Chat,
    members: List<User>,
    onBackClick: () -> Unit,
    onAddMemberClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text(group.title, color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().background(TelegramDarkHeader).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TelegramAvatar(name = group.title, avatarUrl = group.avatarUrl, size = 88)
                    Spacer(Modifier.height(14.dp))
                    Text(group.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${members.size} members", color = Color(0xFF7F92A2), fontSize = 13.sp)
                }
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onAddMemberClick() }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(TelegramLightBlue), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Text("Add Members", color = TelegramLightBlue, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Divider(color = TelegramDarkDivider, thickness = 8.dp)

                Text("${members.size} MEMBERS", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
            }

            items(members, key = { it.uid }) { user ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelegramAvatar(name = "${user.firstName} ${user.lastName}", avatarUrl = user.avatarUrl, size = 46, isOnline = user.isOnline)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("${user.firstName} ${user.lastName}".trim(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(if (user.isOnline) "online" else "last seen recently", color = if (user.isOnline) TelegramLightBlue else Color(0xFF7F92A2), fontSize = 12.sp)
                    }
                }
                Divider(color = TelegramDarkDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
            }
        }
    }
}
