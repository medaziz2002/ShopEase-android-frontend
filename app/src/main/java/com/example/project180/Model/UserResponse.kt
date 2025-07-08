package com.example.project180.Model

data class UserResponse(
    val success: Boolean,
    val message: String,
    val data: Any? // Or define a specific data type
)
