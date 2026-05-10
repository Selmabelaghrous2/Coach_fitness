package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.api.RetrofitInstance
import com.example.coachfitness_belag.data.repository.AppRepository
import com.example.coachfitness_belag.ui.viewmodel.MainViewModel
import com.example.coachfitness_belag.utils.TokenManager

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if already logged in - if so, go to scanner for profiling
        if (TokenManager.isLoggedIn()) {
            navigateToScanner()
            return
        }

        initViews()
        setupViewModel()
        setupObservers()
        setupClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val repository = AppRepository(RetrofitInstance.apiService)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository) as T
            }
        }).get(MainViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
        }

        viewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                // After successful login, go to Morphology Scanner
                navigateToScanner()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email requis"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Mot de passe requis"
            return false
        }
        if (password.length < 6) {
            etPassword.error = "Mot de passe trop court"
            return false
        }
        return true
    }

    private fun navigateToScanner() {
        val intent = Intent(this, MorphologyScannerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
