package com.example.project180.Model

data class SimilarityRequest(val sentences: List<String>)

data class SimilarityResult(
    val id: Int?,
    val question: String?,
    val reponse: String?,
    val similarity: Double?
)



data class SimilarityResponse(
    val total_questions: Int,
    val results: List<SimilarityItem>
)

data class SimilarityItem(
    val question_index: Int,
    val original_question: String?,
    val result: SimilarityResult
)