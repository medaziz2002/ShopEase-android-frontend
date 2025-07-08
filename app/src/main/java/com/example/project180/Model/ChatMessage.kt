package com.example.project180.Model

data class ChatMessage(
    val text: String,
    val isUser: Boolean // vrai si message envoyé par utilisateur
)
