package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.utils.TokenManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnStartSession: LinearLayout
    private lateinit var btnChatbot: LinearLayout
    private lateinit var btnLocation: LinearLayout
    private lateinit var ivProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        if (!TokenManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        initViews()
        setupHeader()
        setupClickListeners()

        if (savedInstanceState == null) {
            val morphology = intent.getStringExtra("FILTER_MORPHOLOGY")
            loadExerciseFragment(morphology)
        }
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnStartSession = findViewById(R.id.btnStartSession)
        btnChatbot = findViewById(R.id.btnChatbot)
        btnLocation = findViewById(R.id.btnLocation)
        ivProfile = findViewById(R.id.ivProfile)
    }

    private fun setupHeader() {
        val userName = TokenManager.getUserName() ?: "Coach"
        tvWelcome.text = "$userName !"
    }

    private fun loadExerciseFragment(morphology: String? = null) {
        val fragment = ExerciseListFragment().apply {
            arguments = Bundle().apply {
                putString("MORPHOLOGY", morphology)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupClickListeners() {
        btnStartSession.setOnClickListener {
            Toast.makeText(this, "Chargement des exercices...", Toast.LENGTH_SHORT).show()
            loadExerciseFragment()
        }

        btnChatbot.setOnClickListener {
            val intent = Intent(this, ChatbotActivity::class.java)
            startActivity(intent)
        }

        btnLocation.setOnClickListener {
            val intent = Intent(this, Mapview::class.java)
            startActivity(intent)
        }

        ivProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
