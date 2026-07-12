package com.example.tubetogether.api

import com.example.tubetogether.auth.AuthManager
import com.example.tubetogether.utils.CryptoUtil
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.URLEncoder

class SecureNetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val originalUrl = request.url

        // 1. Add JWT Token for Authentication
        val token = AuthManager.getToken()
        val requestBuilder = request.newBuilder()
        
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // 2. Encrypt the outgoing request URL if it's an API call to cinemana
        if (originalUrl.encodedPath.startsWith("/api/cinemana/")) {
            // Extract the path and query AFTER /api/cinemana/
            val pathSegment = originalUrl.encodedPath.substringAfter("/api/cinemana/")
            val query = originalUrl.query
            val targetPathAndQuery = if (query.isNullOrEmpty()) pathSegment else "$pathSegment?$query"

            // Encrypt it
            val encryptedData = CryptoUtil.encrypt(targetPathAndQuery)
            
            // Build the new secure URL
            val secureUrl = originalUrl.newBuilder()
                .encodedPath("/api/secure")
                .query(null) // Remove old queries
                .addQueryParameter("data", encryptedData)
                .build()

            requestBuilder.url(secureUrl)
        }

        request = requestBuilder.build()

        // 3. Proceed with the request
        val response = chain.proceed(request)

        // 4. Decrypt the response body if it's successful and from the secure endpoint
        if (response.isSuccessful && request.url.encodedPath == "/api/secure") {
            val responseBody = response.body
            if (responseBody != null) {
                val encryptedText = responseBody.string()
                // If the response is truly encrypted, it shouldn't start with { or [
                if (encryptedText.isNotEmpty() && !encryptedText.startsWith("{") && !encryptedText.startsWith("[")) {
                    val decryptedJson = CryptoUtil.decrypt(encryptedText)
                    val newBody = decryptedJson.toResponseBody("application/json".toMediaTypeOrNull())
                    return response.newBuilder()
                        .body(newBody)
                        .build()
                } else {
                    // Fallback in case the server sent plain text (e.g. during development/errors)
                    val newBody = encryptedText.toResponseBody(responseBody.contentType())
                    return response.newBuilder().body(newBody).build()
                }
            }
        }

        return response
    }
}
