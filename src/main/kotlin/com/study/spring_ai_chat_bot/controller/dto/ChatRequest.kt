package com.study.spring_ai_chat_bot.controller.dto

import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotEmpty

data class ChatRequest(
    @NotEmpty val conversationId: String,
    @NotEmpty val userPrompt: String,
    @Nullable val systemPrompt: String?,
    val chatOptions: ChatOptionsRequest?,
)

data class ChatOptionsRequest(
    val model: String?,
    val temperature: Double?,
    val topP: Double?,
    val topK: Int?,
    val maxTokens: Int?,
    val frequencyPenalty: Double?,
    val presencePenalty: Double?,
    val stopSequences: List<String>?,
)
