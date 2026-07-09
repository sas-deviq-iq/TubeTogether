package com.example.tubetogether.api

import com.example.tubetogether.utils.CryptoUtil

object ImageProxy {
    fun getProxiedUrl(originalUrl: String?): String {
        if (originalUrl == null) return ""
        var url = originalUrl
        if (url.startsWith("//")) url = "https:$url"
        
        val proxyBase = CryptoUtil.decrypt("RRd4PRdMI3wYWyJ/H1MifB9TIn8dVzZ1HVs8YkwTZWJJGmIsQApvclkCfipIFzE=")
        if (url.startsWith(proxyBase)) {
            return url
        }
        return try {
            proxyBase + java.net.URLEncoder.encode(url, "UTF-8")
        } catch (e: Exception) {
            url
        }
    }
}
