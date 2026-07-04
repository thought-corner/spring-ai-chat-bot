package com.study.spring_ai_chat_bot.service

import com.study.spring_ai_chat_bot.controller.dto.ChatOptionsRequest
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Component

/**
 * 요청 필드(system/user 프롬프트, chatOptions)로부터 [Prompt]를 조립하는 단일 책임 컴포넌트.
 *
 * 프롬프트 조립은 HTTP 처리(컨트롤러)나 채팅 클라이언트 호출(서비스)과 구분되는 별개의 관심사이므로,
 * 여기로 분리해 여러 서비스가 중복 없이 재사용한다.
 */
@Component
class PromptFactory {

    /**
     * systemPrompt가 있으면 시스템 메시지로, userPrompt는 항상 사용자 메시지로 담아 프롬프트를 만든다.
     * chatOptions가 주어지면 그대로 반영한다.
     */
    fun create(
        userPrompt: String,
        systemPrompt: String?,
        chatOptions: ChatOptionsRequest?,
    ): Prompt {
        val messages = mutableListOf<Message>()
        if (!systemPrompt.isNullOrBlank()) {
            messages.add(SystemMessage(systemPrompt))
        }
        messages.add(UserMessage(userPrompt))

        val promptBuilder = Prompt.builder().messages(messages)
        chatOptions?.let { promptBuilder.chatOptions(it.toChatOptions()) }
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
}