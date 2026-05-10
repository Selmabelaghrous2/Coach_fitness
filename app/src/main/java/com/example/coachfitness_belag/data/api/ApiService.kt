package com.example.coachfitness_belag.data.api

import com.example.coachfitness_belag.data.models.*
import retrofit2.http.*

interface ApiService {

    // ========== AUTHENTIFICATION ==========
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): LogoutResponse

    // ========== EXERCICES ==========
    @GET("api/exercises")
    suspend fun getAllExercises(): List<Exercise>

    @GET("api/exercises/{id}")
    suspend fun getExerciseById(@Path("id") id: Int): Exercise

    @GET("api/exercises/category/{category}")
    suspend fun getExercisesByCategory(@Path("category") category: String): List<Exercise>

    // ========== UTILISATEUR ==========
    @GET("api/users/profile")
    suspend fun getUserProfile(): User

    @PUT("api/users/profile")
    suspend fun updateUserProfile(@Body user: User): User
}
