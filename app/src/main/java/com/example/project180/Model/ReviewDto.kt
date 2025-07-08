package com.example.project180.Model

import java.time.LocalDateTime

data class ReviewDto(
    var id: Int? = null,
    var rating: Float? = null,
    var comment: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var productId: Int? = null,
    var userId: Int? = null,
    var userNom: String? = null,
    var userPrenom: String? = null,
    var productTitle: String? = null,
    var productRating: Double? = null
)
