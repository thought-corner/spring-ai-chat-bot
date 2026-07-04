package com.study.spring_ai_chat_bot.service

import com.study.spring_ai_chat_bot.controller.dto.RagChatRequest
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class RagChatService(
    chatClientBuilder: ChatClient.Builder,
    advisors: Array<Advisor>,
    private val promptFactory: PromptFactory,
) {
    private val chatClient: ChatClient =
        chatClientBuilder
            .defaultOptions(ChatOptions.builder().temperature(0.0))
            .defaultAdvisors(*advisors)
            .build()

    fun createPrompt(request: RagChatRequest): Prompt =
        promptFactory.create(request.userPrompt, request.systemPrompt, request.chatOptions)

    fun call(prompt: Prompt, conversationId: String, filterExpression: String?): ChatResponse? =
        prepareRequest(prompt, conversationId, filterExpression)
            .call()
            .chatResponse()

    fun stream(prompt: Prompt, conversationId: String, filterExpression: String?): Flux<String>? =
        prepareRequest(prompt, conversationId, filterExpression)
            .stream()
            .content()

    /**
     * 대화 컨텍스트(conversationId)와 메타데이터 필터([VectorStoreDocumentRetriever.FILTER_EXPRESSION])를
     * advisor 파라미터로 주입해 요청을 준비한다. 필터가 없으면 빈 문자열로 전달해 필터링을 건너뛴다.
     *
     * @param filterExpression 벡터 검색에 적용할 메타데이터 필터 표현식 (없으면 null)
     */
    private fun prepareRequest(
        prompt: Prompt,
        conversationId: String,
        filterExpression: String?,
    ): ChatClient.ChatClientRequestSpec =
        chatClient.prompt(prompt)
            .advisors { advisorSpec ->
                advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId)
            }
            .advisors { advisorSpec ->
                advisorSpec.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpression ?: "")
            }
}