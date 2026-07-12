package com.example.tubetogether.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubetogether.api.TubeApi
import com.example.tubetogether.api.auth.AuthApi
import com.example.tubetogether.api.auth.LoginRequest
import com.example.tubetogether.api.auth.RegisterRequest
import com.example.tubetogether.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userName: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Create a standalone Retrofit instance for Auth API pointing to VPS port 8080
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://158.220.120.204:8080/") // VPS IP
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        
    private val authApi = retrofit.create(AuthApi::class.java)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("الرجاء إدخال البريد وكلمة المرور")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = authApi.login(LoginRequest(email, password))
                AuthManager.saveSession(response.token, response.user.name, response.user.email)
                _authState.value = AuthState.Success(response.user.name)
                onSuccess()
            } catch (e: Exception) {
                // To safely handle HTTP 400 errors we could parse the error body, but for simplicity:
                _authState.value = AuthState.Error(if (e.message?.contains("400") == true) "بيانات الدخول غير صحيحة" else "حدث خطأ في الاتصال بالسيرفر")
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("جميع الحقول مطلوبة")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = authApi.register(RegisterRequest(name, email, password))
                AuthManager.saveSession(response.token, response.user.name, response.user.email)
                _authState.value = AuthState.Success(response.user.name)
                onSuccess()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(if (e.message?.contains("400") == true) "البريد الإلكتروني مسجل مسبقاً" else "حدث خطأ في الاتصال بالسيرفر")
            }
        }
    }

    // Google Auth will be implemented later once we add CredentialManager
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
