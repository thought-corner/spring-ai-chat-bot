package com.study.spring_ai_chat_bot.controller.dto

import com.study.spring_ai_chat_bot.constants.Category
import com.study.spring_ai_chat_bot.constants.Urgency
import com.study.spring_ai_chat_bot.service.dto.InquiryEvaluationResult

data class InquiryEvaluationResponse(
    val category: Category,
    val urgency: Urgency,
    val keywords: List<String>,
) {

    companion object {
        fun from(result: InquiryEvaluationResult): InquiryEvaluationResponse =
            InquiryEvaluationResponse(
                category = result.category,
                urgency = result.urgency,
                keywords = result.keywords,
            )
    }
}
