package com.example.tubetogether.auth

import android.content.Context
import android.content.SharedPreferences

object AuthManager {
    private const val PREFS_NAME = "TubeTogetherAuth"
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(token: String, name: String, email: String) {
        prefs.edit()
            .putString(KEY_JWT_TOKEN, token)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getString(KEY_JWT_TOKEN, null) != null
    }

    fun getToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }
}
