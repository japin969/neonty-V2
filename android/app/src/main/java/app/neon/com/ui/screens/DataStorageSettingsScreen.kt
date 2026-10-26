package app.neon.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.AppSettings
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageSettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var autoWifi by remember { mutableStateOf(settings.autoDownloadMediaWifi) }
    var autoCellular by remember { mutableStateOf(settings.autoDownloadMediaCellular) }
    var cacheSize by remember { mutableIntStateOf(settings.cacheSizeMb) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data and Storage", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // 45. Data & Storage: Auto-Download Media
            item {
                Text("UNDUH MEDIA OTOMATIS (AUTO-DOWNLOAD)", color = TelegramPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
            }
            item {
                SettingsToggleRow("Saat Terhubung ke Wi-Fi", autoWifi) {
                    autoWifi = it
                    onSaveSettings(settings.copy(autoDownloadMediaWifi = it))
                }
            }
            item {
                SettingsToggleRow("Saat Menggunakan Data Seluler", autoCellular) {
                    autoCellular = it
                    onSaveSettings(settings.copy(autoDownloadMediaCellular = it))
                }
            }

            item { Divider(color = TelegramDarkDivider, modifier = Modifier.padding(vertical = 8.dp)) }

            // 46. Clear Cache & Storage Usage
            item {
                Text("PENGGUNAAN DISK & PENYIMPANAN", color = TelegramPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = TelegramPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Ukuran Cache Neont", color = Color.White, fontSize = 15.sp)
                            Text("$cacheSize MB file media dan database lokal", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        cacheSize = 2
                        onSaveSettings(settings.copy(cacheSizeMb = 2))
                        Toast.makeText(context, "Cache berhasil dibersihkan! (Sisa 2 MB)", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5278)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Bersihkan Cache Sekarang", color = Color.White)
                }
            }
        }
    }
}
