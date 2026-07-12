package com.example.tubetogether.api

import com.example.tubetogether.utils.CryptoUtil
import java.net.URLDecoder
import java.net.URLEncoder

object ImageProxy {
    fun getProxiedUrl(originalUrl: String?): String {
        if (originalUrl == null) return ""
        var url = originalUrl
        if (url.startsWith("//")) url = "https:$url"

        // TMDB's CDN is globally available (not ISP-blocked like shabakaty), and the
        // existing proxy routes are hardwired to shabakaty targets - so serve it directly.
        if (url.contains("image.tmdb.org")) return url

        val proxyBase = CryptoUtil.decrypt("RRd4PRdMI3wYWyJ/H1MifB9TIn8dVzZ1HVs8YkwTZWJJGmIsQApvclkCfipIFzE=")
        // Prevent double proxying
        if (url.startsWith(proxyBase)) {
            return url
        }
        if (url.contains("158.220.120.204:8080/api/dynamic")) {
            return url
        }
        return try {
            proxyBase + URLEncoder.encode(url, "UTF-8")
        } catch (e: Exception) {
            url
        }
    }

    fun getOriginalUrl(proxiedUrl: String): String {
        // Dynamic proxy URLs
        if (proxiedUrl.contains("target=")) {
            val target = proxiedUrl.substringAfter("target=")
            return URLDecoder.decode(target, "UTF-8")
        }
        
        // Retrofit API proxy URLs
        val proxyCinemanaBase = CryptoUtil.decrypt("RRd4PRdMI3wYWyJ/H1MifB9TIn8dVzZ1HVs8YkwTZWJOCmIoQAJiLAI=")
        val directCinemanaBase = CryptoUtil.decrypt("RRd4PV5ZI2JOCmIoQAJiLAMQZCxPAmcsWRoiLkIOIw==")
        
        if (proxiedUrl.startsWith(proxyCinemanaBase)) {
            return proxiedUrl.replaceFirst(proxyCinemanaBase, directCinemanaBase)
        }
        
        return proxiedUrl
    }
}

