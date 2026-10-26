package app.neon.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.neon.com.data.model.ActiveDevice
import app.neon.com.data.model.AppSettings
import app.neon.com.data.model.PrivacySettings
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var privacy by remember { mutableStateOf(settings.privacy) }

    // Dialog states
    var showTwoStepDialog by remember { mutableStateOf(false) }
    var showPasscodeDialog by remember { mutableStateOf(false) }
    var showActiveSessionsDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showVisibilityPicker by remember { mutableStateOf<String?>(null) } // "Last Seen", "Profile Photo", "Calls", "Forwarded Messages"
    var showAutoDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy and Security", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // SECURITY SECTION (34, 35, 36, 37, 38)
            item {
                Text("KEAMANAN (SECURITY)", color = TelegramPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
            }
            item {
                // 34. Two-Step Verification
                SettingsActionRow(
                    title = "Verifikasi Dua Langkah (2FA)",
                    subtitle = if (privacy.twoStepVerificationEnabled) "Aktif (Password dibuat)" else "Mati",
                    icon = Icons.Default.Security,
                    onClick = { showTwoStepDialog = true }
                )
            }
            item {
                // 35. Passcode Lock
                SettingsActionRow(
                    title = "Kunci Aplikasi (Passcode Lock)",
                    subtitle = if (privacy.passcodeLockEnabled) "Aktif (PIN/Sidik Jari)" else "Mati",
                    icon = Icons.Default.Fingerprint,
                    onClick = { showPasscodeDialog = true }
                )
            }
            item {
                // 36 & 37. Active Sessions & Terminate
                SettingsActionRow(
                    title = "Sesi Aktif & Perangkat",
                    subtitle = "${privacy.activeSessions.size} perangkat terhubung",
                    icon = Icons.Default.Devices,
                    onClick = { showActiveSessionsDialog = true }
                )
            }
            item {
                // 38. Blocked Users
                SettingsActionRow(
                    title = "Pengguna Diblokir",
                    subtitle = "${privacy.blockedUsers.size} kontak",
                    icon = Icons.Default.Block,
                    onClick = { showBlockedUsersDialog = true }
                )
            }

            item { Divider(color = TelegramDarkDivider, modifier = Modifier.padding(vertical = 8.dp)) }

            // PRIVACY SECTION (39, 40, 41, 42, 43, 55)
            item {
                Text("PRIVASI (PRIVACY)", color = TelegramPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
            }
            item {
                // 39. Privacy Last Seen
                SettingsActionRow(
                    title = "Waktu Terakhir Dilihat (Last Seen)",
                    subtitle = privacy.lastSeenVisibility,
                    icon = Icons.Default.Visibility,
                    onClick = { showVisibilityPicker = "Last Seen" }
                )
            }
            item {
                // 40. Privacy Profile Photo
                SettingsActionRow(
                    title = "Foto Profil",
                    subtitle = privacy.profilePhotoVisibility,
                    icon = Icons.Default.AccountCircle,
                    onClick = { showVisibilityPicker = "Profile Photo" }
                )
            }
            item {
                // 41. Privacy Calls
                SettingsActionRow(
                    title = "Panggilan Suara & Video",
                    subtitle = privacy.callsVisibility,
                    icon = Icons.Default.Phone,
                    onClick = { showVisibilityPicker = "Calls" }
                )
            }
            item {
                // 42. Privacy Forwarded Messages
                SettingsActionRow(
                    title = "Pesan yang Diteruskan",
                    subtitle = privacy.forwardedMessagesVisibility,
                    icon = Icons.Default.Share,
                    onClick = { showVisibilityPicker = "Forwarded Messages" }
                )
            }
            item {
                // 43. Auto-Delete Messages
                SettingsActionRow(
                    title = "Hapus Pesan Otomatis Global",
                    subtitle = privacy.autoDeleteTimer,
                    icon = Icons.Default.Timer,
                    onClick = { showAutoDeleteDialog = true }
                )
            }

            item { Divider(color = TelegramDarkDivider, modifier = Modifier.padding(vertical = 8.dp)) }

            // 55. Sensitive Content
            item {
                Text("KONTEN SENSITIF", color = TelegramPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp))
            }
            item {
                SettingsToggleRow("Sensor Media Sensitif", privacy.sensitiveContentEnabled) {
                    privacy = privacy.copy(sensitiveContentEnabled = it)
                    onSaveSettings(settings.copy(privacy = privacy))
                    Toast.makeText(context, if (it) "Sensor media aktif" else "Sensor media dinonaktifkan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 34. Two-Step Verification Dialog
    if (showTwoStepDialog) {
        var passwordInput by remember { mutableStateOf(privacy.twoStepPassword) }
        AlertDialog(
            onDismissRequest = { showTwoStepDialog = false },
            title = { Text("Verifikasi Dua Langkah", color = Color.White) },
            containerColor = TelegramDarkHeader,
            text = {
                Column {
                    Text("Buat password tambahan yang akan diminta saat Anda masuk di perangkat baru.", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password Tambahan", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isEnabled = passwordInput.isNotBlank()
                        privacy = privacy.copy(twoStepVerificationEnabled = isEnabled, twoStepPassword = passwordInput)
                        onSaveSettings(settings.copy(privacy = privacy))
                        showTwoStepDialog = false
                        Toast.makeText(context, if (isEnabled) "2FA Berhasil Diaktifkan" else "2FA Dinonaktifkan", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTwoStepDialog = false }) { Text("Batal", color = Color.White) }
            }
        )
    }

    // 35. Passcode Lock Dialog
    if (showPasscodeDialog) {
        var pinInput by remember { mutableStateOf(privacy.passcodePin) }
        var bioToggle by remember { mutableStateOf(privacy.biometricEnabled) }
        AlertDialog(
            onDismissRequest = { showPasscodeDialog = false },
            title = { Text("Kunci Aplikasi (Passcode Lock)", color = Color.White) },
            containerColor = TelegramDarkHeader,
            text = {
                Column {
                    Text("Masukkan 4 digit PIN untuk mengunci Neont Messenger saat layar mati.", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("4-Digit PIN", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = bioToggle, onCheckedChange = { bioToggle = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Buka dengan Sidik Jari", color = Color.White, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isEnabled = pinInput.length == 4
                        privacy = privacy.copy(passcodeLockEnabled = isEnabled, passcodePin = pinInput, biometricEnabled = bioToggle)
                        onSaveSettings(settings.copy(privacy = privacy))
                        showPasscodeDialog = false
                        Toast.makeText(context, if (isEnabled) "Kunci PIN Aktif" else "Kunci Dinonaktifkan", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                ) {
                    Text("Terapkan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasscodeDialog = false }) { Text("Batal", color = Color.White) }
            }
        )
    }

    // 36 & 37. Active Sessions Dialog
    if (showActiveSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showActiveSessionsDialog = false },
            title = { Text("Sesi Login Aktif", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = TelegramDarkHeader,
            text = {
                Column {
                    Text("Perangkat yang saat ini sedang login dengan akun Anda:", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    privacy.activeSessions.forEach { dev ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = TelegramPrimary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dev.deviceName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("${dev.location} • ${dev.lastActive}", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            // 37. Terminate Session
                            privacy = privacy.copy(activeSessions = privacy.activeSessions.filter { it.isActiveNow })
                            onSaveSettings(settings.copy(privacy = privacy))
                            Toast.makeText(context, "Semua sesi lain telah diakhiri (Terminated)", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Akhiri Semua Sesi Lain", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActiveSessionsDialog = false }) { Text("Tutup", color = Color.White) }
            }
        )
    }

    // 38. Blocked Users Dialog
    if (showBlockedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersDialog = false },
            title = { Text("Pengguna Diblokir", color = Color.White) },
            containerColor = TelegramDarkHeader,
            text = {
                Column {
                    if (privacy.blockedUsers.isEmpty()) {
                        Text("Belum ada pengguna yang Anda blokir.", color = Color.LightGray, fontSize = 13.sp)
                    } else {
                        privacy.blockedUsers.forEach { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user, color = Color.White, fontSize = 14.sp)
                                TextButton(onClick = {
                                    privacy = privacy.copy(blockedUsers = privacy.blockedUsers - user)
                                    onSaveSettings(settings.copy(privacy = privacy))
                                    Toast.makeText(context, "Blokir dibuka", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Buka Blokir", color = TelegramPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedUsersDialog = false }) { Text("Tutup", color = Color.White) }
            }
        )
    }

    // 39, 40, 41, 42. Visibility Picker Dialog (Everybody / My Contacts / Nobody)
    if (showVisibilityPicker != null) {
        val target = showVisibilityPicker!!
        val options = listOf("Semua Orang (Everybody)", "Kontak Saya (My Contacts)", "Tidak Ada (Nobody)")
        val currentVal = when (target) {
            "Last Seen" -> privacy.lastSeenVisibility
            "Profile Photo" -> privacy.profilePhotoVisibility
            "Calls" -> privacy.callsVisibility
            else -> privacy.forwardedMessagesVisibility
        }

        AlertDialog(
            onDismissRequest = { showVisibilityPicker = null },
            title = { Text("Siapa yang dapat melihat $target Anda?", color = Color.White) },
            containerColor = TelegramDarkHeader,
            text = {
                Column {
                    options.forEach { opt ->
                        val isSelected = currentVal.startsWith(opt.split(" ")[0])
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newVal = opt.substringAfter("(").substringBefore(")")
                                    privacy = when (target) {
                                        "Last Seen" -> privacy.copy(lastSeenVisibility = newVal)
                                        "Profile Photo" -> privacy.copy(profilePhotoVisibility = newVal)
                                        "Calls" -> privacy.copy(callsVisibility = newVal)
                                        else -> privacy.copy(forwardedMessagesVisibility = newVal)
                                    }
                                    onSaveSettings(settings.copy(privacy = privacy))
                                    showVisibilityPicker = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(opt, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVisibilityPicker = null }) { Text("Batal", color = Color.White) }
            }
        )
    }

    // 43. Auto-Delete Messages Dialog
    if (showAutoDeleteDialog) {
        val options = listOf("Off", "24 Hours", "7 Days", "1 Month")
        AlertDialog(
            onDismissRequest = { showAutoDeleteDialog = false },
            title = { Text("Hapus Pesan Otomatis Global", color = Color.White) },
            containerColor = TelegramDarkHeader,
            text = {
                Column {
                    options.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    privacy = privacy.copy(autoDeleteTimer = opt)
                                    onSaveSettings(settings.copy(privacy = privacy))
                                    showAutoDeleteDialog = false
                                    Toast.makeText(context, "Hapus otomatis diatur: $opt", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = privacy.autoDeleteTimer == opt, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(opt, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoDeleteDialog = false }) { Text("Tutup", color = Color.White) }
            }
        )
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = TelegramTextSecondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp)
            Text(subtitle, color = TelegramPrimary, fontSize = 13.sp)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TelegramTextSecondary, modifier = Modifier.size(20.dp))
    }
}
