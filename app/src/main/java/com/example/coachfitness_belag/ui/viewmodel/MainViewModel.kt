package com.example.coachfitness_belag.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coachfitness_belag.data.models.Exercise
import com.example.coachfitness_belag.data.models.User
import com.example.coachfitness_belag.data.repository.AppRepository
import com.example.coachfitness_belag.utils.TokenManager
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises
    
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _isLoggedIn = MutableLiveData(TokenManager.isLoggedIn())
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.login(email, password).onSuccess { response ->
                TokenManager.saveToken(response.token)
                TokenManager.saveUserData(response.userId.toString(), response.email, response.name)
                _isLoggedIn.value = true
                _errorMessage.value = null
            }.onFailure { e ->
                // This will show exactly what failed (Timeout, 404, etc.)
                _errorMessage.value = "Login Error: ${e.localizedMessage}"
            }
            _isLoading.value = false
        }
    }

    fun loadAllExercises() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getAllExercises().onSuccess { exercises ->
                _exercises.value = exercises
                _errorMessage.value = null
            }.onFailure { e ->
                _errorMessage.value = "Load Exercises Error: ${e.localizedMessage}"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun register(name: String, email: String, password: String, level: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.register(name, email, password, level).onSuccess { response ->
                if (response.token != null) {
                    TokenManager.saveToken(response.token)
                    TokenManager.saveUserData(response.userId?.toString() ?: "", email, name)
                    _isLoggedIn.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = response.message ?: "Erreur d'inscription"
                }
            }.onFailure { e ->
                _errorMessage.value = "Register Error: ${e.localizedMessage}"
            }
            _isLoading.value = false
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            val token = TokenManager.getToken()
            if (token != null) {
                repository.logout(token)
            }
            TokenManager.clear()
            _isLoggedIn.value = false
            _exercises.value = emptyList()
            _userProfile.value = null
        }
    }

    fun loadExercisesByCategory(category: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getExercisesByCategory(category).onSuccess { exercises ->
                _exercises.value = exercises
                _errorMessage.value = null
            }.onFailure { e ->
                _errorMessage.value = "Category Error: ${e.localizedMessage}"
            }
            _isLoading.value = false
        }
    }

    fun getUserProfile() {
        viewModelScope.launch {
            repository.getUserProfile().onSuccess { user ->
                _userProfile.value = user
            }.onFailure { e ->
                _errorMessage.value = "Profile Error: ${e.localizedMessage}"
            }
        }
    }

    fun updateUserProfile(user: User) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.updateUserProfile(user).onSuccess { updatedUser ->
                _userProfile.value = updatedUser
                _errorMessage.value = null
            }.onFailure { e ->
                _errorMessage.value = "Update Profile Error: ${e.localizedMessage}"
            }
            _isLoading.value = false
        }
    }
}
