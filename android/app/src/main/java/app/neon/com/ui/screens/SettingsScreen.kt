// Neont Messenger - SettingsScreen.kt
// Pixel-Perfect Telegram Android Settings Screen with complete Features 31-60
package app.neon.com.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.neon.com.data.model.AppSettings
import app.neon.com.data.model.User
import app.neon.com.ui.components.TelegramAvatar
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User? = null,
    settings: AppSettings = AppSettings(),
    onEditProfileClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSaveSettings: (AppSettings) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = onLogoutClick,
    onDataStorageClick: () -> Unit = {},
    onChatSettingsClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onDevicesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user = currentUser ?: User(
        uid = "me",
        firstName = "You",
        phoneNumber = "+62 812 3456 7890",
        username = "neont_user"
    )

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramSurface)
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
            // Header User Profile Card (31, 32)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelegramSurface)
                        .clickable { onEditProfileClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelegramAvatar(
                        name = "${user.firstName} ${user.lastName}".trim().ifEmpty { "You" },
                        avatarUrl = user.avatarUrl,
                        size = 64
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${user.firstName} ${user.lastName}".trim().ifEmpty { "You" },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = user.phoneNumber.ifEmpty { "+62 812 3456 7890" },
                            color = TelegramTextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "@${user.username.ifEmpty { "neont_user" }}",
                            color = TelegramPrimary,
                            fontSize = 13.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = TelegramPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))
            }

            // Settings Items Section
            item {
                Column(modifier = Modifier.background(TelegramSurface)) {
                    Text(
                        text = "PENGATURAN (SETTINGS)",
                        color = TelegramPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )

                    // 44. Notifications
                    SettingsMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifikasi dan Suara",
                        onClick = onNotificationClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    // 34-43. Privacy and Security
                    SettingsMenuItem(
                        icon = Icons.Default.Lock,
                        title = "Privasi dan Keamanan",
                        onClick = onPrivacyClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    // 45, 46. Data and Storage
                    SettingsMenuItem(
                        icon = Icons.Default.PieChart,
                        title = "Data dan Penyimpanan",
                        subtitle = "${settings.cacheSizeMb} MB",
                        onClick = onDataStorageClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    // 47, 48, 49, 50, 56, 57. Chat Settings
                    SettingsMenuItem(
                        icon = Icons.Default.Chat,
                        title = "Pengaturan Chat & Tampilan",
                        subtitle = settings.theme,
                        onClick = onChatSettingsClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    // 52. Devices
                    SettingsMenuItem(
                        icon = Icons.Default.Devices,
                        title = "Perangkat Tertaut (Devices)",
                        subtitle = "${settings.privacy.activeSessions.size} aktif",
                        onClick = onDevicesClick
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    // 51. Language
                    SettingsMenuItem(
                        icon = Icons.Default.Language,
                        title = "Bahasa (Language)",
                        subtitle = settings.language,
                        onClick = onLanguageClick
                    )
                }

                Spacer(Modifier.height(14.dp))
            }

            // Lainnya (58. Sync Contacts, 59. Invite Friends, 60. Help & Support)
            item {
                Column(modifier = Modifier.background(TelegramSurface)) {
                    Text(
                        text = "LAINNYA (HELP & INVITE)",
                        color = TelegramPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )

                    // 58. Contacts Sync
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newSync = !settings.contactsSyncEnabled
                                onSaveSettings(settings.copy(contactsSyncEnabled = newSync))
                                Toast.makeText(context, if (newSync) "Sinkronisasi kontak aktif" else "Sinkronisasi kontak nonaktif", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = TelegramTextSecondary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Sinkronkan Kontak Buku Telepon", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Switch(checked = settings.contactsSyncEnabled, onCheckedChange = null)
                    }
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    // 59. Invite Friends
                    SettingsMenuItem(
                        icon = Icons.Default.PersonAdd,
                        title = "Undang Teman (Invite Friends)",
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Ayo mengobrol di Neont Messenger! Unduh aplikasinya sekarang.")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Tautan Undangan"))
                        }
                    )
                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    // 60. Help & Support
                    SettingsMenuItem(
                        icon = Icons.Default.Help,
                        title = "Bantuan & Tanya Jawab (FAQ)",
                        onClick = { showHelpDialog = true }
                    )
                }

                Spacer(Modifier.height(14.dp))
            }

            // 53. Log Out & 54. Delete Account
            item {
                Column(modifier = Modifier.background(TelegramSurface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = TelegramRed, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text(text = "Keluar Akun (Log Out)", color = TelegramRed, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    Divider(color = TelegramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))

                    // 54. Delete Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteAccountDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete Account", tint = Color(0xFFFF5252), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text(text = "Hapus Akun Saya (Delete Account)", color = Color(0xFFFF5252), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }

    // 53. Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Keluar", color = Color.White) },
            containerColor = TelegramDarkHeader,
            text = { Text("Apakah Anda yakin ingin keluar dari akun ini? Riwayat chat lokal tetap aman.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramRed)
                ) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal", color = Color.White) }
            }
        )
    }

    // 54. Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Hapus Akun Neont", color = Color.White) },
            containerColor = TelegramDarkHeader,
            text = { Text("PERINGATAN: Tindakan ini tidak dapat dibatalkan. Semua pesan, media, dan grup Anda akan dihapus secara permanen.", color = Color.Red.copy(alpha = 0.9f)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccountClick()
                        Toast.makeText(context, "Akun berhasil dihapus", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Hapus Permanen", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Batal", color = Color.White) }
            }
        )
    }

    // 60. Help & Support Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Bantuan Neont Messenger", color = Color.White) },
            containerColor = TelegramDarkHeader,
            text = {
                Column {
                    Text("Neont Messenger v1.0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Aplikasi pesan instan berbasis Kotlin Native (Jetpack Compose) dengan sistem penyimpanan offline internal JSON dan sinkronisasi Firebase Realtime Database.", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Butuh bantuan teknis? Hubungi kami di @NeontSupportBot atau kunjungi repositori kami di GitHub.", color = TelegramPrimary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("Mengerti", color = Color.White) }
            }
        )
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TelegramTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = TelegramPrimary,
                fontSize = 13.sp
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TelegramTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
