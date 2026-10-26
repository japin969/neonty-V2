package app.neon.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.AppSettings
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesSettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSessions by remember { mutableStateOf(settings.privacy.activeSessions) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Devices", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // Link Desktop Device Button
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(TelegramPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Laptop, contentDescription = null, tint = TelegramPrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Membuka scanner QR untuk Telegram Desktop...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Tautkan Perangkat Desktop", color = Color.White)
                    }
                }
            }

            item { Divider(color = TelegramDarkDivider) }

            // Active Sessions List
            item {
                Text("PERANGKAT AKTIF LAINNYA", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
            }
            items(activeSessions.size) { idx ->
                val dev = activeSessions[idx]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (dev.deviceName.contains("Desktop")) Icons.Default.Computer else Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = TelegramPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(dev.deviceName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("${dev.appVersion} • ${dev.location}", color = Color.LightGray, fontSize = 12.sp)
                        Text(dev.lastActive, color = if (dev.isActiveNow) TelegramPrimary else Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            if (activeSessions.size > 1) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val singleSession = activeSessions.filter { it.isActiveNow }
                            activeSessions = singleSession
                            val updatedPrivacy = settings.privacy.copy(activeSessions = singleSession)
                            onSaveSettings(settings.copy(privacy = updatedPrivacy))
                            Toast.makeText(context, "Semua sesi lain telah ditutup.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Keluar dari Semua Sesi Lain", color = Color.White)
                    }
                }
            }
        }
    }
}
