package com.example.coachfitness_belag.data.models

data class User(
    val id: String,
    val email: String,
    val name: String,
    val level: String? = null,
    val age: Int? = null,
    val weight: Float? = null,
    val height: Float? = null
)