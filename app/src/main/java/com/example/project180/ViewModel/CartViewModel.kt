package com.example.project180.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.CartDto
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    // Liste des articles du panier
    private val _cartItems = MutableLiveData<List<CartDto>>()
    val cartItems: LiveData<List<CartDto>> = _cartItems

    // États et erreurs
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Totaux
    private val _subTotal = MutableLiveData<Double>()
    val subTotal: LiveData<Double> = _subTotal

    private val _total = MutableLiveData<Double>()
    val total: LiveData<Double> = _total

    // Récupérer le panier
    fun fetchCartItems(userId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.apiWithAuth.getCartItems(userId)
                if (response.isSuccessful) {
                    _cartItems.value = response.body()
                    calculateTotals()
                    Log.d("CartViewModel", "Cart items loaded: ${response.body()}")
                } else {
                    _error.value = "Erreur: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }


    fun addToCart(
        productId: Int,
        userId: Int,
        quantity: Int,
        sizes: List<String>? = null,
        weights: List<String>? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.apiWithAuth.addToCart(
                    productId = productId,
                    userId = userId,
                    quantity = quantity,
                    sizes = sizes,
                    weights = weights
                )

                if (response.isSuccessful) {
                    fetchCartItems(userId)
                } else {
                    _error.value = "Erreur d'ajout : ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau : ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }



    // Mettre à jour la quantité
    fun updateQuantity(cartId: Int, quantity: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.apiWithAuth.updateQuantity(cartId, quantity)
                if (response.isSuccessful) {
                    // Mise à jour locale optimisée
                    _cartItems.value?.let { currentItems ->
                        val updatedItems = currentItems.map { item ->
                            if (item.id == cartId) item.copy(quantity = quantity) else item
                        }
                        _cartItems.value = updatedItems
                        calculateTotals()
                    }
                } else {
                    _error.value = "Erreur de mise à jour: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Supprimer un article
    fun removeFromCart(cartId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.apiWithAuth.removeFromCart(cartId)
                if (response.isSuccessful) {
                    _cartItems.value = _cartItems.value?.filter { it.id != cartId }
                    calculateTotals()
                } else {
                    _error.value = "Erreur de suppression: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Vider le panier
    fun clearCart(userId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.apiWithAuth.clearCart(userId)
                if (response.isSuccessful) {
                    _cartItems.value = emptyList()
                    _subTotal.value = 0.0
                    _total.value = 0.0
                } else {
                    _error.value = "Erreur de vidage: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun calculateTotals() {
        _cartItems.value?.let { items ->
            _subTotal.value = String.format("%.2f", items.sumOf { cartItem ->
                cartItem.productDto?.let { product ->
                    (if (product.discountPercentage > 0) {
                        product.price * (1 - product.discountPercentage / 100)
                    } else {
                        product.price
                    }).toDouble()* cartItem.quantity
                } ?: 0.0
            }).toDouble()
        }
    }

    private val _productInCart = MutableLiveData<Boolean>()
    val productInCart: LiveData<Boolean> = _productInCart


    fun checkProductInCart(productId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.checkProductInCart(productId, userId)
                _productInCart.value = response.body() // suppose que c’est un booléen
            } catch (e: Exception) {
                Log.e("CartViewModel", "Erreur lors de la vérification du panier", e)
                _productInCart.value = null
            }
        }
    }


    fun updateCartItemOptions(cartId: Int, sizes: List<String>? = null, weights: List<String>? = null) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.updateCartItemOptions(
                    cartId = cartId,
                    sizes = sizes,
                    weights = weights
                )

                if (response.isSuccessful) {
                    _cartItems.value?.let { currentItems ->
                        val updatedItems = currentItems.map { item ->
                            if (item.id == cartId) {
                                item.copy(
                                    size = sizes ?: item.size,
                                    weight = weights ?: item.weight
                                )
                            } else {
                                item
                            }
                        }
                        _cartItems.postValue(updatedItems)
                    }
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error updating cart item options", e)
            }
        }
    }

}