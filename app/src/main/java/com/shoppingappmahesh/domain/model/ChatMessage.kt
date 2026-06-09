package com.shoppingappmahesh.domain.model

enum class Participant {
    USER, MODEL, ERROR
}

data class ChatMessage(
    val text: String,
    val participant: Participant = Participant.USER,
    val isPending: Boolean = false
)