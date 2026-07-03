package com.study.spring_ai_chat_bot.service

import com.study.spring_ai_chat_bot.controller.dto.ChatOptionsRequest
import com.study.spring_ai_chat_bot.controller.dto.ChatRequest
import com.study.spring_ai_chat_bot.domain.InquiryEvaluation
import com.study.spring_ai_chat_bot.service.dto.InquiryEvaluationResult
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatService(
    chatClientBuilder: ChatClient.Builder,
    advisors: Array<Advisor>
) {
    private val chatClient: ChatClient =
        chatClientBuilder.defaultAdvisors(*advisors).build()

    fun stream(prompt: Prompt, conversationId: String): Flux<String>? {
        return prepareRequest(prompt, conversationId)
            .stream()
            .content()
    }

    fun call(prompt: Prompt, conversationId: String): ChatResponse? {
        return prepareRequest(prompt, conversationId)
            .call()
            .chatResponse()
    }

    fun inquiryEvaluation(prompt: Prompt, conversationId: String): InquiryEvaluationResult {
        val evaluation = requireNotNull(
            prepareRequest(prompt, conversationId)
                .call()
                .entity(InquiryEvaluation::class.java),
        ) { "문의 평가 결과를 생성하지 못했습니다." }
        return InquiryEvaluationResult.from(evaluation)
    }

    fun createPrompt(request: ChatRequest): Prompt {
        val messages = mutableListOf<Message>()

        if (!request.systemPrompt.isNullOrBlank()) {
            messages.add(SystemMessage(request.systemPrompt))
        }

        messages.add(UserMessage(request.userPrompt))
        val promptBuilder = Prompt.builder().messages(messages)
        request.chatOptions?.let { promptBuilder.chatOptions(it.toChatOptions()) }
        return promptBuilder.build()
    }

    private fun ChatOptionsRequest.toChatOptions(): ChatOptions =
        ChatOptions.builder()
            .model(model)
            .temperature(temperature)
            .topP(topP)
            .topK(topK)
            .maxTokens(maxTokens)
            .frequencyPenalty(frequencyPenalty)
            .presencePenalty(presencePenalty)
            .stopSequences(stopSequences)
            .build()

    private fun prepareRequest(prompt: Prompt, conversationId: String): ChatClient.ChatClientRequestSpec {
        return chatClient.prompt(prompt)
            .advisors { advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId) }
    }
}