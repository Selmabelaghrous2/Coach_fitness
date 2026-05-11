package com.example.coachfitness_belag.data.api

import com.example.coachfitness_belag.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.util.Log

object RetrofitInstance {

    // Utilisation de l'IP du PC pour accéder au serveur depuis l'émulateur et le téléphone physique
    private const val BASE_URL = "http://192.168.11.101:8080/"

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val token = TokenManager.getToken()
        val request = if (!token.isNullOrBlank()) {
            val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
            originalRequest.newBuilder()
                .header("Authorization", authHeader)
                .build()
        } else {
            originalRequest
        }
        
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
