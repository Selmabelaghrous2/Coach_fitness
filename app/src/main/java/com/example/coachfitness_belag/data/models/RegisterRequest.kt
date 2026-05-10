package com.example.coachfitness_belag.data.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val level: String? = null
)