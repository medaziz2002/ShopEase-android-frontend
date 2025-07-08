package com.example.project180.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.WishlistItem
import kotlinx.coroutines.launch

class WishListViewModel : ViewModel() {
    // Liste des éléments de la wishlist
    private val _wishlistItems = MutableLiveData<List<WishlistItem>>()
    val wishlistItems: LiveData<List<WishlistItem>> = _wishlistItems

    // Statut de vérification
    private val _isInWishlist = MutableLiveData<Boolean>()
    val isInWishlist: LiveData<Boolean> = _isInWishlist

    // Erreurs
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Statut d'ajout
    private val _addStatus = MutableLiveData<Boolean>()
    val addStatus: LiveData<Boolean> = _addStatus


    private val _removeStatus = MutableLiveData<Boolean>()
    val removeStatus: LiveData<Boolean> = _removeStatus


    private val _clearStatus = MutableLiveData<Boolean>()
    val clearStatus: LiveData<Boolean> = _clearStatus


    fun checkProductInWishlist(productId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.checkProductInWishList(productId, userId)
                if (response.isSuccessful) {
                    _isInWishlist.postValue(response.body() ?: false)
                } else {
                    _error.postValue("Échec de la vérification: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }

    fun addToWishlist(productId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.addToWishList(productId, userId)
                _addStatus.postValue(response.isSuccessful)

                if (response.isSuccessful) {
                    // Rafraîchir la liste après ajout
                    getWishlistItems(userId)
                } else {
                    _error.postValue("Échec de l'ajout: ${response.message()}")
                }
            } catch (e: Exception) {
                _addStatus.postValue(false)
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }

    fun getWishlistItems(userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getWishListItems(userId)
                if (response.isSuccessful) {
                    _wishlistItems.postValue(response.body())
                    Log.d("WISHLIST", "Items fetched: ${response.body()}")
                } else {
                    _error.postValue("Échec du chargement: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }


    fun removeFromWishlist(wishlistId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.removeFromWishList(wishlistId)
                _removeStatus.postValue(response.isSuccessful)

                if (response.isSuccessful) {
                    // Rafraîchir la liste après suppression
                    getWishlistItems(userId)
                } else {
                    _error.postValue("Échec de la suppression: ${response.message()}")
                }
            } catch (e: Exception) {
                _removeStatus.postValue(false)
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }


    fun clearWishlist(userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.clearWishList(userId)
                _clearStatus.postValue(response.isSuccessful)

                if (response.isSuccessful) {
                    _wishlistItems.postValue(emptyList())
                } else {
                    _error.postValue("Échec du vidage: ${response.message()}")
                }
            } catch (e: Exception) {
                _clearStatus.postValue(false)
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }
}