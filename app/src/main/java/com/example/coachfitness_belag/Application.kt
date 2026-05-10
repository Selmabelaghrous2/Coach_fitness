package com.example.coachfitness_belag

import android.app.Application
import com.example.coachfitness_belag.utils.TokenManager

class CoachFitnessApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}