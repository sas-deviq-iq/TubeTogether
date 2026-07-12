package com.example.tubetogether.api.auth

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)
data class GoogleAuthRequest(val idToken: String)

data class UserDto(val id: Int, val name: String, val email: String, val avatar: String?)
data class AuthResponse(val token: String, val user: UserDto)
data class ErrorResponse(val error: String)

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): AuthResponse
}
