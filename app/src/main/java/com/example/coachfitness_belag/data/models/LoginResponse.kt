package com.example.coachfitness_belag.data.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val token: String,
    @SerializedName("user_id") val userId: Int,
    val email: String,
    val name: String? = null,
    val message: String? = null
)