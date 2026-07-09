package com.example.tubetogether.utils

import android.util.Base64

object CryptoUtil {
    private val key = intArrayOf(45, 99, 12, 77)

    fun decrypt(base64Str: String): String {
        try {
            val decoded = Base64.decode(base64Str, Base64.DEFAULT)
            val result = CharArray(decoded.size)
            for (i in decoded.indices) {
                result[i] = (decoded[i].toInt() xor key[i % key.size]).toChar()
            }
            return String(result)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
