package com.example.project180.Helper

import android.content.Context
import com.example.project180.Util.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val TOKEN_KEY = "token"
    private lateinit var appContext: Context
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val authInterceptor = Interceptor { chain ->
        val token = TokenManager.getToken(context = appContext)
        val requestBuilder: Request.Builder = chain.request().newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()
        println("Request URL: ${request.url}")
        println("Authorization Header: ${request.header("Authorization")}")
        chain.proceed(request)
    }

    private val okHttpClientWithAuth = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // OkHttpClient sans AuthInterceptor pour les requêtes sans token (login, register)
    private val okHttpClientWithoutAuth = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiWithAuth: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8087/")
            .client(okHttpClientWithAuth)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val apiWithoutAuth: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8087/")
            .client(okHttpClientWithoutAuth)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
