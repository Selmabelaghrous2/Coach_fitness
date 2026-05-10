package com.example.coachfitness_belag.data.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    val token: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val email: String? = null,
    val name: String? = null,
    val message: String? = null
)