package com.example.project180.Model

import java.util.Date

data class OrderDto(
    val id: Int? = null,
    val userId: Int,
    val orderDate: Date? = null,
    val deliveryDate: Date? = null,
    val status: String,
    val paymentMethod: String,
    val deliveryAddress: String,
    val deliveryMethod: String,
    val totalAmount: Double,
    val deliveryCost: Double,
    val items: List<OrderItemDto> = emptyList()
)
