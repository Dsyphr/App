package io.github.dsyphr.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import io.github.dsyphr.core.model.Chat
import io.github.dsyphr.core.model.Message
import io.github.dsyphr.core.repository.ChatRepository
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseChatRepository @Inject constructor(
    private val database: DatabaseReference,
    private val auth: FirebaseAuth
) : ChatRepository {
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    private val _chats = MutableStateFlow<Map<String, Chat>>(emptyMap())
    override val chats: StateFlow<Map<String, Chat>> = _chats.asStateFlow()

    private var activeUserId: String? = null
    private var userChatsRef: DatabaseReference? = null

    private val chatListeners = ConcurrentHashMap<String, ValueEventListener>()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        rebindUserChats(firebaseAuth.currentUser?.uid)
    }

    // Listen to user's chats (stored under users/{currentUserId}/chats).
    private val userChatsListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val chatIds = snapshot.children.mapNotNull { it.key }.toSet()

            chatListeners.keys
                .toList()
                .filter { it !in chatIds }
                .forEach { chatId ->
                    unsubscribeFromChat(chatId)
                }

            chatIds.forEach { chatId ->
                subscribeToChat(chatId)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("FirebaseChatRepo", "User chats listener cancelled: ${error.message}")
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        rebindUserChats(auth.currentUser?.uid)
    }

    override fun dispose() {
        auth.removeAuthStateListener(authStateListener)
        userChatsRef?.removeEventListener(userChatsListener)
        chatListeners.keys.toList().forEach { chatId ->
            unsubscribeFromChat(chatId)
        }
        _chats.value = emptyMap()
    }

    private fun rebindUserChats(userId: String?) {
        if (activeUserId == userId) return

        userChatsRef?.removeEventListener(userChatsListener)
        userChatsRef = null

        chatListeners.keys.toList().forEach { chatId ->
            unsubscribeFromChat(chatId)
        }

        _chats.value = emptyMap()
        activeUserId = userId

        if (userId.isNullOrBlank()) return

        userChatsRef = database.child("users").child(userId).child("chats")
        userChatsRef?.addValueEventListener(userChatsListener)
    }

    private fun unsubscribeFromChat(chatId: String) {
        val listener = chatListeners.remove(chatId) ?: return
        database.child("chats").child(chatId).removeEventListener(listener)
        _chats.value = _chats.value.toMutableMap().apply {
            remove(chatId)
        }
    }

    private fun parseTimestampMillis(snapshot: DataSnapshot): Long {
        val value = snapshot.value
        return when (value) {
            is Number -> value.toLong()
            is Map<*, *> -> {
                val seconds = (value["seconds"] as? Number)?.toLong()
                val nanoseconds = (value["nanoseconds"] as? Number)?.toLong() ?: 0L
                if (seconds != null) {
                    (seconds * 1000L) + (nanoseconds / 1_000_000L)
                } else {
                    System.currentTimeMillis()
                }
            }

            else -> System.currentTimeMillis()
        }
    }

    private fun parseParticipants(snapshot: DataSnapshot): List<String> {
        val value = snapshot.value
        return when (value) {
            is List<*> -> value.filterIsInstance<String>()
            is Map<*, *> -> value.values.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    private fun subscribeToChat(chatId: String) {
        if (chatListeners.containsKey(chatId)) return

        val chatRef = database.child("chats").child(chatId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    _chats.value = _chats.value.toMutableMap().apply {
                        remove(chatId)
                    }
                    return
                }

                val lastMessageSnapshot = snapshot.child("lastmessage")
                val participants = parseParticipants(snapshot.child("participants"))

                val lastMessage = if (lastMessageSnapshot.exists()) {
                    val senderId = lastMessageSnapshot.child("senderID").getValue(String::class.java) ?: ""
                    val messageText = lastMessageSnapshot.child("message").getValue(String::class.java) ?: ""
                    val originalText = lastMessageSnapshot.child("engMessage").getValue(String::class.java) ?: messageText
                    val timestampMillis = parseTimestampMillis(lastMessageSnapshot.child("timestamp"))

                    Message(
                        id = "",
                        text = messageText,
                        originalText = originalText,
                        senderId = senderId,
                        senderUsername = "Unknown",
                        timestamp = Date(timestampMillis),
                        isRead = senderId != getCurrentUserId()
                    )
                } else {
                    null
                }

                val updatedAtMillis = lastMessage?.timestamp?.time
                    ?: parseTimestampMillis(snapshot.child("createdAt"))

                _chats.value = _chats.value.toMutableMap().apply {
                    this[chatId] = Chat(
                        id = chatId,
                        participants = participants,
                        lastMessage = lastMessage,
                        updatedAt = Date(updatedAtMillis)
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseChatRepo", "Chat listener cancelled for $chatId: ${error.message}")
            }
        }

        chatRef.addValueEventListener(listener)
        chatListeners[chatId] = listener
    }

    override suspend fun getChatsForUser(userId: String): Result<List<Chat>> {
        return try {
            // Only return chats for the current user
            if (userId != getCurrentUserId()) {
                return Result.success(emptyList())
            }
            
            val userChats = mutableListOf<Chat>()
            
            _chats.value.values.forEach { chat ->
                if (chat.participants.contains(userId)) {
                    userChats.add(chat)
                }
            }
            
            Result.success(userChats.sortedByDescending { it.updatedAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createChat(participant1: String, participant2: String): Result<String> {
        return try {
            val chatId = if (participant1 < participant2) {
                "$participant1.$participant2"
            } else {
                "$participant2.$participant1"
            }
            
            val chatRef = database.child("chats").child(chatId)
            chatRef.setValue(mapOf(
                "participants" to listOf(participant1, participant2),
                "createdAt" to ServerValue.TIMESTAMP
            )).await()
            
            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChat(participant1: String, participant2: String): Result<Chat?> {
        return try {
            val chatId = if (participant1 < participant2) {
                "$participant1.$participant2"
            } else {
                "$participant2.$participant1"
            }
            
            val snapshot = database.child("chats").child(chatId).get().await()
            
            if (snapshot.exists()) {
                val lastMessageSnapshot = snapshot.child("lastmessage")
                var lastMessage: Message? = null
                
                if (lastMessageSnapshot.exists()) {
                    val senderId = lastMessageSnapshot.child("senderID").getValue(String::class.java) ?: ""
                    val messageText = lastMessageSnapshot.child("message").getValue(String::class.java) ?: ""
                    val originalText = lastMessageSnapshot.child("engMessage").getValue(String::class.java) ?: messageText
                    val timestampMillis = parseTimestampMillis(lastMessageSnapshot.child("timestamp"))
                    
                    lastMessage = Message(
                        id = "",
                        text = messageText,
                        originalText = originalText,
                        senderId = senderId,
                        senderUsername = "Unknown",
                        timestamp = Date(timestampMillis),
                        isRead = senderId != getCurrentUserId()
                    )
                }

                val participants = parseParticipants(snapshot.child("participants"))
                    .ifEmpty { listOf(participant1, participant2) }
                
                Result.success(Chat(chatId, participants, lastMessage))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastMessage(chatId: String): Result<Message?> {
        return try {
            val snapshot = database.child("chats").child(chatId).child("lastmessage").get().await()
            
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            
            val senderId = snapshot.child("senderID").getValue(String::class.java) ?: ""
            val messageText = snapshot.child("message").getValue(String::class.java) ?: ""
            val originalText = snapshot.child("engMessage").getValue(String::class.java) ?: messageText
            val timestampMillis = parseTimestampMillis(snapshot.child("timestamp"))
            
            val message = Message(
                id = "",
                text = messageText,
                originalText = originalText,
                senderId = senderId,
                senderUsername = "Unknown",
                timestamp = Date(timestampMillis),
                isRead = senderId != getCurrentUserId()
            )
            
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            val updates = mapOf(
                "chats/$chatId/lastmessage" to mapOf(
                    "senderID" to message.senderId,
                    "message" to message.originalText,
                    "engMessage" to message.text,
                    "timestamp" to ServerValue.TIMESTAMP
                )
            )
            database.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "chats/$chatId" to null
            )
            database.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
