package com.example.project180.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.AuthUser
import com.example.project180.Util.TokenManager
import com.example.project180.Util.UserPreferences
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import retrofit2.Response

class LoginViewModel : ViewModel() {

    val loginResult = MutableLiveData<LoginResult>()


    /*
    fun login(user: AuthUser, context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithoutAuth.login(user)
                Log.d("response", RetrofitInstance.apiWithoutAuth.login(user).toString())
                val token = response.headers()["Authorization"]
                if (token != null) {
                    TokenManager.saveToken(context, token)
                    val email = decodeJWT(token)
                    val userResponse = RetrofitInstance.apiWithoutAuth.getUserByEmail(email)
                    if (userResponse.isSuccessful) {
                        Log.d("sucees","je suis dans sucees")
                        userResponse.body()?.let { user ->
                            System.out.println("le user apres login est "+user.role)
                            UserPreferences.saveUserInfo(context, user.id, user.nom, user.prenom,user.role)
                            registerFCMToken(user.id, context)

                            loginResult.postValue(LoginResult.Success(user))
                        } ?: loginResult.postValue(LoginResult.Error("Utilisateur introuvable"))
                    } else {
                        Log.d("LoginViewModel","je suis dans LoginViewModel")
                        loginResult.postValue(LoginResult.Error("Erreur lors de la récupération des données utilisateur"))
                    }
                }
            } catch (e: Exception) {
                System.out.println("je suis dans le catch")
                loginResult.postValue(LoginResult.Error("Email ou mot de passe incorrect"))
            }
        }
    }
     */

    fun login(user: AuthUser, context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithoutAuth.login(user)
                Log.d("LoginResponse", "Code: ${response.code()}, Headers: ${response.headers()}")

                // Vérifiez d'abord si la réponse est réussie
                if (!response.isSuccessful) {
                    val errorMessage = when (response.code()) {
                        403 -> "Accès refusé: Email ou mot de passe incorrect"
                        401 -> "Authentification requise"
                        else -> "Erreur de connexion (code ${response.code()})"
                    }
                    Log.e("LoginError", errorMessage)
                    loginResult.postValue(LoginResult.Error(errorMessage))
                    return@launch
                }

                val token = response.headers()["Authorization"]
                if (token == null) {
                    val noTokenError = "Token d'authentification manquant dans la réponse"
                    Log.e("LoginError", noTokenError)
                    loginResult.postValue(LoginResult.Error(noTokenError))
                    return@launch
                }

                TokenManager.saveToken(context, token)
                val email = decodeJWT(token)

                val userResponse = RetrofitInstance.apiWithoutAuth.getUserByEmail(email)
                if (!userResponse.isSuccessful) {
                    val userError = "Erreur lors de la récupération des données utilisateur: ${userResponse.code()}"
                    Log.e("LoginError", userError)
                    loginResult.postValue(LoginResult.Error(userError))
                    return@launch
                }

                userResponse.body()?.let { user ->
                    Log.d("LoginSuccess", "User role: ${user.role}")
                    UserPreferences.saveUserInfo(context, user.id, user.nom, user.prenom, user.role)
                    registerFCMToken(user.id, context)
                    loginResult.postValue(LoginResult.Success(user))
                } ?: run {
                    Log.e("LoginError", "Corps de réponse utilisateur vide")
                    loginResult.postValue(LoginResult.Error("Utilisateur introuvable"))
                }

            } catch (e: Exception) {
                Log.e("LoginException", "Erreur lors de la connexion", e)
                loginResult.postValue(LoginResult.Error("Erreur réseau: ${e.localizedMessage}"))
            }
        }
    }

    private fun decodeJWT(token: String): String {
        val parts = token.split(".")
        return try {
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val json = JSONObject(payload)
            json.getString("sub")
        } catch (e: Exception) {
            ""
        }
    }


    private fun registerFCMToken(userId: Int, context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM", "Token reçu: $fcmToken")

                // Sauvegarde locale
                UserPreferences.saveFcmToken(context, fcmToken)
                Log.d("FCM", "Token sauvegardé localement")

                viewModelScope.launch {
                    try {
                        RetrofitInstance.apiWithAuth.updateFcmToken(userId, fcmToken)
                        Log.d("FCM", "Token envoyé avec succès au serveur")
                    } catch (e: Exception) {
                        Log.e("FCM", "Échec d'envoi du token au serveur", e)
                    }
                }
            } else {
                task.exception?.let { exception ->
                    Log.e("FCM", "Échec de récupération du token", exception)
                } ?: Log.e("FCM", "Erreur inconnue lors de la récupération du token")
            }
        }
    }
}