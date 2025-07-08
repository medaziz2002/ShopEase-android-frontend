package com.example.project180.ViewModel

import com.example.project180.Model.UserRequest

sealed class LoginResult {
    data class Success(val user: UserRequest): LoginResult()
    data class Error(val message: String): LoginResult()
}
