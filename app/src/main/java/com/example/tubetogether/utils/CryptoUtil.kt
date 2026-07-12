package com.example.tubetogether.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private const val KEY = "TubeTogetherSecureSecretKey32Bit"
    private const val IV = "1234567890123456"

    fun encrypt(data: String): String {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(KEY.toByteArray(Charsets.UTF_8), "AES")
            val ivParameterSpec = IvParameterSpec(IV.toByteArray(Charsets.UTF_8))
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun decrypt(base64Str: String): String {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(KEY.toByteArray(Charsets.UTF_8), "AES")
            val ivParameterSpec = IvParameterSpec(IV.toByteArray(Charsets.UTF_8))
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
            val decodedBytes = Base64.decode(base64Str, Base64.NO_WRAP) // IMPORTANT: Use NO_WRAP
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
