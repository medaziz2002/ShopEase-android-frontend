package com.example.project180.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.ReviewDto
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {

    private val _reviewCreationResult = MutableLiveData<Result<Unit>>()
    val reviewCreationResult: LiveData<Result<Unit>> = _reviewCreationResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _userHasReviewed = MutableLiveData<Boolean>()
    val userHasReviewed: LiveData<Boolean> get() = _userHasReviewed

    private val _userReview = MutableLiveData<ReviewDto?>()
    val userReview: LiveData<ReviewDto?> get() = _userReview

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun createReview(reviewDto: ReviewDto) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.createReview(reviewDto)
                if (response.isSuccessful) {
                    _reviewCreationResult.postValue(Result.success(Unit))
                    _error.postValue(null)
                } else {
                    _error.postValue("Erreur serveur: ${response.code()}")
                    _reviewCreationResult.postValue(Result.failure(Exception("Erreur serveur: ${response.code()}")))
                }
            } catch (e: Exception) {
                _error.postValue("Erreur réseau: ${e.message}")
                _reviewCreationResult.postValue(Result.failure(e))
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun updateReview(reviewId: Int, updatedReviewDto: ReviewDto) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.updateReview(reviewId, updatedReviewDto)
                if (response.isSuccessful) {
                    _reviewCreationResult.postValue(Result.success(Unit))
                    _error.postValue(null)
                } else if (response.code() == 403) {
                    _error.postValue("Non autorisé à modifier cet avis.")
                    _reviewCreationResult.postValue(Result.failure(Exception("403 Forbidden")))
                } else if (response.code() == 404) {
                    _error.postValue("Avis non trouvé.")
                    _reviewCreationResult.postValue(Result.failure(Exception("404 Not Found")))
                } else {
                    _error.postValue("Erreur serveur: ${response.code()}")
                    _reviewCreationResult.postValue(Result.failure(Exception("Erreur serveur")))
                }
            } catch (e: Exception) {
                _error.postValue("Erreur réseau: ${e.message}")
                _reviewCreationResult.postValue(Result.failure(e))
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun hasUserReviewedProduct( productId: Int,userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.hasUserReviewedProduct(productId, userId)

                _userHasReviewed.postValue(response.isSuccessful && response.body() == true)
            } catch (e: Exception) {
                _userHasReviewed.postValue(false)
            }
        }
    }

    fun getUserReviewForProduct(userId: Int, productId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getReviewByUserAndProduct(productId, userId)
                if (response.isSuccessful) {
                    _userReview.postValue(response.body())
                    _userHasReviewed.postValue(true)
                } else {
                    _userReview.postValue(null)
                    _userHasReviewed.postValue(false)
                }
            } catch (e: Exception) {
                _userReview.postValue(null)
                _userHasReviewed.postValue(false)
            }
        }
    }

    fun updateReviewRating(reviewId: Int?, rating: Float) {
        if (reviewId == null) {
            _error.postValue("ID de l'avis invalide")
            _reviewCreationResult.postValue(Result.failure(Exception("ID de l'avis invalide")))
            return
        }
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.updateReviewRating(reviewId, rating)
                if (response.isSuccessful) {
                    _reviewCreationResult.postValue(Result.success(Unit))
                    _error.postValue(null)
                } else if (response.code() == 403) {
                    _error.postValue("Non autorisé à modifier cet avis.")
                    _reviewCreationResult.postValue(Result.failure(Exception("403 Forbidden")))
                } else if (response.code() == 404) {
                    _error.postValue("Avis non trouvé.")
                    _reviewCreationResult.postValue(Result.failure(Exception("404 Not Found")))
                } else {
                    _error.postValue("Erreur serveur: ${response.code()}")
                    _reviewCreationResult.postValue(Result.failure(Exception("Erreur serveur")))
                }
            } catch (e: Exception) {
                _error.postValue("Erreur réseau: ${e.message}")
                _reviewCreationResult.postValue(Result.failure(e))
            } finally {
                _loading.postValue(false)
            }
        }
    }



    private val _reviews = MutableLiveData<List<ReviewDto>>()
    val reviews: LiveData<List<ReviewDto>> get() = _reviews

    fun fetchReviewsByProductId(productId: Int, userInfo :Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getReviewsByProductId(productId,userInfo)
                Log.d("API_RESPONSE", "Response code: ${response.code()}")
                Log.d("API_RESPONSE", "Response body: ${response.body()}")

                if (response.isSuccessful) {
                    _reviews.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Erreur serveur: ${response.code()}")
                    _reviews.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Network error", e)
                _error.postValue("Erreur réseau: ${e.message}")
                _reviews.postValue(emptyList())
            } finally {
                _loading.postValue(false)
            }
        }
    }




}
