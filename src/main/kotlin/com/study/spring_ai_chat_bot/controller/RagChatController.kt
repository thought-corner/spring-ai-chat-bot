package com.study.spring_ai_chat_bot.controller

import com.study.spring_ai_chat_bot.controller.dto.RagChatRequest
import com.study.spring_ai_chat_bot.service.RagChatService
import jakarta.validation.Valid
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/rag")
class RagChatController(
    private val ragChatService: RagChatService,
) {

    @PostMapping("/call", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun call(@RequestBody @Valid request: RagChatRequest): ChatResponse? {
        val prompt = ragChatService.createPrompt(request)
        return ragChatService.call(prompt, request.conversationId, request.filterExpression)
    }

    @PostMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(@RequestBody @Valid request: RagChatRequest): Flux<String>? {
        val prompt = ragChatService.createPrompt(request)
        return ragChatService.stream(prompt, request.conversationId, request.filterExpression)
    }
}