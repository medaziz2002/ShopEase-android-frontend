package com.example.project180.Util

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings.Global.putString
import com.example.project180.Model.UserRequest
import com.example.project180.Model.UserSharedPreferences

object UserPreferences {

    private const val PREF_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_FIRST_NAME = "user_first_name"
    private const val KEY_USER_ROLE = "role"
    private const val KEY_USER_LAST_NAME = "user_last_name"
    private const val KEY_FCM_TOKEN = "fcm_token"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save user information
    fun saveUserInfo(context: Context, userId: Int, firstName: String, lastName: String, role: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_FIRST_NAME, firstName)
        editor.putString(KEY_USER_LAST_NAME, lastName)
        editor.putString(KEY_USER_ROLE,role)
        editor.apply()
    }



    // Retrieve user information
    fun getUserInfo(context: Context): UserSharedPreferences? {
        val sharedPrefs = getSharedPreferences(context)
        val userId = sharedPrefs.getInt(KEY_USER_ID, 0)
        val firstName = sharedPrefs.getString(KEY_USER_FIRST_NAME, null)
        val lastName = sharedPrefs.getString(KEY_USER_LAST_NAME, null)
        val role = sharedPrefs.getString(KEY_USER_ROLE, null)

        return if (userId != null && firstName != null && lastName != null && role!=null) {
            UserSharedPreferences(userId, firstName, lastName,role)
        } else {
            null
        }
    }

    // Clear user information
    fun clearUserInfo(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USER_FIRST_NAME)
        editor.remove(KEY_USER_LAST_NAME)
        editor.remove(KEY_FCM_TOKEN)
        editor.apply()
    }

    fun saveFcmToken(context: Context, token: String) {
        getSharedPreferences(context).edit().apply {
            putString(KEY_FCM_TOKEN, token)
            apply()
        }
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return getUserInfo(context) != null
    }

    // Get FCM token
    fun getFcmToken(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_FCM_TOKEN, null)
    }

    // Check if FCM token exists
    fun hasFcmToken(context: Context): Boolean {
        return getSharedPreferences(context).contains(KEY_FCM_TOKEN)
    }
}

