package io.github.dsyphr.core.repository

import io.github.dsyphr.core.model.Chat
import io.github.dsyphr.core.model.Message
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val chats: StateFlow<Map<String, Chat>>
    suspend fun createChat(participant1: String, participant2: String): Result<String>
    suspend fun getChat(participant1: String, participant2: String): Result<Chat?>
    suspend fun getChatsForUser(userId: String): Result<List<Chat>>
    suspend fun getLastMessage(chatId: String): Result<Message?>
    suspend fun updateLastMessage(chatId: String, message: Message): Result<Unit>
    suspend fun deleteChat(chatId: String): Result<Unit>
    fun dispose()
}
