package com.example.project180.Helper

import com.example.project180.Model.AuthUser
import com.example.project180.Model.CartDto
import com.example.project180.Model.CategoryDto
import com.example.project180.Model.NotificationItem
import com.example.project180.Model.OrderDto
import com.example.project180.Model.ProductDto
import com.example.project180.Model.ReviewDto
import com.example.project180.Model.SimilarityRequest
import com.example.project180.Model.SimilarityResponse
import com.example.project180.Model.SizeDto
import com.example.project180.Model.UserRequest
import com.example.project180.Model.WeightDto
import com.example.project180.Model.WishlistItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {



    @POST("api/v1/chat/similarityQuestions")
    fun sendQuestions(@Body request: SimilarityRequest): Call<SimilarityResponse>


    @GET("api/v1/products/top-rated")
    suspend fun getTopRatedProducts(
        @Query("limit") limit: Int = 5
    ): Response<List<ProductDto>>


    @POST("api/v1/reviews")
    suspend fun createReview(
        @Body reviewDto: ReviewDto
    ): Response<Void>

    @PUT("api/v1/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Int,
        @Body updatedReview: ReviewDto
    ): Response<Void>

    @PUT("api/v1/reviews/rating/{reviewId}")
    suspend fun updateReviewRating(
        @Path("reviewId") reviewId: Int,
        @Query("rating") rating: Float
    ): Response<Void>

    @DELETE("api/v1/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Int,
        @Query("userId") userId: Int
    ): Response<Void>

    @GET("api/v1/reviews/product/{productId}/user/{userId}")
    suspend fun getReviewByUserAndProduct(
        @Path("productId") productId: Int,
        @Path("userId") userId: Int
    ): Response<ReviewDto>

    @GET("api/v1/reviews/has-reviewed")
    suspend fun hasUserReviewedProduct(
        @Query("productId") productId: Int,
        @Query("userId") userId: Int
    ): Response<Boolean>

    @GET("api/v1/reviews/product/user/{productId}/{userId}")
    suspend fun getReviewsByProductId(
        @Path("productId") productId: Int,
        @Path("userId") userId: Int
    ): Response<List<ReviewDto>>


    @Multipart
    @PUT("api/v1/products/{id}/with-images")
    suspend fun updateProductWithImages(
        @Path("id") id: Int,
        @Part("productDto") productDto: RequestBody,
        @Part images: List<MultipartBody.Part>,
        @Part("imagesToDelete") imagesToDelete: RequestBody?
    ): Response<Void>

    @POST("api/v1/cart/{cartId}/options")
    suspend fun updateCartItemOptions(
        @Path("cartId") cartId: Int,
        @Query("sizes") sizes: List<String>?,
        @Query("weights") weights: List<String>?
    ): Response<CartDto>

    @Multipart
    @POST("api/v1/users/modifier")
    suspend fun modifier(
        @Part("userDto") user: RequestBody,
        @Part image: MultipartBody.Part? = null // Make the image optional by giving it a default value of null
    ): Response<ResponseBody>


    @GET("api/v1/notifications/user/{userId}")
    suspend fun getUserNotifications(
        @Path("userId") userId: Int
    ): Response<List<NotificationItem>>


    @GET("api/v1/users/getById/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): Response<UserRequest>


    @PUT("api/v1/users/{userId}/fcm-token")
    suspend fun updateFcmToken(
        @Path("userId") userId: Int,
        @Query("token") token: String
    ): Response<Void>

    @GET("api/v1/products/{id}/status")
    suspend fun checkStockStatus(
        @Path("id") productId: Int
    ): Response<Boolean>

    @GET("api/v1/wishlist/exists")
    suspend fun checkProductInWishList(
        @Query("productId") productId: Int,
        @Query("userId") userId: Int
    ): Response<Boolean>

    @POST("api/v1/wishlist/add")
    suspend fun addToWishList(
        @Query("productId") productId: Int,
        @Query("userId") userId: Int
    ): Response<Void>

    @GET("api/v1/wishlist/user/{userId}")
    suspend fun getWishListItems(
        @Path("userId") userId: Int
    ): Response<List<WishlistItem>>

    @DELETE("api/v1/wishlist/{wishListId}")
    suspend fun removeFromWishList(
        @Path("wishListId") wishListId: Int
    ): Response<Void>

    @DELETE("api/v1/wishlist/clear/{userId}")
    suspend fun clearWishList(
        @Path("userId") userId: Int
    ): Response<Void>


    @GET("api/v1/cart/exists")
    suspend fun checkProductInCart(
        @Query("productId") productId: Int,
        @Query("userId") userId: Int
    ): Response<Boolean>

    @POST("api/v1/cart/add")
    suspend fun addToCart(
        @Query("productId") productId: Int,
        @Query("userId") userId: Int,
        @Query("quantity") quantity: Int = 1,
        @Query("sizes") sizes: List<String>? = null,
        @Query("weights") weights: List<String>? = null
    ): Response<Void>

    @GET("api/v1/cart/user/{userId}")
    suspend fun getCartItems(
        @Path("userId") userId: Int
    ): Response<List<CartDto>>

    @PUT("api/v1/cart/{cartId}")
    suspend fun updateQuantity(
        @Path("cartId") cartId: Int,
        @Query("quantity") quantity: Int
    ): Response<Void>

    @DELETE("api/v1/cart/{cartId}")
    suspend fun removeFromCart(
        @Path("cartId") cartId: Int
    ): Response<Void>

    @DELETE("api/v1/cart/clear/{userId}")
    suspend fun clearCart(
        @Path("userId") userId: Int
    ): Response<Void>


    @Multipart
    @POST("api/v1/users/add")
    suspend fun registerUser(
        @Part("userDto") user: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>


    @POST("login")
    suspend fun login(@Body user: AuthUser): Response<Void>

    @GET("api/v1/users/getByEmail/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<UserRequest>

    @GET("api/v1/categories")
    suspend fun getAllCategories(): Response<List<CategoryDto>>

    @Multipart
    @PUT("api/v1/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Part("categoryDto") categoryDto: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<Void>

    @DELETE("api/v1/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): Response<Void>


    @Multipart
    @POST("api/v1/categories")
    suspend fun createCategory(
        @Part("categoryDto") categoryDto: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<Void>

    @GET("api/v1/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Int): Response<CategoryDto>

    
    @Multipart
    @POST("api/v1/products")
    suspend fun createProduct(
        @Part("productDto") productDto: RequestBody,
        @Part pathImages: List<MultipartBody.Part>
    ): Response<Void>



    @GET("api/v1/products")
    suspend fun getAllProducts(): Response<List<ProductDto>>




    @DELETE("api/v1/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int
    ): Response<Void>

    @GET("api/v1/products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int
    ): Response<ProductDto>


    @GET("api/v1/users")
    suspend fun getAllUsers(): Response<List<UserRequest>>


    @GET("api/v1/dashboard/stats")
    suspend fun getDashboardStats(): Response<Map<String, Long>>

    @GET("api/v1/dashboard/statsSaller/{seller_id}")
    suspend fun getDashboardStatsSaller(
        @Path("seller_id") sellerId: Int
    ): Response<Map<String, Long>>


    @GET("/api/v1/products/sizes")
    suspend fun getSize(): Response<SizeDto>

    @GET("/api/v1/products/weights")
    suspend fun getWeight(): Response<WeightDto>


    // Order endpoints
    @POST("api/v1/orders")
    suspend fun createOrder(@Body orderDTO: OrderDto): Response<OrderDto>

    @GET("api/v1/orders/user/{userId}")
    suspend fun getUserOrders(@Path("userId") userId: Int): Response<List<OrderDto>>

    @GET("api/v1/orders/vendeur/{userId}")
    suspend fun getVendeurOrders(@Path("userId") userId: Int): Response<List<OrderDto>>


    @GET("api/v1/orders/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: Int): Response<OrderDto>

    @PATCH("api/v1/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Int,
        @Query("status") status: String
    ): Response<OrderDto>

}
