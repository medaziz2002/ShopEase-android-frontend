// CategoryViewModel.kt
package com.example.project180.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.CategoryDto
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class CategoryViewModel : ViewModel() {
    // List des catégories
    private val _categories = MutableLiveData<List<CategoryDto>>()
    val categories: LiveData<List<CategoryDto>> = _categories

    // Erreurs
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Status d'ajout
    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> = _uploadStatus

    // Status de modification
    private val _updateStatus = MutableLiveData<Boolean>()
    val updateStatus: LiveData<Boolean> = _updateStatus

    // Status de suppression
    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus

    // Catégorie individuelle
    private val _category = MutableLiveData<CategoryDto>()
    val category: LiveData<CategoryDto> = _category

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getAllCategories()
                if (response.isSuccessful) {
                    _categories.postValue(response.body())
                    Log.d("API_RESPONSE", "Categories fetched: ${response.body()}")
                } else {
                    _error.postValue("Échec du chargement : ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }

// Modifiez les méthodes createCategory et updateCategory comme suit :

    fun createCategory(title: String, imageFile: File) {
        viewModelScope.launch {
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("pathImage", imageFile.name, requestFile)

                val categoryDto = CategoryDto(titre = title)
                val requestBody = Gson().toJson(categoryDto).toRequestBody("application/json".toMediaTypeOrNull())

                val response = RetrofitInstance.apiWithAuth.createCategory(requestBody, imagePart)

                if (response.isSuccessful) {
                    _uploadStatus.postValue(true)
                    fetchCategories() // Rafraîchir la liste après création
                } else {
                    _uploadStatus.postValue(false)
                    _error.postValue("Échec de l'upload: ${response.message()}")
                }
            } catch (e: Exception) {
                _uploadStatus.postValue(false)
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }

    fun updateCategory(id: Int, title: String, imageFile: File) {
        viewModelScope.launch {
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("pathImage", imageFile.name, requestFile)

                val categoryDto = CategoryDto(id = id, titre = title)
                val requestBody = Gson().toJson(categoryDto).toRequestBody("application/json".toMediaTypeOrNull())

                val response = RetrofitInstance.apiWithAuth.updateCategory(id, requestBody, imagePart)

                if (response.isSuccessful) {
                    _updateStatus.postValue(true)
                    fetchCategories() // Rafraîchir la liste après modification
                } else {
                    _updateStatus.postValue(false)
                    _error.postValue("Échec de la modification: ${response.message()}")
                }
            } catch (e: Exception) {
                _updateStatus.postValue(false)
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.deleteCategory(id)
                _deleteStatus.postValue(response.isSuccessful)

                if (response.isSuccessful) {
                    fetchCategories()
                } else {
                    _error.postValue("Échec de la suppression: ${response.message()}")
                }
            } catch (e: Exception) {
                _deleteStatus.postValue(false)
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }

    fun getCategoryById(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getCategoryById(id)
                if (response.isSuccessful) {
                    _category.postValue(response.body())
                } else {
                    _error.postValue("Échec du chargement: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }
}
