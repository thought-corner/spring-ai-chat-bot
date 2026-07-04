package com.study.spring_ai_chat_bot.controller.dto

import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotEmpty

data class RagChatRequest(
    @NotEmpty val conversationId: String,
    @NotEmpty val userPrompt: String,
    @Nullable val systemPrompt: String?,
    val chatOptions: ChatOptionsRequest?,
    @Nullable val filterExpression: String?,
)