package com.study.spring_ai_chat_bot.config

import com.study.spring_ai_chat_bot.utils.TextSplitter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.document.DocumentReader
import org.springframework.ai.document.DocumentTransformer
import org.springframework.ai.document.DocumentWriter
import org.springframework.ai.model.transformer.KeywordMetadataEnricher
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import tools.jackson.databind.ObjectMapper

private val logger = KotlinLogging.logger { }

@Configuration
class RagConfig {

    /**
     * 지정한 경로 패턴([documentsLocationPattern])에 매칭되는 파일들을 찾아
     * 각각을 [TikaDocumentReader]로 감싼 리더 목록을 생성한다. (ETL의 Extract 단계 소스)
     *
     * @param documentsLocationPattern 읽어들일 문서의 위치 패턴 (예: classpath 하위 docs 폴더의 모든 파일)
     * @return 파일별 [DocumentReader] 목록
     */
    @Bean
    fun documentReaders(
        @Value("\${app.rag.documents-location-pattern}") documentsLocationPattern: String,
    ): List<DocumentReader> {
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources(documentsLocationPattern)
        return resources.map { TikaDocumentReader(it) }
    }

    /**
     * 문서를 최대 200자 단위로 자르되 100자를 겹치게 분할하는 [TextSplitter] 기반 변환기.
     * (ETL의 Transform 단계 — 청크 분할)
     *
     * @return 길이 기반 분할을 수행하는 [DocumentTransformer]
     */
    @Bean
    fun textSplitter(): DocumentTransformer = TextSplitter(200, 100)

    /**
     * 각 문서 조각에서 LLM([chatModel])으로 키워드 4개를 추출해 메타데이터에 추가하는 변환기.
     * (ETL의 Transform 단계 — 메타데이터 보강)
     *
     * @param chatModel 키워드 추출에 사용할 채팅 모델
     * @return 키워드 메타데이터를 채우는 [DocumentTransformer]
     */
    @Bean
    fun keywordMetadataEnricher(chatModel: ChatModel): DocumentTransformer =
        KeywordMetadataEnricher(chatModel, 4)

    /**
     * 넘어온 문서 조각들을 저장하는 대신, 조각 개수와 예쁘게 포맷된 JSON을 로그로 출력하는 디버그용 writer.
     * (ETL의 Load 단계 — 실제 저장 대신 콘솔 확인용)
     *
     * @param objectMapper JSON 직렬화에 사용할 Jackson [ObjectMapper] (tools.jackson, Jackson 3)
     * @return 문서를 로그로 출력하는 [DocumentWriter]
     */
    @Bean
    fun jsonConsoleDocumentWriter(objectMapper: ObjectMapper): DocumentWriter =
        DocumentWriter { documents ->
            logger.info { "======= 저장할 문서 조각(Chunk) 개수: ${documents.size} ========" }
            try {
                val jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(documents)
                logger.info { jsonString }
            } catch (e: Exception) {
                logger.error(e) { "JSON 변환 중 에러가 발생했습니다" }
            }
            logger.info { "======================================================" }
        }
}
