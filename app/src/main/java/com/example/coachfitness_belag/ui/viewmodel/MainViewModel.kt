package com.example.coachfitness_belag.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coachfitness_belag.data.models.Exercise
import com.example.coachfitness_belag.data.models.User
import com.example.coachfitness_belag.data.repository.AppRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises

    private val _selectedExercise = MutableLiveData<Exercise?>()
    val selectedExercise: LiveData<Exercise?> = _selectedExercise

    private val _authToken = MutableLiveData<String?>()
    val authToken: LiveData<String?> = _authToken

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn


    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedCategory = MutableLiveData("all")
    val selectedCategory: LiveData<String> = _selectedCategory

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.onSuccess { token ->
                _authToken.value = token
                _isLoggedIn.value = true
                _errorMessage.value = null
                getUserProfile()  // Charger le profil après login
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Erreur de connexion"
                _isLoggedIn.value = false
            }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(name, email, password)
            result.onSuccess { token ->
                _authToken.value = token
                _isLoggedIn.value = true
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Erreur d'inscription"
            }
            _isLoading.value = false
        }
    }

    fun loadAllExercises() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getAllExercises()
            result.onSuccess { exercises ->
                _exercises.value = exercises
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Erreur chargement exercices"
            }
            _isLoading.value = false
        }
    }

    fun loadExercisesByCategory(category: String) {
        _selectedCategory.value = category
        _isLoading.value = true
        viewModelScope.launch {
            if (category == "all") {
                loadAllExercises()
            } else {
                val result = repository.getExercisesByCategory(category)
                result.onSuccess { exercises ->
                    _exercises.value = exercises
                    _errorMessage.value = null
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Erreur chargement catégorie"
                }
                _isLoading.value = false
            }
        }
    }

    fun selectExercise(exercise: Exercise) {
        _selectedExercise.value = exercise
    }

    fun clearSelectedExercise() {
        _selectedExercise.value = null
    }

    fun getUserProfile() {
        viewModelScope.launch {
            val result = repository.getUserProfile()
            result.onSuccess { user ->
                _userProfile.value = user
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Erreur chargement profil"
            }
        }
    }

    fun updateUserProfile(user: User) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateUserProfile(user)
            result.onSuccess { updatedUser ->
                _userProfile.value = updatedUser
                _errorMessage.value = null
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Erreur mise à jour profil"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _authToken.value = null
        _isLoggedIn.value = false
        _userProfile.value = null
        _exercises.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }


    fun getExercisesByDifficulty(difficulty: String): List<Exercise> {
        return _exercises.value?.filter { it.difficulty == difficulty } ?: emptyList()
    }

    fun getTotalCaloriesBurned(): Int {
        return _exercises.value?.sumOf { it.calories } ?: 0
    }
}