package com.example.coachfitness_belag.data.api

import com.example.coachfitness_belag.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.util.Log

object RetrofitInstance {

    private const val BASE_URL = "http://192.168.11.101:8080/"

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // On n'ajoute pas le token pour les routes d'authentification
        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val token = TokenManager.getToken()
        val request = if (!token.isNullOrBlank()) {
            // S'assurer que le préfixe Bearer est correct et unique
            val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
            
            originalRequest.newBuilder()
                .header("Authorization", authHeader)
                .build()
        } else {
            originalRequest
        }
        
        val response = chain.proceed(request)
        
        // Log pour le débogage si 401
        if (response.code == 401) {
            Log.e("AuthDebug", "401 Unauthorized détecté sur: $path. Token: $token")
        }
        
        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // Ajouter le token en premier
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }) // Logguer APRÈS l'ajout du header pour voir s'il est présent
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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
