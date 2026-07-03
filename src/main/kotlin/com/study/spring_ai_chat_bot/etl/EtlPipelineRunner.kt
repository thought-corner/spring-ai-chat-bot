package com.study.spring_ai_chat_bot.etl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.document.DocumentReader
import org.springframework.ai.document.DocumentTransformer
import org.springframework.ai.document.DocumentWriter
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

/**
 * 애플리케이션 기동 시 RAG ETL 파이프라인(Extract → Transform → Load)을 실행하는 러너.
 *
 * `app.etl.pipeline.init=true`일 때만 빈으로 등록되며([ConditionalOnProperty]),
 * [Order] 1로 다른 러너보다 먼저 실행된다. 각 리더로 원본 문서를 읽어(Extract)
 * 분할·키워드 보강(Transform)한 뒤, 등록된 모든 writer에 적재(Load)한다.
 *
 * @property documentReaders 원본 문서 소스 목록 (Extract)
 * @property textSplitter 문서 분할 변환기 (Transform)
 * @property keywordMetadataEnricher 키워드 메타데이터 보강 변환기 (Transform)
 * @property documentWriters 가공된 문서를 적재할 writer 목록 (Load)
 */
@Component
@Order(1)
@ConditionalOnProperty(prefix = "app.etl.pipeline", name = ["init"], havingValue = "true")
class EtlPipelineRunner(
    private val documentReaders: List<DocumentReader>,
    private val textSplitter: DocumentTransformer,
    private val keywordMetadataEnricher: DocumentTransformer,
    private val documentWriters: List<DocumentWriter>,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        logger.info { "[System] ETL 파이프라인 가동 시작" }

        documentReaders.forEach { reader ->
            // 1. Extract: 원본 파일에서 텍스트 덩어리 읽어오기
            val rawDocuments = reader.get()
            logger.info { "[Extract] 파일 읽기 완료" }

            // 2. Transform: 조각으로 분할 후 키워드 메타데이터 보강
            val chunkedDocuments = textSplitter.apply(rawDocuments)
            logger.info { "[Transform] 문서 분할 완료" }
            val enrichedDocuments = keywordMetadataEnricher.apply(chunkedDocuments)

            // 3. Load: 가공된 조각들을 등록된 모든 저장소에 적재
            documentWriters.forEach { writer -> writer.accept(enrichedDocuments) }
            logger.info { "[Load] 저장소 적재 완료" }
        }

        logger.info { "[System] ETL 파이프라인 적재 종료" }
    }
}
