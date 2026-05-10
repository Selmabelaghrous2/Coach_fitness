package com.example.coachfitness_belag.data.models

import com.google.gson.annotations.SerializedName

data class ExerciseResponse(
    @SerializedName("exercises")
    val exercises: List<Exercise>? = null,
    val message: String? = null,
    val status: String? = null
)
