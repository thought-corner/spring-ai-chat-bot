package com.study.spring_ai_chat_bot.service.dto

import com.study.spring_ai_chat_bot.constants.Category
import com.study.spring_ai_chat_bot.constants.Urgency
import com.study.spring_ai_chat_bot.domain.InquiryEvaluation

data class InquiryEvaluationResult(
    val category: Category,
    val urgency: Urgency,
    val keywords: List<String>,
) {

    companion object {
        fun from(evaluation: InquiryEvaluation): InquiryEvaluationResult =
            InquiryEvaluationResult(
                category = evaluation.category,
                urgency = evaluation.urgency,
                keywords = evaluation.keywords,
            )
    }
}
