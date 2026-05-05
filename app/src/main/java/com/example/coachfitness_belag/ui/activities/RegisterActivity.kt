package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.coachfitness_belag.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {


    private lateinit var btnBack: ImageButton
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var chipGroupLevel: ChipGroup
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView

    private var selectedLevel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        chipGroupLevel = findViewById(R.id.chipGroupLevel)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupListeners() {

        btnBack.setOnClickListener {
            finish()
        }

        // Sélection du niveau
        chipGroupLevel.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds[0])
                selectedLevel = selectedChip.text.toString()
            } else {
                selectedLevel = null
            }
        }


        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            performRegistration(email, password, selectedLevel)
        }


        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun performRegistration(email: String, password: String, niveau: String?) {
        // Validation des champs
        if (!validateInputs(email, password, niveau)) {
            return
        }


        btnRegister.isEnabled = false
        btnRegister.text = "CRÉATION..."

        lifecycleScope.launch {
            try {

                simulateRegistration(email, password, niveau)

            } catch (e: Exception) {
                showError("Erreur d'inscription : ${e.message}")
            } finally {
                btnRegister.isEnabled = true
                btnRegister.text = "CRÉER MON COMPTE"
            }
        }
    }

    private fun validateInputs(email: String, password: String, niveau: String?): Boolean {
        var isValid = true

        // Validation email
        if (email.isEmpty()) {
            tilEmail.error = "L'email est requis"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Email invalide"
            isValid = false
        } else {
            tilEmail.error = null
        }

        // Validation mot de passe
        if (password.isEmpty()) {
            tilPassword.error = "Le mot de passe est requis"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Au moins 6 caractères"
            isValid = false
        } else {
            tilPassword.error = null
        }


        if (niveau == null) {
            Toast.makeText(this, "Veuillez sélectionner votre niveau", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    // Simulation d'inscription (à remplacer par votre API réelle)
    private fun simulateRegistration(email: String, password: String, niveau: String?) {
        // Simuler une requête réseau
        // Thread.sleep(1500)

        // Simulation de succès (À SUPPRIMER avec votre vraie API)
        showSuccess("Inscription réussie ! Bienvenue !")


        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showSuccess(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
