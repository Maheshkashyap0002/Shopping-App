package com.shoppingappmahesh.ui.screens.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.ChatMessage
import com.shoppingappmahesh.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val chatHistory = chatRepository.getChatHistory()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.sendMessage(message)
            _isLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearChat()
        }
    }
}