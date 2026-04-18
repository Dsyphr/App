package io.github.dsyphr.core.model

import java.util.Date

data class Chat(
    val id: String,
    val participants: List<String>,
    val lastMessage: Message? = null,
    val updatedAt: Date = Date()
)
