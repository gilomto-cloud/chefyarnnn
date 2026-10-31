package com.trynite.chefyarnnn.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChatSender {
    USER,
    PINOY_CHEF
}

@Serializable
data class ChatMessage(
    val id: String,
    val sender: ChatSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val voiceAudioUri: String? = null,
    val recipeAttachment: Recipe? = null
)
