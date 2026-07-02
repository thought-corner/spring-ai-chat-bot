package com.study.spring_ai_chat_bot.config

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatConfig {

    /**
     * 클라이언트가 작성한 프롬프트가 실제로 어떤 형태의 JSON으로 변환되어 AI에게 날아가는지?
     * AI가 대답을 생성하는 데 걸린 시간과 정확한 응답 데이터가 무엇인지를 디버깅 콘솔에 전부 찍는 옵션
     */
    @Bean
    fun simpleLoggerAdvisor() : SimpleLoggerAdvisor {
        return SimpleLoggerAdvisor.builder().build()
    }

    /**
     * API를 통해 백엔드 서버에서 AI 직접 호출 시, LLM API는 무상태로 동작(즉, 기억을 하지 못한다.)
     * 새로운 질문을 던질 때마다 과거에 나눴던 대화 컨텍스트 전체를 프롬프트에 덧붙여서 같이 보내야 한다.
     */
    @Bean
    fun chatMemory() : ChatMemory {
        return MessageWindowChatMemory.builder().maxMessages(10).build()
    }

    /**
     * 해당 Advisor가 Controller에 도달하기 전에 가로채서 기존 대화 기록을 끼워 넣어 최종 프롬프트를 AI에게 대신 전달해준다.
     */
    @Bean
    fun messageChatMemoryAdvisor() : MessageChatMemoryAdvisor {
        return MessageChatMemoryAdvisor.builder(chatMemory()).build()
    }
}