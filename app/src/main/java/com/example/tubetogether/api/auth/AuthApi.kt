package com.example.tubetogether.api.auth

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)
data class GoogleAuthRequest(val idToken: String)

data class UserDto(val id: Int, val name: String, val email: String, val avatar: String?, val profile_complete: Int)
data class AuthResponse(val token: String, val user: UserDto)
data class ErrorResponse(val error: String)

data class CompleteProfileRequest(val name: String, val username: String, val dob: String, val country: String)
data class CompleteProfileResponse(val success: Boolean, val profile_complete: Int)

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): AuthResponse

    @POST("api/auth/complete-profile")
    suspend fun completeProfile(
        @retrofit2.http.Header("Authorization") token: String,
        @Body request: CompleteProfileRequest
    ): CompleteProfileResponse
}
