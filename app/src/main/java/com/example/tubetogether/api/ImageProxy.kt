package com.example.tubetogether.api

import com.example.tubetogether.utils.CryptoUtil

object ImageProxy {
    fun getProxiedUrl(originalUrl: String?): String {
        if (originalUrl == null) return ""
        var url = originalUrl
        if (url.startsWith("//")) url = "https:$url"
        
        // We no longer proxy through VPS for images.
        // Coil is configured in MainActivity to inject the necessary headers directly.
        
        return url
    }
}
