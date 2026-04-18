package io.github.dsyphr.core.model

import java.util.Date

data class Message(
    val id: String = "",
    val text: String,
    val originalText: String,
    val senderId: String,
    val senderUsername: String,
    val timestamp: Date,
    val isRead: Boolean = false
)
