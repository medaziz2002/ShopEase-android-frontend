package com.example.project180.Model


data class UserRequest(
    val id:Int,
    val telephone:String,
    val nom: String,
    val prenom: String,
    val email: String,
    val password: String,
    val role: String,
    val imageDto: ImageDto? = null
)
