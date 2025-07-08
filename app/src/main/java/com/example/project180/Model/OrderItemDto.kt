package com.example.project180.Model


data class OrderItemDto(
    val id: Int? = null,
    val productId: Int,
    val productDto: ProductDto? = null,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double,
    val size: List<String>? = null,  // Pour les vêtements
    val weight: List<String>? = null, // Pour la nourriture

)
