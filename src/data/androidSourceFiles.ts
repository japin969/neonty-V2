export interface AndroidFile {
  path: string;
  name: string;
  category: 'config' | 'theme' | 'model' | 'storage' | 'firebase' | 'di' | 'ui' | 'nav' | 'rules';
  content: string;
}

export const ANDROID_FILES: AndroidFile[] = [
  {
    path: 'settings.gradle.kts',
    name: 'settings.gradle.kts',
    category: 'config',
    content: `// Neont Messenger - Root settings configuration
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Neont"
include(":app")
`
  },
  {
    path: 'build.gradle.kts',
    name: 'build.gradle.kts (Project)',
    category: 'config',
    content: `// Neont Messenger - Project-level Gradle build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.google.services) apply false
}
`
  },
  {
    path: 'app/build.gradle.kts',
    name: 'app/build.gradle.kts',
    category: 'config',
    content: `// Neont Messenger - App module Gradle build file
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    kotlin("kapt")
}

android {
    namespace = "com.neont.messenger"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.neont.messenger"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Jetpack Compose & Material 3
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Firebase BOM (Auth & Realtime Database)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // File-based persistence & JSON Serializer
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Coil for Compose (Image Loading)
    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
`
  },
  {
    path: 'app/src/main/AndroidManifest.xml',
    name: 'AndroidManifest.xml',
    category: 'config',
    content: `<?xml version="1.0" encoding="utf-8"?>
<!-- Neont Messenger Android Manifest with Network & Storage permissions -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name=".NeontApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Neont">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/Theme.Neont">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
`
  },
  {
    path: 'app/src/main/java/com/neont/messenger/theme/Color.kt',
    name: 'Color.kt',
    category: 'theme',
    content: `package com.neont.messenger.theme

import androidx.compose.ui.graphics.Color

val TelegramPrimary = Color(0xFF2481CC)
val TelegramPrimaryDark = Color(0xFF1D6FA5)
val TelegramLightBlue = Color(0xFF5288C1)

val TelegramDarkBackground = Color(0xFF0F1721)
val TelegramDarkSurface = Color(0xFF17212B)
val TelegramDarkHeader = Color(0xFF242F3D)
val TelegramDarkDivider = Color(0xFF101419)

val BubbleOutgoingDark = Color(0xFF2B5278)
val BubbleIncomingDark = Color(0xFF182533)
val BubbleDateBadge = Color(0x66000000)

val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF8B9DAE)
val TextGreenOnline = Color(0xFF4FA0FF)
val TextBadgeCount = Color(0xFFFFFFFF)

val BadgeUnread = Color(0xFF456B9A)
val BadgeMuted = Color(0xFF38434F)
val CheckmarkBlue = Color(0xFF6AB2F2)

val AvatarRed = Color(0xFFE17076)
val AvatarOrange = Color(0xFFFAA774)
val AvatarViolet = Color(0xFFA695E7)
val AvatarGreen = Color(0xFF7BC862)
val AvatarCyan = Color(0xFF6EC9CB)
val AvatarBlue = Color(0xFF65AADD)
val AvatarPink = Color(0xFFEE7AAE)
`
  },
  {
    path: 'app/src/main/java/com/neont/messenger/data/model/Models.kt',
    name: 'Models.kt',
    category: 'model',
    content: `package com.neont.messenger.data.model

import com.google.gson.annotations.SerializedName

enum class MessageStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("sent") SENT,
    @SerializedName("delivered") DELIVERED,
    @SerializedName("read") READ,
    @SerializedName("failed") FAILED
}

enum class MessageType {
    @SerializedName("text") TEXT,
    @SerializedName("voice") VOICE,
    @SerializedName("image") IMAGE,
    @SerializedName("sticker") STICKER,
    @SerializedName("file") FILE
}

data class User(
    val uid: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
)

data class Message(
    val msgId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val mediaDurationSec: Int? = null,
    val waveform: List<Int>? = null,
    val status: MessageStatus = MessageStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val replyToMsgId: String? = null,
    val reactions: Map<String, Int>? = null
)

data class LastMessageInfo(
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L,
    val status: MessageStatus = MessageStatus.SENT,
    val type: MessageType = MessageType.TEXT
)

data class Chat(
    val chatId: String = "",
    val type: String = "direct",
    val title: String = "",
    val avatarUrl: String = "",
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessage: LastMessageInfo? = null,
    val memberUids: List<String> = emptyList(),
    val typingUsers: List<String> = emptyList()
)

data class Story(
    val storyId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val mediaUrl: String = "",
    val timestamp: Long = 0L,
    val caption: String = "",
    val isViewed: Boolean = false
)

data class CallLog(
    val callId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val type: String = "incoming",
    val isVideo: Boolean = false,
    val timestamp: Long = 0L,
    val durationSec: Int = 0
)

data class AppSettings(
    val theme: String = "dark",
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val autoDownloadMedia: Boolean = true,
    val saveToGallery: Boolean = false,
    val cacheSizeMb: Int = 24,
    val lastBackupTimestamp: Long = 0L
)
`
  },
  {
    path: 'app/src/main/java/com/neont/messenger/data/file/FileDatabaseManager.kt',
    name: 'FileDatabaseManager.kt',
    category: 'storage',
    content: `package com.neont.messenger.data.file

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.neont.messenger.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDatabaseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dbDir = File(context.filesDir, "neont_db").apply { mkdirs() }
    private val mutexMap = mutableMapOf<String, Mutex>()
    private val globalLock = Any()

    private fun mutexFor(fileName: String): Mutex = synchronized(globalLock) {
        mutexMap.getOrPut(fileName) { Mutex() }
    }

    // USERS (users.json)
    suspend fun readUsers(): List<User> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "users.json")
        mutexFor("users.json").withLock {
            readJson(file, object : TypeToken<List<User>>() {}) ?: emptyList()
        }
    }

    suspend fun writeUsers(users: List<User>) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "users.json")
        mutexFor("users.json").withLock {
            writeJson(file, users)
        }
    }

    suspend fun upsertUser(user: User) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "users.json")
        mutexFor("users.json").withLock {
            val current = (readJson<List<User>>(file, object : TypeToken<List<User>>() {}) ?: emptyList()).toMutableList()
            val index = current.indexOfFirst { it.uid == user.uid }
            if (index >= 0) {
                current[index] = user
            } else {
                current.add(user)
            }
            writeJson(file, current)
        }
    }

    // CHATS (chats.json)
    suspend fun readChats(): List<Chat> = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            readJson(file, object : TypeToken<List<Chat>>() {}) ?: emptyList()
        }
    }

    suspend fun writeChats(chats: List<Chat>) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            writeJson(file, chats)
        }
    }

    suspend fun upsertChat(chat: Chat) = withContext(Dispatchers.IO) {
        val file = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            val current = (readJson<List<Chat>>(file, object : TypeToken<List<Chat>>() {}) ?: emptyList()).toMutableList()
            val index = current.indexOfFirst { it.chatId == chat.chatId }
            if (index >= 0) {
                current[index] = chat
            } else {
                current.add(0, chat)
            }
            writeJson(file, current)
        }
    }

    suspend fun deleteChat(chatId: String) = withContext(Dispatchers.IO) {
        val chatsFile = File(dbDir, "chats.json")
        mutexFor("chats.json").withLock {
            val current = (readJson<List<Chat>>(chatsFile, object : TypeToken<List<Chat>>() {}) ?: emptyList()).filterNot { it.chatId == chatId }
            writeJson(chatsFile, current)
        }
        val messagesFile = File(dbDir, "messages_\${chatId}.json")
        mutexFor("messages_\${chatId}.json").withLock {
            if (messagesFile.exists()) messagesFile.delete()
        }
    }

    // MESSAGES (messages_{chatId}.json)
    suspend fun readMessages(chatId: String): List<Message> = withContext(Dispatchers.IO) {
        val fileName = "messages_\${chatId}.json"
        val file = File(dbDir, fileName)
        mutexFor(fileName).withLock {
            readJson(file, object : TypeToken<List<Message>>() {}) ?: emptyList()
        }
    }

    suspend fun writeMessages(chatId: String, messages: List<Message>) = withContext(Dispatchers.IO) {
        val fileName = "messages_\${chatId}.json"
        val file = File(dbDir, fileName)
        mutexFor(fileName).withLock {
            writeJson(file, messages)
        }
    }

    suspend fun appendMessage(chatId: String, message: Message) = withContext(Dispatchers.IO) {
        val fileName = "messages_\${chatId}.json"
        val file = File(dbDir, fileName)
        mutexFor(fileName).withLock {
            val current = (readJson<List<Message>>(file, object : TypeToken<List<Message>>() {}) ?: emptyList()).toMutableList()
            val existingIndex = current.indexOfFirst { it.msgId == message.msgId }
            if (existingIndex >= 0) {
                current[existingIndex] = message
            } else {
                current.add(message)
            }
            writeJson(file, current)
        }
    }

    suspend fun updateMessageStatus(chatId: String, msgId: String, status: MessageStatus) = withContext(Dispatchers.IO) {
        val fileName = "messages_\${chatId}.json"
        val file = File(dbDir, fileName)
        mutexFor(fileName).withLock {
            val current = (readJson<List<Message>>(file, object : TypeToken<List<Message>>() {}) ?: emptyList()).toMutableList()
            val index = current.indexOfFirst { it.msgId == msgId }
            if (index >= 0) {
                current[index] = current[index].copy(status = status)
                writeJson(file, current)
            }
        }
    }

    suspend fun deleteMessage(chatId: String, msgId: String) = withContext(Dispatchers.IO) {
        val fileName = "messages_\${chatId}.json"
        val file = File(dbDir, fileName)
        mutexFor(fileName).withLock {
            val current = (readJson<List<Message>>(file, object : TypeToken<List<Message>>() {}) ?: emptyList()).filterNot { it.msgId == msgId }
            writeJson(file, current)
        }
    }

    // ATOMIC WRITE (.tmp -> rename)
    private fun <T> readJson(file: File, type: TypeToken<T>): T? {
        if (!file.exists()) return null
        return try {
            FileReader(file).use { reader ->
                gson.fromJson<T>(reader, type.type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun <T> writeJson(file: File, data: T) {
        val tempFile = File(file.parentFile, "\${file.name}.tmp")
        try {
            FileWriter(tempFile).use { writer ->
                gson.toJson(data, writer)
            }
            if (tempFile.exists()) {
                if (file.exists()) file.delete()
                tempFile.renameTo(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
        }
    }
}
`
  },
  {
    path: 'app/src/main/java/com/neont/messenger/data/repository/ChatRepository.kt',
    name: 'ChatRepository.kt',
    category: 'firebase',
    content: `package com.neont.messenger.data.repository

import com.google.firebase.database.*
import com.neont.messenger.data.file.FileDatabaseManager
import com.neont.messenger.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val fileDb: FileDatabaseManager
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        try {
            database.setPersistenceEnabled(true)
        } catch (_: Exception) {}
        observeNetworkConnection()
    }

    private fun observeNetworkConnection() {
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
                    repositoryScope.launch { flushPendingMessagesToFirebase() }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    suspend fun getLocalChats(): List<Chat> = fileDb.readChats()

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        text: String,
        type: MessageType = MessageType.TEXT
    ): Message {
        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val pendingMessage = Message(
            msgId = msgId,
            chatId = chatId,
            senderId = senderId,
            text = text,
            type = type,
            status = MessageStatus.PENDING,
            timestamp = timestamp
        )

        // 1. Tulis instan ke JSON lokal
        fileDb.appendMessage(chatId, pendingMessage)

        // 2. Push ke Firebase Realtime Database
        val payload = mapOf(
            "msgId" to msgId,
            "chatId" to chatId,
            "senderId" to senderId,
            "text" to text,
            "type" to type.name,
            "timestamp" to ServerValue.TIMESTAMP,
            "status" to MessageStatus.SENT.name
        )

        database.getReference("messages").child(chatId).child(msgId)
            .setValue(payload)
            .addOnSuccessListener {
                repositoryScope.launch {
                    fileDb.updateMessageStatus(chatId, msgId, MessageStatus.SENT)
                }
            }

        return pendingMessage
    }

    private suspend fun flushPendingMessagesToFirebase() {
        val chats = fileDb.readChats()
        for (chat in chats) {
            val messages = fileDb.readMessages(chat.chatId)
            for (pendingMsg in messages.filter { it.status == MessageStatus.PENDING }) {
                try {
                    val payload = mapOf(
                        "msgId" to pendingMsg.msgId,
                        "chatId" to pendingMsg.chatId,
                        "senderId" to pendingMsg.senderId,
                        "text" to pendingMsg.text,
                        "type" to pendingMsg.type.name,
                        "timestamp" to ServerValue.TIMESTAMP,
                        "status" to MessageStatus.SENT.name
                    )
                    database.getReference("messages").child(chat.chatId).child(pendingMsg.msgId).setValue(payload).await()
                    fileDb.updateMessageStatus(chat.chatId, pendingMsg.msgId, MessageStatus.SENT)
                } catch (_: Exception) {}
            }
        }
    }

    fun getMessagesStream(chatId: String): Flow<List<Message>> = callbackFlow {
        trySend(fileDb.readMessages(chatId))

        val ref = database.getReference("messages").child(chatId)
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(Message::class.java) ?: return
                repositoryScope.launch {
                    val status = if (msg.status == MessageStatus.PENDING) MessageStatus.DELIVERED else msg.status
                    fileDb.appendMessage(chatId, msg.copy(status = status))
                    trySend(fileDb.readMessages(chatId))
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(Message::class.java) ?: return
                repositoryScope.launch {
                    fileDb.appendMessage(chatId, msg)
                    trySend(fileDb.readMessages(chatId))
                }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
`
  },
  {
    path: 'app/src/main/java/com/neont/messenger/data/repository/PresenceRepository.kt',
    name: 'PresenceRepository.kt',
    category: 'firebase',
    content: `package com.neont.messenger.data.repository

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    fun setupPresence(userId: String) {
        val presenceRef = database.getReference("presence").child(userId)
        val connectedRef = database.getReference(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
                    presenceRef.setValue(mapOf("isOnline" to true, "lastSeen" to ServerValue.TIMESTAMP))
                    presenceRef.onDisconnect().setValue(mapOf("isOnline" to false, "lastSeen" to ServerValue.TIMESTAMP))
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeUserPresence(userId: String): Flow<Pair<Boolean, Long>> = callbackFlow {
        val ref = database.getReference("presence").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                trySend(Pair(isOnline, lastSeen))
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun setTyping(chatId: String, userId: String, isTyping: Boolean) {
        val ref = database.getReference("typing").child(chatId).child(userId)
        if (isTyping) {
            ref.setValue(true)
            ref.onDisconnect().removeValue()
        } else {
            ref.removeValue()
        }
    }

    fun observeTypingUsers(chatId: String, currentUserId: String): Flow<List<String>> = callbackFlow {
        val ref = database.getReference("typing").child(chatId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typers = mutableListOf<String>()
                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    if (uid != currentUserId && child.getValue(Boolean::class.java) == true) {
                        typers.add(uid)
                    }
                }
                trySend(typers)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
`
  },
  {
    path: 'app/src/main/java/com/neont/messenger/ui/components/MessageBubble.kt',
    name: 'MessageBubble.kt',
    category: 'ui',
    content: `package com.neont.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neont.messenger.data.model.*
import com.neont.messenger.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isOutgoing) BubbleOutgoingDark else BubbleIncomingDark
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 72.dp, max = 320.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column {
                Text(message.text, color = Color.White, fontSize = 15.sp)
                Row(
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(timeFormat, color = Color(0x99FFFFFF), fontSize = 11.sp)
                    if (isOutgoing) {
                        when (message.status) {
                            MessageStatus.PENDING -> Icon(Icons.Default.Schedule, "Pending", tint = Color(0x99FFFFFF), modifier = Modifier.size(12.dp))
                            MessageStatus.SENT -> Icon(Icons.Default.Check, "Sent", tint = CheckmarkBlue, modifier = Modifier.size(12.dp))
                            MessageStatus.DELIVERED, MessageStatus.READ -> Icon(Icons.Default.DoneAll, "Read", tint = CheckmarkBlue, modifier = Modifier.size(14.dp))
                            MessageStatus.FAILED -> Text("!", color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
`
  },
  {
    path: 'app/src/main/java/com/neont/messenger/ui/screens/ChatDetailScreen.kt',
    name: 'ChatDetailScreen.kt',
    category: 'ui',
    content: `package com.neont.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.neont.messenger.data.model.*
import com.neont.messenger.theme.*
import com.neont.messenger.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    messages: List<Message>,
    currentUserId: String,
    onBackClick: () -> Unit,
    onSendMessage: (String, MessageType) -> Unit,
    onForwardMessages: (targetChatIds: List<String>, messageIds: List<String>) -> Unit = { _, _ -> },
    onTyping: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedMsgIds by remember { mutableStateOf(setOf<String>()) }
    var showForwardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !isSelectionMode) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("\${selectedMsgIds.size} dipilih", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { isSelectionMode = false; selectedMsgIds = emptySet() }) {
                            Icon(Icons.Default.Close, "Batal", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showForwardDialog = true },
                            enabled = selectedMsgIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Share, "Teruskan", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2B5278))
                )
            } else {
                TopAppBar(
                    title = { Text(chat.title, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSelectionMode = true; selectedMsgIds = emptySet() }) {
                            Icon(Icons.Default.Check, "Pilih Pesan", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TelegramDarkHeader)
                )
            }
        },
        containerColor = TelegramDarkBackground
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(messages, key = { it.msgId }) { msg ->
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
                        }
                    )
                }
            }
            if (!isSelectionMode) {
                ComposerBar(
                    onSendMessage = { onSendMessage(it, MessageType.TEXT) },
                    onSendVoice = { onSendMessage("Voice note (0:14)", MessageType.VOICE) },
                    onAttachClick = {},
                    onEmojiClick = {},
                    onTyping = onTyping
                )
            }
        }
    }
}
`
  },
  {
    path: 'app/src/main/java/com/neont/messenger/navigation/NavGraph.kt',
    name: 'NavGraph.kt',
    category: 'nav',
    content: `package com.neont.messenger.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neont.messenger.ui.screens.*

@Composable
fun NeontNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(isUserLoggedIn = true, onNavigateNext = { navController.navigate("main") })
        }
        composable("auth") {
            AuthScreen(onLogin = { _, _ -> navController.navigate("main") }, onRegister = { _, _, _, _ -> navController.navigate("main") })
        }
        composable("main") {
            MainScreen(
                currentUser = null,
                chats = emptyList(),
                stories = emptyList(),
                onChatClick = { chatId -> navController.navigate("chat_detail/\$chatId") },
                onStoryClick = {},
                onLogoutClick = { navController.navigate("auth") }
            )
        }
        composable(
            route = "chat_detail/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            // ChatDetailScreen destination
        }
    }
}
`
  }
];
