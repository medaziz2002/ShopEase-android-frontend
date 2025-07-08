package com.example.project180.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.OrderDto
import com.example.project180.Model.OrderItemDto
import com.example.project180.Util.UserPreferences
import kotlinx.coroutines.launch


class OrderViewModel : ViewModel() {

    private val _orderCreationResult = MutableLiveData<Result<OrderDto>>()
    val orderCreationResult: LiveData<Result<OrderDto>> = _orderCreationResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun createOrder(
        userId: Int,
        items: List<OrderItemDto>,
        totalAmount: Double,
        isDelivery: Boolean,
        deliveryAddress: String?,
        deliveryCost: Double
    ) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val orderDto = OrderDto(
                    userId = userId,
                    status = "EN_Attente_De_Confirmation",
                    paymentMethod = "PAYPAL",
                    deliveryAddress = deliveryAddress ?: "Retrait en magasin",
                    deliveryMethod = if (isDelivery) "DELIVERY" else "PICKUP",
                    totalAmount = totalAmount,
                    deliveryCost = if (isDelivery) deliveryCost else 0.0,
                    items = items
                )

                val response = RetrofitInstance.apiWithAuth.createOrder(orderDto)
                if (response.isSuccessful) {
                    response.body()?.let { order ->
                        _orderCreationResult.value = Result.success(order)
                    } ?: run {
                        _error.value = "Réponse vide du serveur"
                    }
                } else {
                    _error.value = "Erreur serveur: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
                _orderCreationResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }

    private val _orders = MutableLiveData<List<OrderDto>>()
    val orders: LiveData<List<OrderDto>> = _orders

    fun loadUserOrders(userId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getUserOrders(userId)
                if (response.isSuccessful) {
                    _orders.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Erreur serveur: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }



    fun loadVendeurOrders(userId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getVendeurOrders(userId)
                if (response.isSuccessful) {
                    _orders.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Erreur serveur: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }



    private val _orderUpdateResult = MutableLiveData<Result<OrderDto>>()
    val orderUpdateResult: LiveData<Result<OrderDto>> = _orderUpdateResult

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.updateOrderStatus(orderId, newStatus)
                if (response.isSuccessful) {
                    response.body()?.let { updatedOrder ->
                        _orderUpdateResult.value = Result.success(updatedOrder)
                        // Mettre à jour la liste des orders si nécessaire
                        _orders.value = _orders.value?.map { order ->
                            if (order.id == orderId) updatedOrder else order
                        }
                    } ?: run {
                        _error.value = "Réponse vide du serveur"
                    }
                } else {
                    _error.value = "Erreur serveur: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
                _orderUpdateResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }











}