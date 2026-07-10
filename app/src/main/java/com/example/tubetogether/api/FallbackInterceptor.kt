package com.example.tubetogether.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class FallbackInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            val response = chain.proceed(request)
            // If the proxy returns a server error (e.g. 502 Bad Gateway), it means the proxy is down or failing.
            if (!response.isSuccessful && response.code >= 500) {
                response.close()
                return tryFallback(chain, request)
            }
            return response
        } catch (e: Exception) {
            // Catch connection timeouts, unknown host, etc.
            return tryFallback(chain, request)
        }
    }

    private fun tryFallback(chain: Interceptor.Chain, request: okhttp3.Request): Response {
        val currentUrl = request.url.toString()
        val originalUrl = ImageProxy.getOriginalUrl(currentUrl)
        
        if (originalUrl != currentUrl) {
            val fallbackRequest = request.newBuilder()
                .url(originalUrl)
                .build()
            return chain.proceed(fallbackRequest)
        }
        
        throw IOException("Proxy connection failed and no fallback available for: $currentUrl")
    }
}

