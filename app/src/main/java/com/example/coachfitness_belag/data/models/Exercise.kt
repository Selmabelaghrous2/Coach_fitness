package com.example.coachfitness_belag.data.models

data class Exercise(
    val id: Int,
    val name: String,
    val category: String,      // "cardio", "musculation", "yoga", etc.
    val duration: Int,          // durée en minutes
    val calories: Int,          // calories brûlées
    val difficulty: String,     // "facile", "moyen", "difficile"
    val imageUrl: String? = null,
    val description: String,
    val instructions: List<String> = emptyList()
)

// Exercice avec progression (pour historique)
data class ExerciseProgress(
    val exerciseId: Int,
    val date: String,
    val duration: Int,
    val caloriesBurned: Int,
    val completed: Boolean = false
)

// Catégories d'exercices
object ExerciseCategories {
    const val CARDIO = "cardio"
    const val MUSCULATION = "musculation"
    const val YOGA = "yoga"
    const val STRETCHING = "stretching"
    const val HIIT = "hiit"
}

// Utilisateur
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int? = null,
    val weight: Double? = null,  // poids en kg
    val height: Double? = null,   // taille en cm
    val fitnessLevel: String? = null  // "débutant", "intermédiaire", "avancé"
)