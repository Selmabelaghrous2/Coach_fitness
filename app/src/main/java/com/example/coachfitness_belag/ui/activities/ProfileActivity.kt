package com.example.coachfitness_belag.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.api.RetrofitInstance
import com.example.coachfitness_belag.data.repository.AppRepository
import com.example.coachfitness_belag.ui.viewmodel.MainViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAge: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var tvTotalExercises: TextView
    private lateinit var tvTotalMinutes: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button
    private lateinit var tvDeleteAccount: TextView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupToolbar()
        setupViewModel()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etAge = findViewById(R.id.etAge)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        tvTotalExercises = findViewById(R.id.tvTotalExercises)
        tvTotalMinutes = findViewById(R.id.tvTotalMinutes)
        tvTotalCalories = findViewById(R.id.tvTotalCalories)
        progressBar = findViewById(R.id.progressBar)
        btnSave = findViewById(R.id.btnSave)
        btnLogout = findViewById(R.id.btnLogout)
        tvDeleteAccount = findViewById(R.id.tvDeleteAccount)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewModel() {
        val repository = AppRepository(RetrofitInstance.apiService)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
        }).get(MainViewModel::class.java)

        // Observer le profil utilisateur
        viewModel.userProfile.observe(this) { user ->
            user?.let {
                etName.setText(it.name)
                etEmail.setText(it.email)
                etAge.setText(it.age?.toString() ?: "")
                etWeight.setText(it.weight?.toString() ?: "")
                etHeight.setText(it.height?.toString() ?: "")
            }
        }

        // Observer le chargement
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Observer les erreurs
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun loadUserData() {
        viewModel.getUserProfile()
        loadStatistics()
    }

    private fun loadStatistics() {
        // Simuler des statistiques (à remplacer par des données réelles)
        viewModel.exercises.observe(this) { exercises ->
            exercises?.let {
                tvTotalExercises.text = it.size.toString()
                tvTotalMinutes.text = it.sumOf { exercise -> exercise.duration }.toString()
                tvTotalCalories.text = it.sumOf { exercise -> exercise.calories }.toString()
            }
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveUserProfile()
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        tvDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun saveUserProfile() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val age = etAge.text.toString().toIntOrNull()
        val weight = etWeight.text.toString().toFloatOrNull()
        val height = etHeight.text.toString().toFloatOrNull()

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = viewModel.userProfile.value
        val updatedUser = currentUser?.copy(
            name = name,
            email = email,
            age = age,
            weight = weight,
            height = height
        )

        updatedUser?.let {
            viewModel.updateUserProfile(it)
            Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Voulez-vous vraiment vous déconnecter ?")
            .setPositiveButton("Oui") { _, _ ->
                logout()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun logout() {
        viewModel.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer le compte")
            .setMessage("Êtes-vous sûr de vouloir supprimer définitivement votre compte ? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun deleteAccount() {
        // Implémenter la suppression du compte
        Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show()
        logout()
    }
}