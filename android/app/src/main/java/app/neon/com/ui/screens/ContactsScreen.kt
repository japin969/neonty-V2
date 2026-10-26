package app.neon.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.model.User
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<User>? = null,
    fileDb: FileDatabaseManager? = null,
    onContactClick: (User) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var localContacts by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(fileDb) {
        if (contacts == null && fileDb != null) {
            localContacts = fileDb.readUsers().sortedBy { it.firstName }
        }
    }

    val activeContacts = remember(contacts, localContacts) {
        (contacts ?: localContacts).sortedBy { it.firstName }
    }

    val groupedContacts = remember(activeContacts) {
        activeContacts.groupBy { it.firstName.firstOrNull()?.uppercaseChar() ?: '#' }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramSurface
                )
            )
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            groupedContacts.forEach { (initial, userList) ->
                item(key = "header_$initial") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TelegramDarkBG)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = initial.toString(),
                            color = TelegramPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                items(userList, key = { it.uid }) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContactClick(user) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = TelegramPrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user.firstName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${user.firstName} ${user.lastName}".trim(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (user.isOnline) "online" else "last seen recently",
                                color = if (user.isOnline) TelegramPrimary else TelegramTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
                }
            }
        }
    }
}
