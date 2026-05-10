package com.example.coachfitness_belag.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.models.Exercise
import com.example.coachfitness_belag.ui.adapter.InstructionsAdapter

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var ivExerciseImage: ImageView
    private lateinit var tvExerciseName: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvDescription: TextView
    private lateinit var rvInstructions: RecyclerView
    private lateinit var btnStart: Button

    private lateinit var exercise: Exercise

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercisedetail)

        // Récupérer l'exercice passé en intent
        val serializable = intent.getSerializableExtra("exercise")
        if (serializable is Exercise) {
            exercise = serializable
        } else {
            finish()
            return
        }

        initViews()
        setupToolbar()
        displayExerciseDetails()
        setupClickListeners()
        setupInstructions()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        ivExerciseImage = findViewById(R.id.ivExerciseImage)
        tvExerciseName = findViewById(R.id.tvExerciseName)
        tvDifficulty = findViewById(R.id.tvDifficulty)
        tvCategory = findViewById(R.id.tvCategory)
        tvDuration = findViewById(R.id.tvDuration)
        tvCalories = findViewById(R.id.tvCalories)
        tvDescription = findViewById(R.id.tvDescription)
        rvInstructions = findViewById(R.id.rvInstructions)
        btnStart = findViewById(R.id.btnStart)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayExerciseDetails() {
        tvExerciseName.text = exercise.name

        // Difficulté
        tvDifficulty.text = when (exercise.difficulty.lowercase()) {
            "easy" -> "DÉBUTANT"
            "medium" -> "INTERMÉDIAIRE"
            "hard" -> "AVANCÉ"
            else -> exercise.difficulty.uppercase()
        }

        tvCategory.text = exercise.category.uppercase()
        tvDuration.text = "${exercise.duration} min"
        tvCalories.text = "${exercise.calories} cal"
        tvDescription.text = exercise.description

        // Attribution de l'image correspondante depuis drawable
        val imageRes = when (exercise.name.lowercase()) {
            "yoga" -> R.drawable.yoga
            "pompes" -> R.drawable.pompes
            "squats" -> R.drawable.squats
            "burpees" -> R.drawable.burpees
            "course", "courses" -> R.drawable.course
            else -> R.drawable.bg_exercise_header // Image par défaut
        }
        ivExerciseImage.setImageResource(imageRes)
    }

    private fun setupInstructions() {
        // Conversion de la String d'instructions en List<String> pour l'adapter
        val instructionsList = if (exercise.instructions.isBlank()) {
            listOf(
                "Échauffez-vous pendant 5 minutes",
                "Adoptez la bonne posture",
                "Effectuez l'exercice lentement",
                "Respirez correctement",
                "Hydratez-vous après l'exercice"
            )
        } else {
            // On sépare la chaîne par les retours à la ligne pour créer une liste
            exercise.instructions.split("\n").filter { it.isNotBlank() }
        }

        val instructionsAdapter = InstructionsAdapter(instructionsList)
        rvInstructions.apply {
            layoutManager = LinearLayoutManager(this@ExerciseDetailActivity)
            adapter = instructionsAdapter
        }
    }

    private fun setupClickListeners() {
        btnStart.setOnClickListener {
            Toast.makeText(this, "Démarrage de ${exercise.name}", Toast.LENGTH_SHORT).show()
            // Démarrer l'entraînement (timer, compteur, etc.)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
