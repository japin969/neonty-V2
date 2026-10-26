package app.neon.com

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.neon.com.data.AudioRecorder
import app.neon.com.data.ChatRepository
import app.neon.com.data.FileDatabaseManager
import app.neon.com.data.FilebaseStorage
import app.neon.com.data.model.*
import app.neon.com.ui.screens.*
import app.neon.com.ui.theme.NeontTheme
import app.neon.com.ui.theme.TelegramDarkBG
import app.neon.com.utils.NotificationHelper
import app.neon.com.utils.UpdateChecker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var fileDb: FileDatabaseManager
    private lateinit var chatRepo: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileDb = FileDatabaseManager(applicationContext)
        chatRepo = ChatRepository(fileDb)

        setContent {
            NeontTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = TelegramDarkBG) {
                    AppRoot(fileDb, chatRepo, applicationContext)
                }
            }
        }
    }
}

@Composable
fun AppRoot(fileDb: FileDatabaseManager, chatRepo: ChatRepository, context: Context) {
    var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val info = UpdateChecker.checkForUpdate()
        val currentVersionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        if (info != null && info.latestVersionCode > currentVersionCode) {
            updateInfo = info
        }
        isChecking = false
    }

    if (isChecking) {
        Box(modifier = Modifier.fillMaxSize().background(TelegramDarkBG), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else if (updateInfo != null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).background(TelegramDarkBG),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Update Tersedia", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(updateInfo!!.message, color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Text("Cek link download di bio TikTok atau WhatsApp kami.", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        }
    } else {
        NeontApp(fileDb, chatRepo, context)
    }
}

sealed class Screen {
    object Home : Screen()
    object Settings : Screen()
    object Privacy : Screen()
    object Notification : Screen()
    object EditProfile : Screen()
    object DataStorage : Screen()
    object ChatSettings : Screen()
    object Language : Screen()
    object Devices : Screen()
    object Search : Screen()
    object CreateGroup : Screen()
    data class Chat(val chatId: String) : Screen()
    data class UserProfile(val user: User) : Screen()
}

@Composable
fun NeontApp(fileDb: FileDatabaseManager, chatRepo: ChatRepository, context: Context) {
    val scope = rememberCoroutineScope()
    val filebase = remember { FilebaseStorage(context) }
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var contacts by remember { mutableStateOf<List<User>>(emptyList()) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var chatMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isContactOnline by remember { mutableStateOf(false) }
    var lastSeenTimestamp by remember { mutableStateOf(0L) }
    var isContactTyping by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var appSettings by remember { mutableStateOf(AppSettings()) }

    var isRecording by remember { mutableStateOf(false) }
    val audioRecorder = remember { AudioRecorder(context) }

    LaunchedEffect(Unit) {
        chatRepo.setupPresence("me")
        scope.launch {
            contacts = fileDb.readUsers()
            currentUser = contacts.firstOrNull { it.uid == "me" }
            appSettings = fileDb.readSettings()
            
            chatRepo.listenToChats().collect { updatedChats -> chats = updatedChats }
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen is Screen.Chat) {
            val chatId = (currentScreen as Screen.Chat).chatId
            launch {
                chatRepo.listenToMessages(chatId).collect { msgs ->
                    chatMessages = msgs
                    if (msgs.isNotEmpty() && msgs.last().senderId != "me") {
                        val chatName = chats.firstOrNull { it.chatId == chatId }?.title ?: "New Message"
                        NotificationHelper.showNotification(context, chatName, msgs.last().text)
                    }
                }
            }
            launch {
                chatRepo.listenToUserPresence(chatId).collect { (online, lastSeen) ->
                    isContactOnline = online
                    lastSeenTimestamp = lastSeen
                }
            }
            launch {
                chatRepo.listenToTyping(chatId, chatId).collect { typing -> isContactTyping = typing }
            }
        } else {
            chatMessages = emptyList()
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && currentScreen is Screen.Chat) {
            scope.launch {
                isUploading = true
                try {
                    val contentResolver = context.contentResolver
                    var fileName = "file_${System.currentTimeMillis()}"
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex >= 0) fileName = cursor.getString(nameIndex)
                        }
                    }
                    val inputStream = contentResolver.openInputStream(uri)
                    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                    if (inputStream != null) {
                        val fileUrl = filebase.uploadFile(fileName, mimeType, inputStream)
                        inputStream.close()
                        val type = if (mimeType.startsWith("image/")) MessageType.IMAGE else if (mimeType.startsWith("video/")) MessageType.VIDEO else MessageType.FILE
                        chatRepo.sendMessage((currentScreen as Screen.Chat).chatId, "me", fileUrl, type, fileUrl)
                        chats = fileDb.readChats() // Refresh list chat
                    }
                } catch (e: Exception) { e.printStackTrace() } finally { isUploading = false }
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            isRecording = true
            audioRecorder.startRecording()
        }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    BackHandler(enabled = currentScreen !is Screen.Home) {
        currentScreen = Screen.Home
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            MainScreen(
                currentUser = currentUser,
                chats = chats,
                contacts = contacts,
                stories = emptyList(),
                calls = emptyList(),
                settings = appSettings,
                fileDb = fileDb,
                onChatClick = { chatId -> currentScreen = Screen.Chat(chatId) },
                onSearchClick = { currentScreen = Screen.Search },
                onContactClick = { user -> currentScreen = Screen.UserProfile(user) },
                onNewGroupClick = { currentScreen = Screen.CreateGroup },
                onSettingsClick = { currentScreen = Screen.Settings },
                onAddStoryClick = { /* TODO: Story */ },
                onStoryClick = { /* TODO: Story */ }
            )
        }
        is Screen.Chat -> {
            val chat = chats.firstOrNull { it.chatId == screen.chatId }
            ChatDetailScreen(
                chatId = screen.chatId,
                chat = chat,
                messages = chatMessages,
                currentUserId = "me",
                isContactOnline = isContactOnline,
                isContactTyping = isContactTyping,
                lastSeenTimestamp = lastSeenTimestamp,
                fileDb = fileDb,
                chatRepo = chatRepo,
                onBackClicked = { currentScreen = Screen.Home },
                onEditMessage = { msg, newText ->
                    scope.launch { chatRepo.editMessage(screen.chatId, msg.msgId, newText) }
                },
                onReactMessage = { msg, emoji ->
                    scope.launch { chatRepo.reactToMessage(screen.chatId, msg.msgId, "me", emoji) }
                },
                onDeleteMessage = { msg ->
                    scope.launch { chatRepo.deleteMessage(screen.chatId, msg.msgId) }
                },
                onHeaderClick = {
                    val user = contacts.firstOrNull { it.uid == screen.chatId } ?: currentUser
                    if (user != null) currentScreen = Screen.UserProfile(user)
                },
                onTyping = { typing -> chatRepo.setTypingStatus(screen.chatId, "me", typing) },
                onSendMessage = { text ->
                    scope.launch { chatRepo.sendMessage(screen.chatId, "me", text) }
                },
                onAttachClick = { option ->
                    when (option) {
                        "Gallery" -> filePicker.launch("image/*")
                        "Video" -> filePicker.launch("video/*")
                        "Music" -> filePicker.launch("audio/*")
                        else -> filePicker.launch("*/*")
                    }
                },
                onMicClick = {
                    if (isRecording) {
                        val file = audioRecorder.stopRecording()
                        isRecording = false
                        if (file != null && file.exists()) {
                            scope.launch {
                                try {
                                    val inputStream = file.inputStream()
                                    val fileUrl = filebase.uploadFile(file.name, "audio/m4a", inputStream)
                                    inputStream.close()
                                    chatRepo.sendMessage(screen.chatId, "me", fileUrl, MessageType.VOICE, fileUrl)
                                    chats = fileDb.readChats() // Refresh list chat
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            isRecording = true
                            audioRecorder.startRecording()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                currentUser = currentUser,
                settings = appSettings,
                onEditProfileClick = { currentScreen = Screen.EditProfile },
                onPrivacyClick = { currentScreen = Screen.Privacy },
                onNotificationClick = { currentScreen = Screen.Notification },
                onDataStorageClick = { currentScreen = Screen.DataStorage },
                onChatSettingsClick = { currentScreen = Screen.ChatSettings },
                onLanguageClick = { currentScreen = Screen.Language },
                onDevicesClick = { currentScreen = Screen.Devices },
                onSaveSettings = { newSettings ->
                    appSettings = newSettings
                    scope.launch { fileDb.writeSettings(newSettings) }
                },
                onLogoutClick = {
                    currentScreen = Screen.Home
                },
                onDeleteAccountClick = {
                    scope.launch {
                        fileDb.writeChats(emptyList())
                        fileDb.writeSettings(AppSettings())
                        currentScreen = Screen.Home
                    }
                }
            )
        }
        is Screen.Privacy -> {
            PrivacySettingsScreen(
                settings = appSettings,
                onSaveSettings = { newSettings ->
                    appSettings = newSettings
                    scope.launch { fileDb.writeSettings(newSettings) }
                },
                onBackClick = { currentScreen = Screen.Settings }
            )
        }
        is Screen.Notification -> {
            NotificationSettingsScreen(
                settings = appSettings,
                onSaveSettings = { newSettings ->
                    appSettings = newSettings
                    scope.launch { fileDb.writeSettings(newSettings) }
                },
                onBackClick = { currentScreen = Screen.Settings }
            )
        }
        is Screen.EditProfile -> {
            EditProfileScreen(
                currentUser = currentUser,
                onSaveProfile = { fName, lName, bio, avatar ->
                    currentUser?.let { user ->
                        val updated = user.copy(firstName = fName, lastName = lName, bio = bio, avatarUrl = avatar)
                        currentUser = updated
                        scope.launch { fileDb.upsertUser(updated) }
                        currentScreen = Screen.Settings
                    }
                },
                onBackClick = { currentScreen = Screen.Settings }
            )
        }
        is Screen.DataStorage -> {
            DataStorageSettingsScreen(
                settings = appSettings,
                onSaveSettings = { newSettings ->
                    appSettings = newSettings
                    scope.launch { fileDb.writeSettings(newSettings) }
                },
                onBackClick = { currentScreen = Screen.Settings }
            )
        }
        is Screen.ChatSettings -> {
            ChatAppearanceSettingsScreen(
                settings = appSettings,
                onSaveSettings = { newSettings ->
                    appSettings = newSettings
                    scope.launch { fileDb.writeSettings(newSettings) }
                },
                onBackClick = { currentScreen = Screen.Settings }
            )
        }
        is Screen.Language -> {
            LanguageSettingsScreen(
                settings = appSettings,
                onSaveSettings = { newSettings ->
                    appSettings = newSettings
                    scope.launch { fileDb.writeSettings(newSettings) }
                },
                onBackClick = { currentScreen = Screen.Settings }
            )
        }
        is Screen.Devices -> {
            DevicesSettingsScreen(
                settings = appSettings,
                onSaveSettings = { newSettings ->
                    appSettings = newSettings
                    scope.launch { fileDb.writeSettings(newSettings) }
                },
                onBackClick = { currentScreen = Screen.Settings }
            )
        }
        is Screen.CreateGroup -> {
            CreateGroupScreen(
                contacts = contacts,
                onGroupCreated = { groupName, selectedUids ->
                    scope.launch {
                        val newChatId = "group_${System.currentTimeMillis()}"
                        val newChat = Chat(
                            chatId = newChatId,
                            type = "group",
                            title = groupName,
                            memberUids = selectedUids + "me"
                        )
                        fileDb.upsertChat(newChat)
                        chats = fileDb.readChats()
                        currentScreen = Screen.Home
                    }
                },
                onBackClick = { currentScreen = Screen.Home }
            )
        }
        is Screen.Search -> {
            SearchScreen(
                chats = chats,
                contacts = contacts,
                fileDb = fileDb,
                onChatClick = { chatId -> currentScreen = Screen.Chat(chatId) },
                onContactClick = { user -> currentScreen = Screen.UserProfile(user) }
            )
        }
        is Screen.UserProfile -> {
            OtherProfileScreen(
                user = screen.user,
                onBackClick = { currentScreen = Screen.Home },
                onSendMessageClick = { currentScreen = Screen.Chat(screen.user.uid) },
                onVoiceCallClick = { /* TODO: Voice Call */ },
                onVideoCallClick = { /* TODO: Video Call */ }
            )
        }
    }
}
