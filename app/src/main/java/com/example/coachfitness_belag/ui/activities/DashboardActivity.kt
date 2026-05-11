package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.utils.TokenManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvProfileBadge: TextView
    private lateinit var btnStartSession: LinearLayout
    private lateinit var btnChatbot: LinearLayout
    private lateinit var btnLocation: LinearLayout
    private lateinit var ivProfile: ImageView
    private lateinit var tvExerciseTitle: TextView

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

        // Récupération de la morphologie après le scan
        val morphology = intent.getStringExtra("FILTER_MORPHOLOGY")
        updateUIForMorphology(morphology)
        
        if (savedInstanceState == null) {
            loadExerciseFragment(morphology)
        }
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvProfileBadge = findViewById(R.id.tvProfileBadge)
        btnStartSession = findViewById(R.id.btnStartSession)
        btnChatbot = findViewById(R.id.btnChatbot)
        btnLocation = findViewById(R.id.btnLocation)
        ivProfile = findViewById(R.id.ivProfile)
        tvExerciseTitle = findViewById(R.id.tvExerciseTitle)
    }

    private fun setupHeader() {
        val userName = TokenManager.getUserName() ?: "Coach"
        tvWelcome.text = "$userName !"
    }

    private fun updateUIForMorphology(morphology: String?) {
        if (morphology != null) {
            tvProfileBadge.visibility = View.VISIBLE
            tvProfileBadge.text = "Profil détecté : $morphology"
            
            // Personnalisation selon le type
            when (morphology) {
                "Ectomorph" -> {
                    tvExerciseTitle.text = "Programme Force & Volume"
                    tvProfileBadge.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
                }
                "Mesomorph" -> {
                    tvExerciseTitle.text = "Programme Performance Athlétique"
                    tvProfileBadge.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                }
                "Endomorph" -> {
                    tvExerciseTitle.text = "Programme Cardio & Définition"
                    tvProfileBadge.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                }
            }
        } else {
            tvProfileBadge.visibility = View.GONE
            tvExerciseTitle.text = "Vos exercices recommandés"
        }
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
            loadExerciseFragment() // Recharge tout
            Toast.makeText(this, "Session démarrée", Toast.LENGTH_SHORT).show()
        }

        btnChatbot.setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        btnLocation.setOnClickListener {
            startActivity(Intent(this, Mapview::class.java))
        }

        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
