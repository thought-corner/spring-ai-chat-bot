package com.study.spring_ai_chat_bot.config

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * RAG 벡터 스토어(임베딩 저장/검색 인프라)를 구성하는 설정.
 *
 * 벡터 스토어는 ETL의 Load 단계(청크 적재)와 질의 시점의 retrieval 양쪽에서 공유되는
 * 교체 가능한 인프라 빈이므로, ETL([RagConfig])이나 채팅([ChatConfig]) 관심사와 분리해 별도로 둔다.
 * 구현 교체(in-memory ↔ pgvector 등)는 [ConditionalOnProperty]로 프로퍼티에 따라 전환한다.
 */
@Configuration
class VectorStoreConfig {

    /**
     * 애플리케이션 메모리 내에 임베딩을 보관하는 개발/학습용 벡터 스토어.
     * `app.vectorstore.in-memory.enabled=true`일 때만 빈으로 등록된다.
     * 운영 환경에서는 pgvector 등 영속 구현으로 교체 예정.
     *
     * @param embeddingModel 문서/질의를 벡터로 변환할 임베딩 모델
     * @return 인메모리 [VectorStore]
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "app.vectorstore.in-memory",
        name = ["enabled"],
        havingValue = "true",
    )
    fun inMemoryVectorStore(embeddingModel: EmbeddingModel): VectorStore =
        SimpleVectorStore.builder(embeddingModel).build()
}