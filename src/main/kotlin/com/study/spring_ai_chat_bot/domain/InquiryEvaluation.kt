package com.study.spring_ai_chat_bot.domain

import com.study.spring_ai_chat_bot.constants.Category
import com.study.spring_ai_chat_bot.constants.Urgency

data class InquiryEvaluation(
    val category: Category,
    val urgency: Urgency,
    val keywords: List<String>,
)
