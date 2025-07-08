package com.example.project180.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.Model.ProductDto
import com.example.project180.Model.SizeDto
import com.example.project180.Model.WeightDto
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProductViewModel : ViewModel() {

    private val _products = MutableLiveData<List<ProductDto>>()
    val products: LiveData<List<ProductDto>> = _products


    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error


    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> = _uploadStatus


    private val _updateStatus = MutableLiveData<Boolean>()
    val updateStatus: LiveData<Boolean> = _updateStatus


    private val _deleteStatus = MutableLiveData<Boolean>()
    val deleteStatus: LiveData<Boolean> = _deleteStatus


    private val _product = MutableLiveData<ProductDto>()
    val product: LiveData<ProductDto> = _product

    val _topRatedProducts = MutableLiveData<List<ProductDto>>()
    val topRatedProducts: LiveData<List<ProductDto>> = _topRatedProducts

    private val _topRatedLoading = MutableLiveData<Boolean>()
    val topRatedLoading: LiveData<Boolean> = _topRatedLoading

    private val _topRatedError = MutableLiveData<String?>()
    val topRatedError: MutableLiveData<String?> = _topRatedError

    fun fetchTopRatedProducts(limit: Int = 5) {
        viewModelScope.launch {
            _topRatedLoading.postValue(true)
            _topRatedError.postValue(null)
            try {
                val response = RetrofitInstance.apiWithAuth.getTopRatedProducts(limit)
                if (response.isSuccessful) {
                    _topRatedProducts.postValue(response.body())
                    Log.d("TopRated", "Top rated products fetched: ${response.body()?.size ?: 0} items")
                } else {
                    _topRatedError.postValue("Failed to load top rated products: ${response.message()}")
                    Log.e("TopRated", "API error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _topRatedError.postValue("Network error: ${e.message}")
                Log.e("TopRated", "Exception fetching top rated products", e)
            } finally {
                _topRatedLoading.postValue(false)
            }
        }
    }


    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getAllProducts()
                if (response.isSuccessful) {
                    _products.postValue(response.body())
                    Log.d("API_RESPONSE", "Products fetched: ${response.body()}")
                } else {
                    _error.postValue("Échec du chargement : ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }



    suspend fun checkProductStockStatus(productId: Int): Boolean {
        return try {
            val response = RetrofitInstance.apiWithAuth.checkStockStatus(productId)
            if (response.isSuccessful) {
                val isOutOfStock = response.body() ?: false
                Log.d("StockStatus", "Product $productId stock status: $isOutOfStock")
                isOutOfStock
            } else {
                Log.e("StockStatus", "Failed to check stock status: ${response.message()}")
                false // ou true selon la logique métier
            }
        } catch (e: Exception) {
            Log.e("StockError", "Error checking stock status", e)
            false // ou true si vous voulez bloquer le paiement en cas d'erreur
        }
    }













    fun createProduct(productDto: ProductDto, imageFiles: List<File>) {
        viewModelScope.launch {
            _uploadStatus.postValue(true)

            try {
                val productJson = Gson().toJson(productDto)
                val productRequestBody = productJson.toRequestBody("application/json".toMediaTypeOrNull())

                val imageParts = imageFiles.mapIndexed { index, file ->
                    Log.d("ImageDebug", "Image[$index] - Nom: ${file.name} - Taille: ${file.length()} octets")
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("pathImages", file.name, requestFile)
                }

                Log.d("ProductDebug", "DTO JSON: $productJson")
                Log.d("ProductDebug", "Nombre d'images: ${imageParts.size}")
                Log.d("ProductDebug", "Type première image: ${imageParts.firstOrNull()?.body?.contentType()}")

                val response = RetrofitInstance.apiWithAuth.createProduct(productRequestBody, imageParts)

                _uploadStatus.postValue(response.isSuccessful)
                if (!response.isSuccessful) {
                    _error.postValue("Échec de la création: ${response.message()}")
                    Log.e("ProductError", "Erreur API: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _uploadStatus.postValue(false)
                _error.postValue("Erreur: ${e.message}")
                Log.e("ProductError", "Exception: ", e)
            }
        }
    }



    fun updateProduct(
        id: Int,
        productDto: ProductDto,
        imageFiles: List<File>,
        imagesToDelete: List<Int>?
    ) {
        viewModelScope.launch {
            try {
                Log.d("ProductUpdate", "Début de la mise à jour du produit ID: $id")

                // Créer les parties images
                val imageParts = imageFiles.map { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("pathImages", file.name, requestFile)
                }

                // Créer le RequestBody pour l'objet ProductDto
                val productJson = Gson().toJson(productDto)
                val requestBody = productJson.toRequestBody("application/json".toMediaTypeOrNull())

                // Créer le RequestBody pour les images à supprimer
                val imagesToDeleteBody = imagesToDelete?.let {
                    Gson().toJson(it).toRequestBody("application/json".toMediaTypeOrNull())
                }

                Log.d("ProductUpdate", "Envoi de la requête au serveur...")
                val response = RetrofitInstance.apiWithAuth.updateProductWithImages(
                    id,
                    requestBody,
                    imageParts,
                    imagesToDeleteBody
                )

                if (response.isSuccessful) {
                    Log.d("ProductUpdate", "Mise à jour réussie!")
                    _updateStatus.postValue(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProductUpdate", "Échec de la modification. Code: ${response.code()}")
                    _error.postValue("Échec de la modification: ${response.message()}")
                    _updateStatus.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("ProductUpdate", "Exception lors de la mise à jour", e)
                _updateStatus.postValue(false)
                _error.postValue("Erreur: ${e.message ?: "Erreur inconnue"}")
            }
        }
    }


    // Supprimer un produit
    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.deleteProduct(id)
                if (response.isSuccessful) {
                    _deleteStatus.postValue(true)
                    fetchProducts() // Rafraîchir la liste
                } else {
                    _error.postValue("Échec de la suppression: ${response.errorBody()?.string()}")
                    _deleteStatus.postValue(false)
                }
            } catch (e: Exception) {
                _error.postValue("Erreur réseau: ${e.message}")
                _deleteStatus.postValue(false)
            }
        }
    }

    // Obtenir un produit par son ID
    fun getProductById(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getProductById(id)
                if (response.isSuccessful) {
                    _product.postValue(response.body())
                } else {
                    _error.postValue("Échec du chargement: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }




    private val _sizes = MutableLiveData<SizeDto?>()
    val sizes: LiveData<SizeDto?> = _sizes

    private val _weights = MutableLiveData<WeightDto?>()
    val weights: LiveData<WeightDto?> = _weights

    // Modifiez les méthodes fetch
    fun fetchWeights() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getWeight()
                if (response.isSuccessful) {
                    _weights.postValue(response.body())
                } else {
                    _error.postValue("Échec du chargement des poids")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }

    fun fetchSizes() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiWithAuth.getSize()
                if (response.isSuccessful) {
                    _sizes.postValue(response.body())
                } else {
                    _error.postValue("Échec du chargement des tailles")
                }
            } catch (e: Exception) {
                _error.postValue("Erreur: ${e.message}")
            }
        }
    }


}