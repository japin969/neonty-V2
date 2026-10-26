package app.neon.com.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import app.neon.com.data.crypto.RealMessageCrypto
import app.neon.com.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class FileDatabaseManager(private val context: Context) {
    private val gson = Gson()
    private val dbDir = File(context.filesDir, "neont_db").apply { if (!exists()) mkdirs() }
    private val mutexMap = mutableMapOf<String, Mutex>()
    private val globalLock = Any()

    init { seedInitialData() }

    private fun getMutex(filename: String): Mutex = synchronized(globalLock) {
        mutexMap.getOrPut(filename) { Mutex() }
    }

    private fun <T> readJson(filename: String, typeToken: TypeToken<T>): T? {
        val file = File(dbDir, filename)
        if (!file.exists()) return null
        return try {
            FileReader(file).use { gson.fromJson(it, typeToken.type) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun <T> writeJson(filename: String, data: T) {
        val file = File(dbDir, filename)
        val tempFile = File(dbDir, "${filename}.tmp")
        try {
            FileWriter(tempFile).use { gson.toJson(data, it) }
            if (tempFile.exists()) {
                if (file.exists()) file.delete()
                tempFile.renameTo(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private fun atomicWrite(file: File, data: Any) {
        val tempFile = File(file.parentFile, "${file.name}.tmp")
        try {
            FileWriter(tempFile).use { gson.toJson(data, it) }
            if (tempFile.exists()) {
                if (file.exists()) file.delete()
                tempFile.renameTo(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
        }
    }

    // --- CHATS ---
    suspend fun readChats(): List<Chat> = withContext(Dispatchers.IO) {
        getMutex("chats.json").withLock { readJson("chats.json", object : TypeToken<List<Chat>>() {}) ?: emptyList() }
    }

    suspend fun writeChats(chats: List<Chat>) = withContext(Dispatchers.IO) {
        getMutex("chats.json").withLock { writeJson("chats.json", chats) }
    }

    suspend fun upsertChat(chat: Chat) = withContext(Dispatchers.IO) {
        val current = readChats().toMutableList()
        val idx = current.indexOfFirst { it.chatId == chat.chatId }
        if (idx >= 0) current[idx] = chat else current.add(chat)
        writeChats(current)
    }

    // --- MESSAGES ---
    suspend fun readMessages(chatId: String): List<Message> = withContext(Dispatchers.IO) {
        val filename = "messages_${chatId}.json"
        val encryptedList = getMutex(filename).withLock { readJson(filename, object : TypeToken<List<Message>>() {}) ?: emptyList() }
        encryptedList.map { msg ->
            if (!msg.cipherText.isNullOrBlank() && !msg.iv.isNullOrBlank()) {
                val plain = RealMessageCrypto.decrypt(msg.cipherText, msg.iv, chatId, msg.text)
                msg.copy(text = plain)
            } else msg
        }
    }

    suspend fun appendMessage(chatId: String, message: Message): List<Message> = withContext(Dispatchers.IO) {
        val filename = "messages_${chatId}.json"
        val diskMsg = if (message.cipherText.isNullOrBlank() && message.text.isNotBlank()) {
            val encrypted = RealMessageCrypto.encrypt(message.text, chatId)
            message.copy(text = "", cipherText = encrypted.cipherText, iv = encrypted.iv, encryptedKey = encrypted.encryptedKey)
        } else message

        getMutex(filename).withLock {
            val currentList = readJson(filename, object : TypeToken<MutableList<Message>>() {}) ?: mutableListOf()
            val idx = currentList.indexOfFirst { it.msgId == diskMsg.msgId }
            if (idx >= 0) currentList[idx] = diskMsg else currentList.add(diskMsg)
            writeJson(filename, currentList)

            currentList.map { msg ->
                if (!msg.cipherText.isNullOrBlank() && !msg.iv.isNullOrBlank()) {
                    msg.copy(text = RealMessageCrypto.decrypt(msg.cipherText, msg.iv, chatId, msg.text))
                } else msg
            }
        }
    }

    // --- USERS ---
    suspend fun readUsers(): List<User> = withContext(Dispatchers.IO) {
        getMutex("users.json").withLock { readJson("users.json", object : TypeToken<List<User>>() {}) ?: emptyList() }
    }
    suspend fun writeUsers(users: List<User>) = withContext(Dispatchers.IO) {
        getMutex("users.json").withLock { writeJson("users.json", users) }
    }
    suspend fun upsertUser(user: User) = withContext(Dispatchers.IO) {
        val current = readUsers().toMutableList()
        val idx = current.indexOfFirst { it.uid == user.uid }
        if (idx >= 0) current[idx] = user else current.add(user)
        writeUsers(current)
    }

    // --- STORIES ---
    suspend fun readStories(): List<Story> = withContext(Dispatchers.IO) {
        getMutex("stories.json").withLock { readJson("stories.json", object : TypeToken<List<Story>>() {}) ?: emptyList() }
    }
    suspend fun writeStories(stories: List<Story>) = withContext(Dispatchers.IO) {
        getMutex("stories.json").withLock { writeJson("stories.json", stories) }
    }

    // --- CALLS ---
    suspend fun readCalls(): List<CallLog> = withContext(Dispatchers.IO) {
        getMutex("calls.json").withLock { readJson("calls.json", object : TypeToken<List<CallLog>>() {}) ?: emptyList() }
    }
    suspend fun writeCalls(calls: List<CallLog>) = withContext(Dispatchers.IO) {
        getMutex("calls.json").withLock { writeJson("calls.json", calls) }
    }

    // --- SETTINGS ---
    suspend fun readSettings(): AppSettings = withContext(Dispatchers.IO) {
        getMutex("settings.json").withLock { readJson("settings.json", object : TypeToken<AppSettings>() {}) ?: AppSettings() }
    }
    suspend fun writeSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        getMutex("settings.json").withLock { writeJson("settings.json", settings) }
    }

    private fun seedInitialData() {
        val chatsFile = File(dbDir, "chats.json")
        if (!chatsFile.exists()) {
            val initialChats = listOf(
                Chat("c_pavel", "direct", "Pavel Durov", "", true, false, 2, emptyList(), LastMessageInfo("Native Android Kotlin + Jetpack Compose ready.", "u_pavel", System.currentTimeMillis() - 120000, MessageStatus.READ, MessageType.TEXT)),
                Chat("c_elena", "direct", "Elena Rostova", "", false, false, 0, emptyList(), LastMessageInfo("Voice message (0:14)", "u_elena", System.currentTimeMillis() - 2400000, MessageStatus.READ, MessageType.VOICE)),
                Chat("c_saved", "direct", "Saved Messages", "", true, false, 0, emptyList(), LastMessageInfo("JSON file persistence", "me", System.currentTimeMillis() - 3600000, MessageStatus.SENT, MessageType.TEXT))
            )
            writeJson("chats.json", initialChats)
        }
        val usersFile = File(dbDir, "users.json")
        if (!usersFile.exists()) {
            val initialUsers = listOf(
                User("u_pavel", "Pavel", "Durov", "durov", "", "Founder", "", false, 0L),
                User("u_elena", "Elena", "Rostova", "elena", "", "Designer", "", false, 0L),
                User("me", "You", "User", "me", "", "Hey there!", "", true, System.currentTimeMillis())
            )
            writeJson("users.json", initialUsers)
        }
    }

    // --- FITUR HAPUS & EDIT PESAN ---
    fun deleteMessage(chatId: String, msgId: String) {
        val file = File(dbDir, "messages_${chatId}.json")
        if (!file.exists()) return
        try {
            val currentList: MutableList<Message> = FileReader(file).use { 
                gson.fromJson(it, object : TypeToken<MutableList<Message>>() {}.type) ?: mutableListOf() 
            }
            currentList.removeAll { it.msgId == msgId }
            atomicWrite(file, currentList)
            
            // Refresh preview chat
            val chatsFile = File(dbDir, "chats.json")
            if (chatsFile.exists()) {
                val chats: MutableList<Chat> = FileReader(chatsFile).use {
                    gson.fromJson(it, object : TypeToken<MutableList<Chat>>() {}.type) ?: mutableListOf()
                }
                val index = chats.indexOfFirst { it.chatId == chatId }
                if (index >= 0) {
                    val lastMsg = currentList.lastOrNull()
                    chats[index] = chats[index].copy(
                        lastMessage = lastMsg?.let { LastMessageInfo(it.text, it.senderId, it.timestamp, it.status, it.type) }
                    )
                    atomicWrite(chatsFile, chats)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun updateMessage(chatId: String, msgId: String, newText: String) {
        val file = File(dbDir, "messages_${chatId}.json")
        if (!file.exists()) return
        try {
            val currentList: MutableList<Message> = FileReader(file).use { 
                gson.fromJson(it, object : TypeToken<MutableList<Message>>() {}.type) ?: mutableListOf() 
            }
            val index = currentList.indexOfFirst { it.msgId == msgId }
            if (index >= 0) {
                val encrypted = RealMessageCrypto.encrypt(newText, chatId)
                currentList[index] = currentList[index].copy(
                    text = "",
                    cipherText = encrypted.cipherText,
                    iv = encrypted.iv,
                    encryptedKey = encrypted.encryptedKey
                )
                atomicWrite(file, currentList)
                
                // Refresh preview chat
                val chatsFile = File(dbDir, "chats.json")
                if (chatsFile.exists()) {
                    val chats: MutableList<Chat> = FileReader(chatsFile).use {
                        gson.fromJson(it, object : TypeToken<MutableList<Chat>>() {}.type) ?: mutableListOf()
                    }
                    val chatIndex = chats.indexOfFirst { it.chatId == chatId }
                    if (chatIndex >= 0) {
                        val lastMsg = currentList.lastOrNull()
                        chats[chatIndex] = chats[chatIndex].copy(
                            lastMessage = lastMsg?.let { LastMessageInfo(newText, it.senderId, it.timestamp, it.status, it.type) }
                        )
                        atomicWrite(chatsFile, chats)
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun saveMessages(chatId: String, messages: List<Message>) {
        val file = File(dbDir, "messages_${chatId}.json")
        atomicWrite(file, messages)
    }

    fun saveDraft(chatId: String, draftText: String) {
        val draftsFile = File(dbDir, "drafts.json")
        try {
            val drafts: MutableMap<String, String> = if (draftsFile.exists()) {
                FileReader(draftsFile).use { gson.fromJson(it, object : TypeToken<MutableMap<String, String>>() {}.type) ?: mutableMapOf() }
            } else mutableMapOf()
            if (draftText.isBlank()) drafts.remove(chatId) else drafts[chatId] = draftText
            atomicWrite(draftsFile, drafts)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun getDraft(chatId: String): String {
        val draftsFile = File(dbDir, "drafts.json")
        if (!draftsFile.exists()) return ""
        return try {
            val drafts: Map<String, String>? = FileReader(draftsFile).use { gson.fromJson(it, object : TypeToken<Map<String, String>>() {}.type) }
            drafts?.get(chatId) ?: ""
        } catch (e: Exception) { "" }
    }

    fun setPinnedMessage(chatId: String, msgId: String?) {
        val file = File(dbDir, "pinned.json")
        try {
            val pins: MutableMap<String, String> = if (file.exists()) {
                FileReader(file).use { gson.fromJson(it, object : TypeToken<MutableMap<String, String>>() {}.type) ?: mutableMapOf() }
            } else mutableMapOf()
            if (msgId == null) pins.remove(chatId) else pins[chatId] = msgId
            atomicWrite(file, pins)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun getPinnedMessage(chatId: String): String? {
        val file = File(dbDir, "pinned.json")
        if (!file.exists()) return null
        return try {
            val pins: Map<String, String>? = FileReader(file).use { gson.fromJson(it, object : TypeToken<Map<String, String>>() {}.type) }
            pins?.get(chatId)
        } catch (e: Exception) { null }
    }
}
