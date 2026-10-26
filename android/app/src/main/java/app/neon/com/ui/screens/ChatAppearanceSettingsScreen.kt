package app.neon.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
fun ChatAppearanceSettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTheme by remember { mutableStateOf(settings.theme) }
    var currentAccent by remember { mutableStateOf(settings.accentColor) }
    var currentTextSize by remember { mutableFloatStateOf(settings.textSize.toFloat()) }
    var currentWallpaper by remember { mutableStateOf(settings.chatWallpaper) }
    var enterToSend by remember { mutableStateOf(settings.enterToSend) }

    val accentColors = listOf(
        "#5288C1" to "Classic Blue",
        "#10B981" to "Emerald Green",
        "#8B5CF6" to "Royal Purple",
        "#06B6D4" to "Cyan Sky",
        "#F59E0B" to "Amber Warm"
    )

    val wallpapers = listOf("Doodle Pola", "Solid Gelap", "Navy Blue", "Minimalist Black")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Settings & Appearance", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // Live Preview Bubble
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2C3A))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Pratinjau Gelembung Pesan:", color = Color.LightGray, fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(android.graphics.Color.parseColor(currentAccent)))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Halo! Ukuran font dan aksen warna langsung disesuaikan.",
                                color = Color.White,
                                fontSize = currentTextSize.sp
                            )
                        }
                    }
                }
            }

            // 49. Ukuran Font Slider
            item {
                Text("UKURAN FONT TEKS PESAN (${currentTextSize.toInt()} sp)", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Slider(
                        value = currentTextSize,
                        onValueChange = {
                            currentTextSize = it
                            onSaveSettings(settings.copy(textSize = it.toInt()))
                        },
                        valueRange = 12f..24f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(android.graphics.Color.parseColor(currentAccent)),
                            activeTrackColor = Color(android.graphics.Color.parseColor(currentAccent))
                        )
                    )
                }
            }

            item { Divider(color = TelegramDarkDivider, modifier = Modifier.padding(vertical = 8.dp)) }

            // 50. Warna Aksen Palet
            item {
                Text("WARNA AKSEN TELEGRAM", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    accentColors.forEach { (hex, name) ->
                        val isSelected = currentAccent == hex
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable {
                                    currentAccent = hex
                                    onSaveSettings(settings.copy(accentColor = hex))
                                    Toast.makeText(context, "Aksen diubah ke $name", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            item { Divider(color = TelegramDarkDivider, modifier = Modifier.padding(vertical = 8.dp)) }

            // 47. Theme Settings (Light, Dark, AMOLED, System)
            item {
                Text("TEMA APLIKASI (THEME)", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                listOf("dark" to "Dark Mode (Telegram Default)", "amoled" to "AMOLED Pure Black", "light" to "Light Mode", "system" to "Ikuti Sistem Android").forEach { (themeKey, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentTheme = themeKey
                                onSaveSettings(settings.copy(theme = themeKey))
                                Toast.makeText(context, "Tema diubah: $label", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentTheme == themeKey, onClick = null)
                        Spacer(Modifier.width(10.dp))
                        Text(label, color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            item { Divider(color = TelegramDarkDivider, modifier = Modifier.padding(vertical = 8.dp)) }

            // 48. Chat Wallpaper
            item {
                Text("WALLPAPER LATAR BELAKANG CHAT", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    wallpapers.forEach { wp ->
                        val isSelected = currentWallpaper == wp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF2B5278) else Color(0xFF1E2C3A))
                                .border(1.dp, if (isSelected) TelegramPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    currentWallpaper = wp
                                    onSaveSettings(settings.copy(chatWallpaper = wp))
                                    Toast.makeText(context, "Wallpaper diubah: $wp", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(wp, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item { Divider(color = TelegramDarkDivider, modifier = Modifier.padding(vertical = 8.dp)) }

            // 56. Enter to Send & 57. Sticker Settings
            item {
                Text("PENGATURAN TEKS & STIKER", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 8.dp))
            }
            item {
                SettingsToggleRow("Tekan Enter untuk Mengirim Pesan", enterToSend) {
                    enterToSend = it
                    onSaveSettings(settings.copy(enterToSend = it))
                }
            }
            item {
                SettingsActionRow(
                    title = "Paket Stiker & Saran Animasi",
                    subtitle = "6 paket stiker bawaan terpasang",
                    icon = Icons.Default.Mood,
                    onClick = {
                        Toast.makeText(context, "Paket stiker aktif diperbarui otomatis", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
