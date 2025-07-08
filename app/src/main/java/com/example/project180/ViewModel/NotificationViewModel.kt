package com.example.project180.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.NotificationItem
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Pass the userId parameter to fetch notifications
    fun fetchNotifications(userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getUserNotifications(userId)  // Pass userId
                if (response.isSuccessful) {
                    _notifications.postValue(response.body())
                    Log.d("API_RESPONSE", "Notifications fetched: ${response.body()}")
                } else {
                    _error.postValue("Échec du chargement: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }
}
