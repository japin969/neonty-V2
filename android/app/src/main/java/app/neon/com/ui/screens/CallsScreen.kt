package app.neon.com.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.model.CallLog
import app.neon.com.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    calls: List<CallLog>? = null,
    fileDb: FileDatabaseManager? = null,
    onCallClick: (CallLog) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var localCalls by remember { mutableStateOf<List<CallLog>>(emptyList()) }

    LaunchedEffect(fileDb) {
        if (calls == null && fileDb != null) {
            localCalls = fileDb.readCalls()
        }
    }

    val activeCalls = calls ?: localCalls

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calls",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = TelegramFAB,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Call")
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
            items(activeCalls, key = { it.callId }) { call ->
                val timeFormatted = remember(call.timestamp) {
                    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(call.timestamp))
                }
                val isMissed = call.type == "missed"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCallClick(call) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contact Avatar Initial
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = TelegramPrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = call.userName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = call.userName,
                            color = if (isMissed) Color(0xFFEF4444) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (call.type) {
                                "incoming" -> Icons.Default.CallReceived
                                "missed" -> Icons.Default.CallMissed
                                else -> Icons.Default.CallMade
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isMissed) Color(0xFFEF4444) else TelegramCheckmark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = timeFormatted,
                                color = TelegramTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    IconButton(onClick = { onCallClick(call) }) {
                        Icon(
                            imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = TelegramPrimary
                        )
                    }
                }
                Divider(
                    color = TelegramDivider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 78.dp)
                )
            }
        }
    }
}
