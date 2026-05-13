package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.api.RetrofitInstance
import com.example.coachfitness_belag.data.repository.AppRepository
import com.example.coachfitness_belag.ui.viewmodel.MainViewModel
import com.example.coachfitness_belag.utils.TokenManager
import java.util.*

class LoginActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var ivLogo: ImageView
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize TTS
        tts = TextToSpeech(this, this)

        // Check if already logged in
        if (TokenManager.isLoggedIn()) {
            navigateToScanner()
            return
        }

        initViews()
        setupViewModel()
        setupObservers()
        setupClickListeners()
        loadAvatar()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)
        ivLogo = findViewById(R.id.ivLogo)
    }

    private fun loadAvatar() {
        Glide.with(this)
            .asGif()
            .load("file:///android_asset/avatar/myavatar.gif")
            .into(ivLogo)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.FRENCH
            speak("Bonjour, veuillez vous authentifier")
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
                navigateToScanner()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                speak(it)
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
        return true
    }

    private fun navigateToScanner() {
        val intent = Intent(this, MorphologyScannerActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
