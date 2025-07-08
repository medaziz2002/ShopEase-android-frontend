package com.example.project180.Util

import android.content.Context

object TokenManager {
    private const val TOKEN_KEY = "jwt_token"
    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().putString(TOKEN_KEY, token).apply()
        println("Token saved: $token")  // Ajouter un log ici pour vérifier que le token est bien sauvegardé
    }


    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().remove(TOKEN_KEY).apply()
    }
}
