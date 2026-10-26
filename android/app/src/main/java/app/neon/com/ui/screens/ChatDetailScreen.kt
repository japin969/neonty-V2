package app.neon.com.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.neon.com.data.AudioRecorder
import app.neon.com.data.ChatRepository
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.model.*
import app.neon.com.ui.components.*
import app.neon.com.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String? = null,
    chat: Chat? = null,
    messages: List<Message>? = null,
    currentUserId: String = "me",
    isContactOnline: Boolean = true,
    isContactTyping: Boolean = false,
    lastSeenTimestamp: Long = 0L,
    fileDb: FileDatabaseManager? = null,
    chatRepo: ChatRepository? = null,
    onBackClick: () -> Unit = {},
    onBackClicked: () -> Unit = onBackClick,
    onSendMessage: (text: String) -> Unit = {},
    onTyping: (isTyping: Boolean) -> Unit = {},
    onDeleteMessage: (msg: Message) -> Unit = {},
    onEditMessage: (msg: Message, newText: String) -> Unit = { _, _ -> },
    onReactMessage: (msg: Message, emoji: String) -> Unit = { _, _ -> },
    onCallClick: () -> Unit = {},
    onAttachClick: (String) -> Unit = {},
    onHeaderClick: () -> Unit = {},
    onMicClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val actualChatId = chatId ?: chat?.chatId ?: ""

    // Dialog & sheet states
    var showAttachSheet by remember { mutableStateOf(false) }
    var showEmojiKeyboard by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showSelfDestructDialog by remember { mutableStateOf(false) }
    var activeSelfDestructSec by remember { mutableStateOf<Int?>(null) }

    // Media viewer state
    var viewingMediaUrl by remember { mutableStateOf<String?>(null) }
    var viewingMediaType by remember { mutableStateOf(MessageType.IMAGE) }

    // Input & editing state
    var textInput by remember { mutableStateOf("") }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var replyingTo by remember { mutableStateOf<Message?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) }

    // Multi-message selection & forwarding
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedMsgIds by remember { mutableStateOf(setOf<String>()) }
    var showForwardDialog by remember { mutableStateOf(false) }
    var selectedForwardTargets by remember { mutableStateOf(setOf<String>()) }

    // Voice recording
    val audioRecorder = remember { AudioRecorder(context) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }

    // Pinned Message state
    var pinnedMessageId by remember { mutableStateOf<String?>(null) }

    val displayMessages = messages ?: emptyList()

    // 23. Draft Pesan: Load draft on launch
    LaunchedEffect(actualChatId) {
        if (actualChatId.isNotBlank() && fileDb != null) {
            val draft = fileDb.getDraft(actualChatId)
            if (draft.isNotBlank()) {
                textInput = draft
            }
            pinnedMessageId = fileDb.getPinnedMessage(actualChatId)
        }
    }

    // 30. Auto-Scroll ke bawah saat kirim/terima pesan baru
    LaunchedEffect(displayMessages.size) {
        if (displayMessages.isNotEmpty() && !isSelectionMode) {
            listState.animateScrollToItem(displayMessages.size - 1)
        }
    }

    // Timer for voice recording
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingSeconds = 0
            while (isRecordingAudio) {
                kotlinx.coroutines.delay(1000)
                recordingSeconds++
            }
        }
    }

    // ActivityResult launchers for File, Image, Video, Music
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && actualChatId.isNotBlank()) {
            scope.launch {
                val path = uri.toString()
                val type = when {
                    path.contains("video") -> MessageType.VIDEO
                    path.contains("image") -> MessageType.IMAGE
                    path.contains("audio") -> MessageType.VOICE
                    else -> MessageType.FILE
                }
                chatRepo?.sendMessage(
                    chatId = actualChatId,
                    senderId = currentUserId,
                    text = path,
                    type = type,
                    mediaUrl = path
                )
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            isRecordingAudio = true
            audioRecorder.startRecording()
        } else {
            Toast.makeText(context, "Izin mikrofon diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    val pinnedMessage = displayMessages.firstOrNull { it.msgId == pinnedMessageId }

    if (showAttachSheet) {
        AttachBottomSheet(
            onDismiss = { showAttachSheet = false },
            onOptionSelected = { optionName ->
                showAttachSheet = false
                when (optionName) {
                    "Gallery" -> fileLauncher.launch("image/*")
                    "File" -> fileLauncher.launch("*/*")
                    "Music" -> fileLauncher.launch("audio/*")
                    "Location" -> showLocationDialog = true
                    "Contact" -> showContactDialog = true
                    "Poll" -> {
                        scope.launch {
                            chatRepo?.sendMessage(
                                chatId = actualChatId,
                                senderId = currentUserId,
                                text = "📊 Polling: Apakah Neont Messenger sudah lengkap?",
                                type = MessageType.TEXT
                            )
                        }
                    }
                    else -> onAttachClick(optionName)
                }
            }
        )
    }

    // 25. Media Viewer Full-screen
    if (viewingMediaUrl != null) {
        MediaViewerDialog(
            mediaUrl = viewingMediaUrl!!,
            title = if (viewingMediaType == MessageType.VIDEO) "Video" else "Foto",
            onDismiss = { viewingMediaUrl = null }
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                // 20. Multi-Select Contextual Header
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedMsgIds.size} dipilih",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedMsgIds = emptySet()
                        }) {
                            Icon(Icons.Default.Close, "Batal", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            selectedMsgIds = if (selectedMsgIds.size == displayMessages.size) {
                                emptySet()
                            } else {
                                displayMessages.map { it.msgId }.toSet()
                            }
                        }) {
                            Text(
                                text = if (selectedMsgIds.size == displayMessages.size) "Lepas Semua" else "Pilih Semua",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                if (selectedMsgIds.isNotEmpty()) {
                                    showForwardDialog = true
                                }
                            },
                            enabled = selectedMsgIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Share, "Teruskan", tint = Color.White)
                        }
                        IconButton(
                            onClick = {
                                val toDelete = displayMessages.filter { selectedMsgIds.contains(it.msgId) }
                                toDelete.forEach { onDeleteMessage(it) }
                                isSelectionMode = false
                                selectedMsgIds = emptySet()
                            },
                            enabled = selectedMsgIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, "Hapus", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2B5278))
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onHeaderClick() }
                        ) {
                            TelegramAvatar(name = chat?.title ?: "Chat", size = 40, isOnline = isContactOnline)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(chat?.title ?: "Chat", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                // 28. Indikator Typing & 29. Online/Last Seen Status
                                if (isContactTyping) {
                                    Text("mengetik...", color = TelegramPrimary, fontSize = 12.sp)
                                } else if (isContactOnline) {
                                    Text("online", color = TelegramPrimary, fontSize = 12.sp)
                                } else if (lastSeenTimestamp > 0) {
                                    Text(formatLastSeen(lastSeenTimestamp), color = TelegramTextSecondary, fontSize = 12.sp)
                                } else {
                                    Text("terlihat baru-baru ini", color = TelegramTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(Icons.Default.ArrowBack, "Kembali", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            isSelectionMode = true
                            selectedMsgIds = emptySet()
                        }) {
                            Icon(Icons.Default.Check, "Pilih Pesan", tint = Color.White)
                        }
                        IconButton(onClick = { showSelfDestructDialog = true }) {
                            Icon(Icons.Default.LocalFireDepartment, "Timer Hapus Otomatis", tint = if (activeSelfDestructSec != null) Color(0xFFFF5722) else Color.White)
                        }
                        IconButton(onClick = onCallClick) {
                            Icon(Icons.Default.Phone, "Panggilan", tint = Color.White)
                        }
                        IconButton(onClick = { /* Menu Opsi Chat */ }) {
                            Icon(Icons.Default.MoreVert, "Lainnya", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
                )
            }
        },
        containerColor = TelegramDarkBG,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 18. Pin Pesan Header Banner
            if (pinnedMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF242F3D))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(3.dp).height(28.dp).background(TelegramPrimary))
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f).clickable {
                        val index = displayMessages.indexOfFirst { it.msgId == pinnedMessage.msgId }
                        if (index >= 0) {
                            scope.launch { listState.animateScrollToItem(index) }
                        }
                    }) {
                        Text("Pesan Disematkan", color = TelegramPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(pinnedMessage.text, color = Color.White, fontSize = 12.sp, maxLines = 1)
                    }
                    IconButton(
                        onClick = {
                            pinnedMessageId = null
                            fileDb?.setPinnedMessage(actualChatId, null)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, "Lepas Pin", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Message List Stream
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(displayMessages, key = { it.msgId }) { msg ->
                    MessageBubble(
                        message = msg,
                        isOutgoing = msg.senderId == currentUserId,
                        isSelected = selectedMsgIds.contains(msg.msgId),
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) {
                                selectedMsgIds = if (selectedMsgIds.contains(msg.msgId)) {
                                    selectedMsgIds - msg.msgId
                                } else {
                                    selectedMsgIds + msg.msgId
                                }
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                selectedMessage = it
                                showMessageDialog = true
                            } else {
                                selectedMsgIds = if (selectedMsgIds.contains(msg.msgId)) {
                                    selectedMsgIds - msg.msgId
                                } else {
                                    selectedMsgIds + msg.msgId
                                }
                            }
                        },
                        // 12. Swipe to Reply
                        onSwipeReply = {
                            replyingTo = it
                        },
                        // 25. Media Viewer Trigger
                        onMediaClick = { url, type ->
                            viewingMediaUrl = url
                            viewingMediaType = type
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            // Context Menu Dialog (Reply, Edit, Copy, Pin, Star, Self-Destruct, Delete)
            if (showMessageDialog && selectedMessage != null) {
                AlertDialog(
                    onDismissRequest = { showMessageDialog = false },
                    title = { Text("Pilihan Pesan", color = Color.White, fontWeight = FontWeight.Bold) },
                    containerColor = app.neon.com.ui.theme.TelegramSurface,
                    text = {
                        Column {
                            // 12. Reply
                            TextButton(onClick = {
                                showMessageDialog = false
                                replyingTo = selectedMessage
                            }) { Text("Balas (Reply)", color = Color.White) }

                            // 14. Copy Teks Pesan
                            TextButton(onClick = {
                                showMessageDialog = false
                                clipboardManager.setText(AnnotatedString(selectedMessage?.text ?: ""))
                                Toast.makeText(context, "Teks disalin ke clipboard", Toast.LENGTH_SHORT).show()
                            }) { Text("Salin Teks (Copy)", color = Color.White) }

                            // 15. Edit Pesan (hanya pesan sendiri)
                            if (selectedMessage?.senderId == currentUserId) {
                                TextButton(onClick = {
                                    showMessageDialog = false
                                    editingMessage = selectedMessage
                                    textInput = selectedMessage?.text ?: ""
                                }) { Text("Edit Pesan", color = Color.White) }
                            }

                            // 18. Pin Pesan
                            TextButton(onClick = {
                                showMessageDialog = false
                                pinnedMessageId = selectedMessage?.msgId
                                fileDb?.setPinnedMessage(actualChatId, selectedMessage?.msgId)
                                Toast.makeText(context, "Pesan disematkan di header", Toast.LENGTH_SHORT).show()
                            }) { Text("Sematkan Pesan (Pin)", color = Color.White) }

                            // 19. Star / Simpan ke "Saved Messages"
                            TextButton(onClick = {
                                showMessageDialog = false
                                scope.launch {
                                    selectedMessage?.let { orig ->
                                        fileDb?.appendMessage("c_saved", orig.copy(chatId = "c_saved", isSaved = true))
                                        Toast.makeText(context, "Disimpan ke Saved Messages", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) { Text("Simpan Pesan (Save)", color = Color.White) }

                            // 13. Forward
                            TextButton(onClick = {
                                showMessageDialog = false
                                selectedMsgIds = setOf(selectedMessage!!.msgId)
                                showForwardDialog = true
                            }) { Text("Teruskan (Forward)", color = Color(0xFF64B5F6)) }

                            // 20. Multi-select mode
                            TextButton(onClick = {
                                showMessageDialog = false
                                isSelectionMode = true
                                selectedMsgIds = setOf(selectedMessage!!.msgId)
                            }) { Text("Pilih Pesan (Multi-Select)", color = Color.White) }

                            // 21. Reaction Emoji
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("👍", "❤️", "😂", "😮", "😢", "🔥").forEach { emoji ->
                                    TextButton(onClick = {
                                        showMessageDialog = false
                                        onReactMessage(selectedMessage!!, emoji)
                                    }) { Text(emoji, fontSize = 20.sp) }
                                }
                            }

                            // 16. Delete for Me & 17. Delete for Everyone
                            Row {
                                TextButton(onClick = {
                                    showMessageDialog = false
                                    fileDb?.deleteMessage(actualChatId, selectedMessage!!.msgId)
                                    Toast.makeText(context, "Dihapus untuk saya", Toast.LENGTH_SHORT).show()
                                }) { Text("Hapus untuk Saya", color = Color.LightGray) }

                                TextButton(onClick = {
                                    showMessageDialog = false
                                    onDeleteMessage(selectedMessage!!)
                                    Toast.makeText(context, "Dihapus untuk semua", Toast.LENGTH_SHORT).show()
                                }) { Text("Hapus Semua", color = Color.Red) }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMessageDialog = false }) {
                            Text("Batal", color = Color.White)
                        }
                    }
                )
            }

            // 13. Forward Pesan Dialog
            if (showForwardDialog && selectedMsgIds.isNotEmpty()) {
                val defaultTargets = listOf(
                    "c_saved" to "Saved Messages",
                    "c_pavel" to "Pavel Durov",
                    "c_elena" to "Elena Rostova",
                    "c_android_core" to "Neont Android Core Team",
                    "c_neont_news" to "Neont News & Releases"
                )

                AlertDialog(
                    onDismissRequest = { showForwardDialog = false },
                    title = {
                        Text("Teruskan ${selectedMsgIds.size} Pesan Ke:", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    containerColor = TelegramDarkHeader,
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Pilih satu atau lebih chat tujuan:", color = TelegramTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                            defaultTargets.forEach { (targetId, title) ->
                                val isSelected = selectedForwardTargets.contains(targetId)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedForwardTargets = if (isSelected) selectedForwardTargets - targetId else selectedForwardTargets + targetId
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedForwardTargets = if (checked) selectedForwardTargets + targetId else selectedForwardTargets - targetId
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF5288C1), uncheckedColor = Color.Gray)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(title, color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val messagesToForward = displayMessages.filter { selectedMsgIds.contains(it.msgId) }
                                selectedForwardTargets.forEach { targetChatId ->
                                    messagesToForward.forEach { origMsg ->
                                        scope.launch {
                                            chatRepo?.sendMessage(
                                                chatId = targetChatId,
                                                senderId = currentUserId,
                                                text = origMsg.text,
                                                type = origMsg.type,
                                                mediaUrl = origMsg.mediaUrl
                                            )
                                        }
                                    }
                                }
                                showForwardDialog = false
                                isSelectionMode = false
                                selectedMsgIds = emptySet()
                                selectedForwardTargets = emptySet()
                                Toast.makeText(context, "Pesan berhasil diteruskan", Toast.LENGTH_SHORT).show()
                            },
                            enabled = selectedForwardTargets.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5288C1))
                        ) {
                            Text("Kirim (${selectedForwardTargets.size})", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showForwardDialog = false }) {
                            Text("Batal", color = Color.White)
                        }
                    }
                )
            }

            // 7. Kirim Lokasi Dialog
            if (showLocationDialog) {
                var locationText by remember { mutableStateOf("Jakarta, Indonesia (-6.2088, 106.8456)") }
                AlertDialog(
                    onDismissRequest = { showLocationDialog = false },
                    title = { Text("Bagikan Lokasi", color = Color.White) },
                    containerColor = TelegramDarkHeader,
                    text = {
                        Column {
                            Text("Pilih atau masukkan koordinat lokasi:", color = Color.LightGray, fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = locationText,
                                onValueChange = { locationText = it },
                                textStyle = LocalTextStyle.current.copy(color = Color.White)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLocationDialog = false
                                scope.launch {
                                    chatRepo?.sendMessage(
                                        chatId = actualChatId,
                                        senderId = currentUserId,
                                        text = locationText,
                                        type = MessageType.LOCATION
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                        ) {
                            Text("Kirim Lokasi", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLocationDialog = false }) {
                            Text("Batal", color = Color.White)
                        }
                    }
                )
            }

            // 6. Kirim Kontak Dialog
            if (showContactDialog) {
                var contactName by remember { mutableStateOf("Elena Rostova (+62 812-3456-7890)") }
                AlertDialog(
                    onDismissRequest = { showContactDialog = false },
                    title = { Text("Bagikan Kontak", color = Color.White) },
                    containerColor = TelegramDarkHeader,
                    text = {
                        Column {
                            Text("Pilih kontak untuk dikirimkan:", color = Color.LightGray, fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = contactName,
                                onValueChange = { contactName = it },
                                textStyle = LocalTextStyle.current.copy(color = Color.White)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showContactDialog = false
                                scope.launch {
                                    chatRepo?.sendMessage(
                                        chatId = actualChatId,
                                        senderId = currentUserId,
                                        text = contactName,
                                        type = MessageType.CONTACT
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                        ) {
                            Text("Kirim Kontak", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showContactDialog = false }) {
                            Text("Batal", color = Color.White)
                        }
                    }
                )
            }

            // 22. Self-Destruct Timer Dialog
            if (showSelfDestructDialog) {
                AlertDialog(
                    onDismissRequest = { showSelfDestructDialog = false },
                    title = { Text("Timer Hapus Otomatis (Self-Destruct)", color = Color.White) },
                    containerColor = TelegramDarkHeader,
                    text = {
                        Column {
                            listOf(null to "Mati (Off)", 5 to "5 Detik", 10 to "10 Detik", 60 to "1 Menit", 3600 to "1 Jam", 86400 to "1 Hari").forEach { (sec, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeSelfDestructSec = sec
                                            showSelfDestructDialog = false
                                            Toast.makeText(context, "Timer diatur: $label", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = activeSelfDestructSec == sec, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSelfDestructDialog = false }) {
                            Text("Tutup", color = Color.White)
                        }
                    }
                )
            }

            // Bottom Bar: Selection Toolbar OR Composer
            if (isSelectionMode) {
                Surface(color = TelegramDarkHeader, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${selectedMsgIds.size} pesan terpilih", color = Color.White, fontSize = 14.sp)
                        Button(
                            onClick = { if (selectedMsgIds.isNotEmpty()) showForwardDialog = true },
                            enabled = selectedMsgIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5288C1))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Teruskan", fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // Editing banner
                if (editingMessage != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFF242F3D)).padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, "Edit", tint = TelegramPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mengedit Pesan", color = TelegramPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(editingMessage?.text ?: "", color = Color.White, fontSize = 13.sp, maxLines = 1)
                        }
                        IconButton(onClick = { editingMessage = null; textInput = "" }) {
                            Icon(Icons.Default.Close, "Batal Edit", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // 8. Voice Recording Live Indicator
                if (isRecordingAudio) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E2C3A))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.Red))
                            Spacer(Modifier.width(8.dp))
                            Text("Merekam 0:${if (recordingSeconds < 10) "0" else ""}$recordingSeconds", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Row {
                            TextButton(onClick = {
                                audioRecorder.stopRecording()
                                isRecordingAudio = false
                            }) {
                                Text("Batal", color = Color.LightGray)
                            }
                            Button(
                                onClick = {
                                    val recordedFile = audioRecorder.stopRecording()
                                    isRecordingAudio = false
                                    if (recordedFile != null && recordedFile.exists()) {
                                        scope.launch {
                                            chatRepo?.sendMessage(
                                                chatId = actualChatId,
                                                senderId = currentUserId,
                                                text = "Voice message (0:${recordingSeconds})",
                                                type = MessageType.VOICE,
                                                mediaUrl = recordedFile.absolutePath
                                            )
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                            ) {
                                Text("Kirim", color = Color.White)
                            }
                        }
                    }
                } else {
                    // Standard Composer Bar
                    ComposerBar(
                        text = textInput,
                        onTextChange = {
                            textInput = it
                            // 23. Draft Pesan: Auto-save draft
                            fileDb?.saveDraft(actualChatId, it)
                        },
                        onSendMessage = { text ->
                            if (text.isNotBlank()) {
                                if (editingMessage != null) {
                                    onEditMessage(editingMessage!!, text)
                                    editingMessage = null
                                } else {
                                    scope.launch {
                                        val newMsg = chatRepo?.sendMessage(
                                            chatId = actualChatId,
                                            senderId = currentUserId,
                                            text = text,
                                            type = MessageType.TEXT
                                        )
                                        // Self-destruct simulation if active
                                        if (activeSelfDestructSec != null && newMsg != null) {
                                            scope.launch {
                                                kotlinx.coroutines.delay((activeSelfDestructSec!! * 1000).toLong())
                                                fileDb?.deleteMessage(actualChatId, newMsg.msgId)
                                            }
                                        }
                                    }
                                }
                                replyingTo = null
                                textInput = ""
                                fileDb?.saveDraft(actualChatId, "")
                                showEmojiKeyboard = false
                            }
                        },
                        onTyping = onTyping,
                        onAttachClick = { showAttachSheet = true },
                        onMicClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                isRecordingAudio = true
                                audioRecorder.startRecording()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onEmojiClick = { showEmojiKeyboard = !showEmojiKeyboard },
                        replyingTo = replyingTo?.let { "${it.senderId}: ${it.text}" },
                        onCancelReply = { replyingTo = null }
                    )
                }
            }

            // 10. Stiker & 11. GIF & Emoji Keyboard
            if (showEmojiKeyboard && !isSelectionMode) {
                EmojiKeyboard(
                    onEmojiSelected = { emoji ->
                        textInput += emoji
                        fileDb?.saveDraft(actualChatId, textInput)
                    },
                    onStickerSelected = { stickerUrl ->
                        showEmojiKeyboard = false
                        scope.launch {
                            chatRepo?.sendMessage(
                                chatId = actualChatId,
                                senderId = currentUserId,
                                text = stickerUrl,
                                type = MessageType.STICKER,
                                mediaUrl = stickerUrl
                            )
                        }
                    },
                    onGifSelected = { gifUrl ->
                        showEmojiKeyboard = false
                        scope.launch {
                            chatRepo?.sendMessage(
                                chatId = actualChatId,
                                senderId = currentUserId,
                                text = gifUrl,
                                type = MessageType.GIF,
                                mediaUrl = gifUrl
                            )
                        }
                    },
                    onBackspaceClick = {
                        if (textInput.isNotEmpty()) {
                            textInput = textInput.dropLast(1)
                            fileDb?.saveDraft(actualChatId, textInput)
                        }
                    }
                )
            }
        }
    }
}
