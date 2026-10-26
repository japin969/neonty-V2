// Neont Messenger - CreateGroupScreen.kt
// Form Pembuatan Grup Baru Telegram Android: Nama grup, ikon kamera, pemilihan kontak dengan multi-checkbox
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
import app.neon.com.data.model.User
import app.neon.com.ui.theme.TelegramDarkBackground
import app.neon.com.ui.theme.TelegramDarkDivider
import app.neon.com.ui.theme.TelegramDarkHeader
import app.neon.com.ui.theme.TelegramLightBlue
import app.neon.com.ui.components.TelegramAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    contacts: List<User>,
    onGroupCreated: (groupName: String, selectedMemberUids: List<String>) -> Unit,
    onBackClick: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val selectedUids = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("New Group", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { onGroupCreated(groupName, selectedUids.toList()) },
                        enabled = groupName.isNotBlank() && selectedUids.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Create", tint = TelegramLightBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
            )
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(TelegramDarkHeader).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(TelegramLightBlue), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                TextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = { Text("Enter group name", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("Select Members (${selectedUids.size})", color = TelegramLightBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(contacts, key = { it.uid }) { user ->
                    val isChecked = selectedUids.contains(user.uid)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedUids.remove(user.uid) else selectedUids.add(user.uid)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TelegramAvatar(name = user.firstName, avatarUrl = user.avatarUrl, size = 46)
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${user.firstName} ${user.lastName}".trim(), color = Color.White, fontSize = 15.sp)
                            Text(if (user.isOnline) "online" else "last seen recently", color = Color(0xFF7F92A2), fontSize = 12.sp)
                        }
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (isChecked) selectedUids.remove(user.uid) else selectedUids.add(user.uid)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = TelegramLightBlue)
                        )
                    }
                    Divider(color = TelegramDarkDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
                }
            }
        }
    }
}
