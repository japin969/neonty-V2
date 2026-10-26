// Neont Messenger - EditProfileScreen.kt
// Pixel-Perfect Telegram Android Edit Profile Screen
package app.neon.com.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.neon.com.data.model.User
import app.neon.com.ui.components.TelegramAvatar
import app.neon.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentUser: User?,
    onSaveProfile: (firstName: String, lastName: String, bio: String, avatarUrl: String) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var firstName by remember { mutableStateOf(currentUser?.firstName ?: "") }
    var lastName by remember { mutableStateOf(currentUser?.lastName ?: "") }
    var username by remember { mutableStateOf(currentUser?.username ?: "neont_user") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var avatarUrl by remember { mutableStateOf(currentUser?.avatarUrl ?: "") }
    var showQrDialog by remember { mutableStateOf(false) }

    // 31. Image picker for profile photo
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            avatarUrl = uri.toString()
            Toast.makeText(context, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    // 33. QR Code Profil Button
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Default.QrCode, "QR Code Profil", tint = Color.White)
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = TelegramPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 12.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                onSaveProfile(firstName.trim(), lastName.trim(), bio.trim(), avatarUrl)
                            },
                            enabled = firstName.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = if (firstName.isNotBlank()) TelegramPrimary else Color.Gray
                            )
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Avatar 80dp dengan Kamera Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramSurface)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    TelegramAvatar(
                        name = firstName.ifEmpty { "User" },
                        avatarUrl = avatarUrl,
                        size = 80
                    )

                    // Ikon Kamera Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Ganti Foto",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Ganti Foto Profil",
                    color = TelegramPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
                )
            }

            Spacer(Modifier.height(14.dp))

            // Form Input Data Diri
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramSurface)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // First Name
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Nama Depan (wajib)", color = TelegramTextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = TelegramPrimary,
                        unfocusedIndicatorColor = TelegramPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Last Name
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nama Belakang (opsional)", color = TelegramTextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = TelegramPrimary,
                        unfocusedIndicatorColor = TelegramPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // 32. Username (@)
                TextField(
                    value = username,
                    onValueChange = { username = it.replace("@", "").trim() },
                    label = { Text("Username (@)", color = TelegramTextSecondary) },
                    leadingIcon = { Text("@", color = TelegramPrimary, fontWeight = FontWeight.Bold) },
                    supportingText = {
                        Text(
                            text = "Orang lain dapat menemukan Anda melalui t.me/$username",
                            color = TelegramTextSecondary,
                            fontSize = 11.sp
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = TelegramPrimary,
                        unfocusedIndicatorColor = TelegramPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Bio
                TextField(
                    value = bio,
                    onValueChange = { if (it.length <= 70) bio = it },
                    label = { Text("Bio", color = TelegramTextSecondary) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = TelegramPrimary,
                        unfocusedIndicatorColor = TelegramPrimary.copy(alpha = 0.5f)
                    ),
                    supportingText = {
                        Text(
                            text = "${70 - bio.length} karakter tersisa",
                            color = TelegramTextSecondary,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "Tulis sedikit tentang diri Anda, pekerjaan, atau minat Anda.\nContoh: Mahasiswa Teknik Informatika @ Jakarta.",
                color = TelegramTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }

    // 33. QR Code Profil Modal Card
    if (showQrDialog) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E2C3A),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TelegramAvatar(name = firstName.ifEmpty { "User" }, avatarUrl = avatarUrl, size = 70)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "$firstName $lastName".trim().ifEmpty { "You" },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "@$username",
                        color = TelegramPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    // QR Pattern Box
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                tint = Color.Black,
                                modifier = Modifier.size(130.dp)
                            )
                            Text(
                                "SCAN TO CONNECT",
                                color = Color.DarkGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Arahkan kamera untuk membuka profil Neont langsung.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            clipboardManager.setText(AnnotatedString("https://t.me/$username"))
                            Toast.makeText(context, "Tautan profil disalin", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp), tint = TelegramPrimary)
                            Spacer(Modifier.width(4.dp))
                            Text("Salin Link", color = TelegramPrimary)
                        }
                        Button(
                            onClick = { showQrDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                        ) {
                            Text("Tutup", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
