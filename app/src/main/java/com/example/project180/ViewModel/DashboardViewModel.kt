package com.example.project180.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.DashboardStats
import com.example.project180.Model.DashboardStatsSaller
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _stats = MutableLiveData<DashboardStats>()
    val stats: LiveData<DashboardStats> = _stats

    private val _statsSeller = MutableLiveData<DashboardStatsSaller>()
    val statsSeller: LiveData<DashboardStatsSaller> = _statsSeller

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchDashboardStats() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "Fetching admin dashboard stats...")
                val response = RetrofitInstance.apiWithAuth.getDashboardStats()

                if (response.isSuccessful) {
                    Log.d("DashboardViewModel", "API call successful")
                    val statsMap = response.body()

                    statsMap?.let {
                        Log.d("DashboardViewModel", "Response body: $it")
                        val dashboardStats = DashboardStats(
                            totalUsers = it["totalUsers"] ?: 0,
                            totalClients = it["totalClients"] ?: 0,
                            totalSellers = it["totalSellers"] ?: 0,
                            totalProducts = it["totalProducts"] ?: 0,
                            totalCategories = it["totalCategories"] ?: 0
                        )
                        _stats.postValue(dashboardStats)
                    }
                } else {
                    Log.e("DashboardViewModel", "API call failed: ${response.message()}")
                    _error.postValue("Erreur: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching stats: ${e.message}")
                _error.postValue("Erreur: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchDashboardStatsVendeur(sellerId: Int) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "Fetching seller dashboard stats...")
                val response = RetrofitInstance.apiWithAuth.getDashboardStatsSaller(sellerId)

                if (response.isSuccessful) {
                    Log.d("DashboardViewModel", "API call successful")
                    val statsMap = response.body()

                    statsMap?.let {
                        Log.d("DashboardViewModel", "Response body: $it")
                        val dashboardStatsSeller = DashboardStatsSaller(
                            totalProducts = it["totalProducts"] ?: 0,
                            totalCategories = it["totalCategories"] ?: 0
                        )
                        _statsSeller.postValue(dashboardStatsSeller)
                    }
                } else {
                    Log.e("DashboardViewModel", "API call failed: ${response.message()}")
                    _error.postValue("Erreur: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching seller stats: ${e.message}")
                _error.postValue("Erreur: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}