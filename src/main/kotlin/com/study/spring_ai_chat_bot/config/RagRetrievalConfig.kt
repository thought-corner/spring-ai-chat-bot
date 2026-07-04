package com.study.spring_ai_chat_bot.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 질의(query) 시점의 RAG 검색·증강 파이프라인을 구성하는 설정.
 *
 * ETL 적재([RagConfig])나 스토리지 인프라([VectorStoreConfig])와 달리, 사용자의 질문이 들어왔을 때
 * 벡터 스토어에서 관련 문서를 찾아 프롬프트에 결합하는 retrieval 관심사를 담당한다.
 */
@Configuration
class RagRetrievalConfig {

    companion object {
        const val SIMILARITY = 0.3
        const val TOP_K = 3
    }

    /**
     * 검색기·증강기·쿼리 확장/변환기를 조립한 RAG 검색 [RetrievalAugmentationAdvisor].
     *
     * [Advisor] 타입이므로 별도 배선 없이 `ChatService`가 주입받는 advisor 목록에 자동 수집되어
     * 채팅 클라이언트에 등록된다. [VectorStore] 빈(현재 인메모리)에 의존한다.
     *
     * @param vectorStore 유사 문서 검색 대상 벡터 스토어
     * @param chatClientBuilder 쿼리 확장/번역에 LLM을 호출하기 위한 채팅 클라이언트 빌더
     * @return RAG 검색 증강 어드바이저
     */
    @Bean
    fun retrievalAugmentationAdvisor(
        vectorStore: VectorStore,
        chatClientBuilder: ChatClient.Builder,
    ): RetrievalAugmentationAdvisor {
        // 1. 문서 검색기: 벡터 DB에서 유사도 0.3 이상인 문서를 최대 3개(topK) 검색
        val documentRetriever = VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(SIMILARITY)
            .topK(TOP_K)
            .build()

        // 2. 프롬프트 결합기: 검색된 문서가 없어도 에러 없이 LLM에 유연하게 위임
        val queryAugmenter = ContextualQueryAugmenter.builder()
            .allowEmptyContext(true)
            .build()

        // 3. 쿼리 확장기: 원 질의를 의미가 유사한 여러 쿼리로 확장해 검색 재현율을 높인다
        val queryExpander = MultiQueryExpander.builder()
            .chatClientBuilder(chatClientBuilder)
            .build()

        // 4. 쿼리 변환기: 질의를 한국어로 번역해 문서 언어와 정렬한다
        val queryTransformer = TranslationQueryTransformer.builder()
            .chatClientBuilder(chatClientBuilder)
            .targetLanguage("korean")
            .build()

        // 5. 최종 조립
        return RetrievalAugmentationAdvisor.builder()
            .documentRetriever(documentRetriever)
            .queryAugmenter(queryAugmenter)
            .queryExpander(queryExpander)
            .queryTransformers(queryTransformer)
            .build()
    }
}