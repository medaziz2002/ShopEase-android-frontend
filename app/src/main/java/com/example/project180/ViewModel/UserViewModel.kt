package com.example.project180.ViewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.ApiService
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.UserRequest
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserViewModel() : ViewModel() {

    private val _users = MutableLiveData<List<UserRequest>>()
    val users: LiveData<List<UserRequest>> get() = _users

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _selectedUser = MutableLiveData<UserRequest>()
    val selectedUser: LiveData<UserRequest> get() = _selectedUser

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getAllUsers()
                if (response.isSuccessful && response.body() != null) {
                    _users.value = response.body()
                } else {
                    _error.value = "Erreur: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur : ${e.localizedMessage}"
            }
        }
    }

    fun getUserById(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getUserById(id)
                if (response.isSuccessful && response.body() != null) {
                    _selectedUser.value = response.body()
                } else {
                    _error.value = "Erreur: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur : ${e.localizedMessage}"
            }
        }
    }


    private val _modificationResult = MutableLiveData<Boolean>()
    val modificationResult: LiveData<Boolean> get() = _modificationResult

    fun modifierUser(user: UserRequest, imageFile: File?) {
        viewModelScope.launch {
            try {
                val userJson = Gson().toJson(user) // Using Gson to serialize the UserRequest object to JSON
                val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), userJson)
                val imagePart: MultipartBody.Part? = if (imageFile != null) {
                    val requestBodyImage = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                    MultipartBody.Part.createFormData("image", imageFile.name, requestBodyImage)
                } else {
                    null
                }

                // Call the API
                val response = RetrofitInstance.apiWithAuth.modifier(requestBody, imagePart)

                // Post the result based on whether the request was successful
                _modificationResult.postValue(response.isSuccessful)
            } catch (e: Exception) {
                // Handle exceptions (e.g., network error, parsing issues)
                _modificationResult.postValue(false)
            }
        }
    }





}
