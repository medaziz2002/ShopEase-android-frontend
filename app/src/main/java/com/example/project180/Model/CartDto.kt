package com.example.project180.Model




data class CartDto(
    val id: Int? = null,
    val productId: Int,
    val userId: Int,
    val productDto: ProductDto? = null,
    val userRequest: UserRequest? = null,
    val quantity: Int,
    val size: List<String>? = null,  // Pour les vêtements
    val weight: List<String>? = null, // Pour la nourriture
)