package com.study.spring_ai_chat_bot.utils

import org.springframework.ai.transformer.splitter.TextSplitter
import org.springframework.util.StringUtils

class TextSplitter(
    private val chunkSize: Int,
    private val chunkOverlap: Int,
) : TextSplitter() {

    override fun splitText(text: String): List<String> {
        if (!StringUtils.hasText(text)) {
            return emptyList()
        }

        val chunks = mutableListOf<String>()
        val textLength = text.length

        var chunkStart = 0
        while (chunkStart < textLength) {
            val chunkEnd = minOf(chunkStart + chunkSize, textLength)
            chunks.add(text.substring(chunkStart, chunkEnd))

            val nextStart = chunkEnd - chunkOverlap
            if (nextStart <= chunkStart) {
                break
            }
            chunkStart = nextStart
        }
        return chunks
    }
}
