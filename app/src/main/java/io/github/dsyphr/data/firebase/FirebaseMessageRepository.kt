package io.github.dsyphr.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import io.github.dsyphr.core.model.Message
import io.github.dsyphr.core.repository.MessageRepository
import io.github.dsyphr.core.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMessageRepository @Inject constructor(
    private val database: DatabaseReference,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : MessageRepository {
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    override val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()
    
    private val chatListeners = ConcurrentHashMap<String, ValueEventListener>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val allChatsListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
            snapshot.children.forEach { chatSnapshot ->
                val chatId = chatSnapshot.key ?: return@forEach
                subscribeToChatMessages(chatId)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            android.util.Log.e("FirebaseMessageRepo", "All chats listener cancelled: ${error.message}")
        }
    }

    init {
        subscribeToAllChats()
    }

    override fun dispose() {
        // No-op: singleton lifecycle managed by Hilt
    }

    private fun subscribeToAllChats() {
        database.child("chats").addValueEventListener(allChatsListener)
    }
    
    private fun subscribeToChatMessages(chatId: String) {
        val messagesRef = database.child("chats").child(chatId).child("messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                
                snapshot.children.forEach { messageSnapshot ->
                    try {
                        val messageId = messageSnapshot.key ?: return@forEach
                        val senderId = messageSnapshot.child("senderID").getValue(String::class.java) ?: return@forEach
                        val messageText = messageSnapshot.child("message").getValue(String::class.java) ?: ""
                        val originalText = messageSnapshot.child("engMessage")
                            .getValue(String::class.java) ?: messageText
                        
                        val timestampSeconds = messageSnapshot.child("timestamp")
                            .child("seconds")
                            .getValue(Long::class.java) ?: System.currentTimeMillis() / 1000
                        
                        // Fetch username asynchronously
                        scope.launch {
                            val usernameResult = userRepository.getUsernameById(senderId)
                            val senderUsername = usernameResult.getOrNull() ?: "Unknown"
                            
                            val message = Message(
                                id = messageId,
                                text = messageText,
                                originalText = originalText,
                                senderId = senderId,
                                senderUsername = senderUsername,
                                timestamp = Date(timestampSeconds * 1000),
                                isRead = senderId != getCurrentUserId()
                            )
                            
                            // Update messages with new message
                            val currentMessages = _messages.value[chatId]?.toMutableList() ?: mutableListOf()
                            val existingIndex = currentMessages.indexOfFirst { it.id == messageId }
                            if (existingIndex >= 0) {
                                currentMessages[existingIndex] = message
                            } else {
                                currentMessages.add(message)
                            }
                            
                            _messages.value = _messages.value.toMutableMap().apply {
                                this[chatId] = currentMessages.sortedBy { it.timestamp }
                            }
                        }
                    } catch (e: Exception) {
                        // Skip invalid messages
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FirebaseMessageRepo",
                    "Chat messages listener cancelled for $chatId: ${error.message}")
            }
        }
        
        messagesRef.addValueEventListener(listener)
        chatListeners[chatId] = listener
    }

    override suspend fun getMessagesForChat(chatId: String): Result<List<Message>> {
        val userId = getCurrentUserId()
        return if (userId.isEmpty()) {
            Result.success(emptyList())
        } else {
            try {
                val snapshot = database.child("chats").child(chatId).child("messages").get().await()
                val messagesList = mutableListOf<Message>()
                
                snapshot.children.forEach { messageSnapshot ->
                    val messageId = messageSnapshot.key ?: return@forEach
                    val senderId = messageSnapshot.child("senderID").getValue(String::class.java) ?: return@forEach
                    val messageText = messageSnapshot.child("message").getValue(String::class.java) ?: ""
                    val originalText = messageSnapshot.child("engMessage").getValue(String::class.java) ?: messageText
                    
                    val timestampSeconds = messageSnapshot.child("timestamp")
                        .child("seconds")
                        .getValue(Long::class.java) ?: System.currentTimeMillis() / 1000
                    
                    // Resolve username
                    val usernameResult = userRepository.getUsernameById(senderId)
                    val senderUsername = usernameResult.getOrNull() ?: "Unknown"
                    
                    val message = Message(
                        id = messageId,
                        text = messageText,
                        originalText = originalText,
                        senderId = senderId,
                        senderUsername = senderUsername,
                        timestamp = Date(timestampSeconds * 1000),
                        isRead = senderId != userId
                    )
                    messagesList.add(message)
                }
                
                Result.success(messagesList.sortedBy { it.timestamp })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun sendMessage(chatId: String, text: String, originalText: String): Result<String> {
        val userId = getCurrentUserId()
        return if (userId.isEmpty()) {
            Result.failure(Exception("User not logged in"))
        } else {
            try {
                val messageId = database.child("chats").child(chatId).child("messages").push().key 
                    ?: return Result.failure(Exception("Failed to generate message ID"))
                
                val messageData = mapOf(
                    "id" to messageId,
                    "senderID" to userId,
                    "message" to text,
                    "engMessage" to originalText,
                    "timestamp" to ServerValue.TIMESTAMP,
                    "isRead" to false
                )
                
                val updates = mapOf(
                    "chats" to mapOf(
                        chatId to mapOf(
                            "messages" to mapOf(messageId to messageData),
                            "lastmessage" to mapOf(
                                "senderID" to userId,
                                "message" to text,
                                "engMessage" to originalText,
                                "timestamp" to ServerValue.TIMESTAMP
                            )
                        )
                    )
                )
                
                database.updateChildren(updates).await()
                Result.success(messageId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun markAsRead(chatId: String, messageId: String): Result<Unit> {
        val userId = getCurrentUserId()
        return if (userId.isEmpty()) {
            Result.failure(Exception("User not logged in"))
        } else {
            try {
                val updates = mapOf(
                    "chats/$chatId/messages/$messageId/isRead" to true
                )
                database.updateChildren(updates).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun sendMessageToChat(chatId: String, message: Message): Result<Unit> {
        return try {
            val updates = mapOf(
                "chats/$chatId/messages/${message.id}" to mapOf(
                    "id" to message.id,
                    "senderID" to message.senderId,
                    "message" to message.originalText,
                    "engMessage" to message.text,
                    "timestamp" to ServerValue.TIMESTAMP,
                    "isRead" to message.isRead
                )
            )
            database.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
