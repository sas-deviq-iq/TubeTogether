package com.example.tubetogether.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubetogether.api.auth.AuthApi
import com.example.tubetogether.api.auth.CompleteProfileRequest
import com.example.tubetogether.api.auth.UserProfileDto
import com.example.tubetogether.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserProfileDto) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    // Standalone Retrofit instance for Auth API, mirrors AuthViewModel
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://158.220.120.204:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApi = retrofit.create(AuthApi::class.java)

    init {
        loadProfile()
    }

    fun loadProfile() {
        val token = AuthManager.getToken() ?: return
        viewModelScope.launch {
            _state.value = ProfileState.Loading
            try {
                val profile = authApi.getProfile("Bearer $token")
                _state.value = ProfileState.Success(profile)
            } catch (e: Exception) {
                _state.value = ProfileState.Error("تعذر تحميل بيانات الحساب")
            }
        }
    }

    fun updateProfile(
        name: String,
        username: String,
        dob: String,
        country: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = AuthManager.getToken() ?: return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val response = authApi.completeProfile("Bearer $token", CompleteProfileRequest(name, username, dob, country))
                if (response.success) {
                    AuthManager.setProfileComplete()
                    loadProfile()
                    onSuccess()
                } else {
                    onError("حدث خطأ أثناء حفظ البيانات")
                }
            } catch (e: Exception) {
                onError(if (e.message?.contains("400") == true) "اسم المستخدم هذا محجوز مسبقاً" else "حدث خطأ في الاتصال بالسيرفر")
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun logout() {
        AuthManager.clearSession()
    }
}
