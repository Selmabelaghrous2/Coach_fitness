package com.example.coachfitness_belag.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUserData(userId: String, email: String, name: String?) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
