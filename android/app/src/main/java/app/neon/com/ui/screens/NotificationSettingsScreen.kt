package app.neon.com.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.AppSettings
import app.neon.com.data.model.NotificationSettings
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notif by remember { mutableStateOf(settings.notifications) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications and Sounds", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            item {
                Text("MESSAGE NOTIFICATIONS", color = TelegramPrimary, fontSize = 13.sp, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                ToggleRow("Private Chats", notif.privateChatsEnabled) {
                    notif = notif.copy(privateChatsEnabled = it)
                    onSaveSettings(settings.copy(notifications = notif))
                }
            }
            item {
                ToggleRow("Groups", notif.groupsEnabled) {
                    notif = notif.copy(groupsEnabled = it)
                    onSaveSettings(settings.copy(notifications = notif))
                }
            }
            item {
                ToggleRow("Channels", notif.channelsEnabled) {
                    notif = notif.copy(channelsEnabled = it)
                    onSaveSettings(settings.copy(notifications = notif))
                }
            }
            item { Divider(color = TelegramDarkDivider) }
            item {
                Text("IN-APP", color = TelegramPrimary, fontSize = 13.sp, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                ToggleRow("Sounds", notif.inAppSounds) {
                    notif = notif.copy(inAppSounds = it)
                    onSaveSettings(settings.copy(notifications = notif))
                }
            }
            item {
                ToggleRow("Vibrate", notif.vibrateEnabled) {
                    notif = notif.copy(vibrateEnabled = it)
                    onSaveSettings(settings.copy(notifications = notif))
                }
            }
            item {
                ToggleRow("LED Notification", notif.ledEnabled) {
                    notif = notif.copy(ledEnabled = it)
                    onSaveSettings(settings.copy(notifications = notif))
                }
            }
            item {
                ToggleRow("Message Preview", notif.inAppPreview) {
                    notif = notif.copy(inAppPreview = it)
                    onSaveSettings(settings.copy(notifications = notif))
                }
            }
        }
    }
}

@Composable
fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
