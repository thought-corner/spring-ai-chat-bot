package com.study.spring_ai_chat_bot.controller

import com.study.spring_ai_chat_bot.controller.dto.ChatRequest
import com.study.spring_ai_chat_bot.service.ChatService
import jakarta.validation.Valid
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService,
) {

    @PostMapping("/call", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun call(@RequestBody @Valid request: ChatRequest): ChatResponse? {
        val prompt = chatService.createPrompt(request)
        return chatService.call(prompt, request.conversationId)
    }

    @PostMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(@RequestBody @Valid request: ChatRequest): Flux<String>? {
        val prompt = chatService.createPrompt(request)
        return chatService.stream(prompt, request.conversationId)
    }
}