package io.github.dsyphr.core.repository

import io.github.dsyphr.core.model.Message
import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    val messages: StateFlow<Map<String, List<Message>>>
    suspend fun getMessagesForChat(chatId: String): Result<List<Message>>
    suspend fun sendMessage(chatId: String, text: String, originalText: String): Result<String>
    suspend fun markAsRead(chatId: String, messageId: String): Result<Unit>
    suspend fun sendMessageToChat(chatId: String, message: Message): Result<Unit>
    fun dispose()
}
