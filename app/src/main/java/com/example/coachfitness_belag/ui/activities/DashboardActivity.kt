package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coachfitness_belag.R
import kotlinx.coroutines.launch


class DashboardActivity : AppCompatActivity() {

    // Déclaration des vues
    private lateinit var tvWelcome: TextView
    private lateinit var tvSessionCount: TextView
    private lateinit var progressWeek: ProgressBar
    private lateinit var btnStartSession: LinearLayout
    private lateinit var btnChatbot: LinearLayout
    private lateinit var rvRecommended: RecyclerView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setupListeners()
        loadUserData()
        loadRecommendedExercises()
        loadWeeklyProgress()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvSessionCount = findViewById(R.id.tvSessionCount)
        progressWeek = findViewById(R.id.progressWeek)
        btnStartSession = findViewById(R.id.btnStartSession)
        btnChatbot = findViewById(R.id.btnChatbot)
        rvRecommended = findViewById(R.id.rvRecommended)

        /*
        exerciseAdapter = ExerciseAdapter(exerciseList) { exercise ->
            onExerciseClicked(exercise)
        }
        rvRecommended.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvRecommended.adapter = exerciseAdapter
        */
    }

    private fun setupListeners() {

        btnStartSession.setOnClickListener {
            // startActivity(Intent(this, StartSessionActivity::class.java))
        }


        btnChatbot.setOnClickListener {
            // startActivity(Intent(this, ChatbotActivity::class.java))
        }
    }

    private fun loadUserData() {

        val sharedPref = getSharedPreferences("coach_fitness_prefs", MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "Coach")
        val userName = userEmail?.split("@")?.first() ?: "Coach"

        tvWelcome.text = userName.replaceFirstChar { it.uppercase() }
    }

    private fun loadRecommendedExercises() {
        lifecycleScope.launch {
            try {

                // val exercises = getRecommendedExercises()
                // exerciseList.clear()
                // exerciseList.addAll(exercises)
                // exerciseAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadWeeklyProgress() {
        lifecycleScope.launch {
            try {

                val sessionCount = getWeeklySessionCount()
                tvSessionCount.text = sessionCount.toString()


                val progress = (sessionCount * 100) / 5
                progressWeek.progress = progress.coerceAtMost(100)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun getWeeklySessionCount(): Int {

        // Thread.sleep(300)


        // val sessions = apiService.getUserSessions(userId)
        // return sessions.filter { it.date.isAfter(startOfWeek) }.size

        return 3
    }


    override fun onResume() {
        super.onResume()

        loadWeeklyProgress()
    }
}
