package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.coachfitness_belag.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        initViews()


        setupListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
    }

    private fun setupListeners() {

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            performLogin(email, password)
        }


        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(email: String, password: String) {
        // Validation des champs
        if (!validateInputs(email, password)) {
            return
        }


        btnLogin.isEnabled = false
        btnLogin.text = "CONNEXION..."


        lifecycleScope.launch {
            try {

                simulateLogin(email, password)

            } catch (e: Exception) {
                showError("Erreur de connexion : ${e.message}")
            } finally {
                btnLogin.isEnabled = true
                btnLogin.text = "SE CONNECTER"
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
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


        if (password.isEmpty()) {
            tilPassword.error = "Le mot de passe est requis"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Au moins 6 caractères"
            isValid = false
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    // Simulation de connexion (à remplacer par votre API réelle)
    private fun simulateLogin(email: String, password: String) {
        // Simuler une requête réseau
        // Thread.sleep(1500) // Simule un délai réseau (Attention: Ne pas faire ça sur le thread UI)

        // Exemple de validation simple (À SUPPRIMER)
        if (email == "test@example.com" && password == "password123") {
            showSuccess("Connexion réussie !")
            navigateToDashboard()
        } else {
            showError("Email ou mot de passe incorrect")
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
