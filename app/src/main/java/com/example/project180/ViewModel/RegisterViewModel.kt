package com.example.project180.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch
import android.util.Log
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.UserRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RegisterViewModel : ViewModel() {

    fun registerUser(user: UserRequest,imageFile: File) {
        viewModelScope.launch {
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("pathImage", imageFile.name, requestFile)
                val requestBody = Gson().toJson(user).toRequestBody("application/json".toMediaTypeOrNull())
                val response = RetrofitInstance.apiWithoutAuth.registerUser(requestBody,imagePart)
                if (response.isSuccessful) {
                    Log.d("Register", "Utilisateur ajouté avec succès: ${response.body()}")
                } else {

                    Log.e("Register", "Erreur d'ajout: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Register", "Erreur exception: ${e.message}")
            }
        }
    }



}










