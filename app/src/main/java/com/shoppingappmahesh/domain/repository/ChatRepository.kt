package com.shoppingappmahesh.domain.repository

import com.shoppingappmahesh.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatHistory(): Flow<List<ChatMessage>>
    suspend fun sendMessage(userMessage: String): Result<String>
    suspend fun clearChat()
}