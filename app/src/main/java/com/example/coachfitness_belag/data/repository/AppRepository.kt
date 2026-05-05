package com.example.coachfitness_belag.data.repository

import com.example.coachfitness_belag.data.api.ApiService
import com.example.coachfitness_belag.data.api.LoginRequest
import com.example.coachfitness_belag.data.api.RegisterRequest
import com.example.coachfitness_belag.data.models.Exercise
import com.example.coachfitness_belag.data.models.User

class AppRepository(
    private val apiService: ApiService
) {


    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.token.isNotEmpty()) {
                Result.success(response.token)
            } else {
                Result.failure(Exception("Échec de connexion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val response = apiService.register(RegisterRequest(name, email, password))
            Result.success(response.token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getAllExercises(): Result<List<Exercise>> {
        return try {
            val exercises = apiService.getAllExercises()
            Result.success(exercises)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExerciseById(id: Int): Result<Exercise> {
        return try {
            val exercise = apiService.getExerciseById(id)
            Result.success(exercise)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExercisesByCategory(category: String): Result<List<Exercise>> {
        return try {
            val exercises = apiService.getExercisesByCategory(category)
            Result.success(exercises)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getUserProfile(): Result<User> {
        return try {
            val user = apiService.getUserProfile()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            val updatedUser = apiService.updateUserProfile(user)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}