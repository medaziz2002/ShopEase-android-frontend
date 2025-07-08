package com.example.project180.Model

data class ResetPasswordRequest(
    val phoneNumber: String,
    val token: String,
    val newPassword: String
)