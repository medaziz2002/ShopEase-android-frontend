package com.example.project180.Model

data class WishlistItem(
    val id: Int,
    val productId: Int,
    val userId: Int,
    val product: ProductDto? = null,
    val userRequest: UserRequest? = null
)

