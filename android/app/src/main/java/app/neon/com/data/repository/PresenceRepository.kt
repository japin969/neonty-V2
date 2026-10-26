// Neont Messenger - PresenceRepository
// Realtime online presence, last seen, dan typing status via Firebase Realtime Database
package app.neon.com.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    /**
     * Mendaftarkan koneksi user ke Firebase presence.
     * Menggunakan /.info/connected dan onDisconnect() untuk menjamin keakuratan status online.
     */
    fun setupPresence(userId: String) {
        if (userId.isBlank()) return

        val presenceRef = database.getReference("presence").child(userId)
        val connectedRef = database.getReference(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
                    val onlineStatus = mapOf(
                        "isOnline" to true,
                        "lastSeen" to ServerValue.TIMESTAMP
                    )
                    presenceRef.setValue(onlineStatus)

                    // Auto-cleanup status saat aplikasi crash, freeze, atau network terputus
                    val offlineStatus = mapOf(
                        "isOnline" to false,
                        "lastSeen" to ServerValue.TIMESTAMP
                    )
                    presenceRef.onDisconnect().setValue(offlineStatus)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Mengamati status online & waktu terakhir terlihat dari user lain
     */
    fun observeUserPresence(userId: String): Flow<Pair<Boolean, Long>> = callbackFlow {
        val presenceRef = database.getReference("presence").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                val lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                trySend(Pair(isOnline, lastSeen))
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        presenceRef.addValueEventListener(listener)
        awaitClose { presenceRef.removeEventListener(listener) }
    }

    /**
     * Mengatur status mengetik di percakapan
     */
    fun setTyping(chatId: String, userId: String, isTyping: Boolean) {
        val typingRef = database.getReference("typing").child(chatId).child(userId)
        if (isTyping) {
            typingRef.setValue(true)
            typingRef.onDisconnect().removeValue()
        } else {
            typingRef.removeValue()
        }
    }

    /**
     * Mendengarkan daftar user yang sedang mengetik di dalam chatId tertentu
     */
    fun observeTypingUsers(chatId: String, currentUserId: String): Flow<List<String>> = callbackFlow {
        val typingChatRef = database.getReference("typing").child(chatId)
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

        typingChatRef.addValueEventListener(listener)
        awaitClose { typingChatRef.removeEventListener(listener) }
    }
}
