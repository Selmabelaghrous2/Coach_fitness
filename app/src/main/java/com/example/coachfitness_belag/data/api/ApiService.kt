package com.example.coachfitness_belag.data.api

import com.example.coachfitness_belag.data.models.Exercise
import com.example.coachfitness_belag.data.models.User
import retrofit2.http.*

interface ApiService {

    // ========== AUTHENTIFICATION ==========
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): LoginResponse

    // ========== EXERCICES ==========
    @GET("exercises")
    suspend fun getAllExercises(): List<Exercise>

    @GET("exercises/{id}")
    suspend fun getExerciseById(
        @Path("id") id: Int
    ): Exercise

    @GET("exercises/category/{category}")
    suspend fun getExercisesByCategory(
        @Path("category") category: String
    ): List<Exercise>

    // ========== UTILISATEURS ==========
    @GET("users/profile")
    suspend fun getUserProfile(): User

    @PUT("users/profile")
    suspend fun updateUserProfile(
        @Body user: User
    ): User
}

// Request and Response DTOs
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: Int,
    val message: String? = null
)
