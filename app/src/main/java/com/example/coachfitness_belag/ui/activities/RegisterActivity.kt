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
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class RegisterActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var btnBack: ImageButton
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var chipGroupLevel: ChipGroup
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var ivAvatarPreview: ImageView
    private var tts: TextToSpeech? = null

    private var selectedLevel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tts = TextToSpeech(this, this)

        initViews()
        setupViewModel()
        setupObservers()
        setupListeners()
        loadAvatar()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        chipGroupLevel = findViewById(R.id.chipGroupLevel)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
        ivAvatarPreview = findViewById(R.id.ivAvatarPreview)
    }

    private fun loadAvatar() {
        Glide.with(this)
            .asGif()
            .load("file:///android_asset/avatar/myavatar.gif")
            .into(ivAvatarPreview)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.FRENCH
            speak("Bienvenue, créez votre compte pour commencer")
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
            btnRegister.isEnabled = !isLoading
            btnRegister.text = if (isLoading) "CRÉATION..." else "CRÉER MON COMPTE"
        }

        viewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show()
                navigateToDashboard()
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

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        chipGroupLevel.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds[0])
                selectedLevel = selectedChip.text.toString()
            } else {
                selectedLevel = null
            }
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(email, password, selectedLevel)) {
                viewModel.register(
                    if(name.isEmpty()) "Utilisateur" else name, 
                    email, 
                    password, 
                    selectedLevel
                )
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(email: String, password: String, niveau: String?): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            tilEmail.error = "L'email est requis"
            isValid = false
        } else {
            tilEmail.error = null
        }
        if (password.length < 6) {
            tilPassword.error = "Au moins 6 caractères"
            isValid = false
        } else {
            tilPassword.error = null
        }
        if (niveau == null) {
            Toast.makeText(this, "Sélectionnez votre niveau", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}