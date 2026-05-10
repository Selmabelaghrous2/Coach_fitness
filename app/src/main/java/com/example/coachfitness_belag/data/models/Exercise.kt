package com.example.coachfitness_belag.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Exercise(
    val id: Int,
    val name: String,
    val category: String,      // "cardio", "musculation", "yoga", etc.
    val duration: Int,          // durée en minutes
    val calories: Int,          // calories brûlées
    val difficulty: String,     // "facile", "moyen", "difficile"
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    val description: String,
    
    // Changed from List<String> to String to match the backend response
    val instructions: String
) : Serializable

// Exercice avec progression (pour historique)
data class ExerciseProgress(
    val exerciseId: Int,
    val date: String,
    val duration: Int,
    val caloriesBurned: Int,
    val completed: Boolean = false
) : Serializable

// Catégories d'exercices
object ExerciseCategories {
    const val CARDIO = "cardio"
    const val MUSCULATION = "musculation"
    const val YOGA = "yoga"
    const val STRETCHING = "stretching"
    const val HIIT = "hiit"
}
