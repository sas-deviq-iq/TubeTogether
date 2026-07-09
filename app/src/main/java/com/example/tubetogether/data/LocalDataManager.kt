package com.example.tubetogether.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.tubetogether.api.VideoResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LocalDataManager {
    private const val PREFS_NAME = "TubeTogetherPrefs"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_PROGRESS = "watch_progress"
    private const val KEY_PROGRESS_V2 = "watch_progress_v2"
    private const val KEY_LAST_EPISODE = "last_episode"

    data class WatchProgressV2(
        val positionMs: Long,
        val durationMs: Long
    )

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Fallback or handle error (e.g. key corrupted). For a robust app, clearing the old prefs might be needed.
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
                // Retry creating encrypted
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
        }
    }

    // --- Favorites ---
    fun getFavorites(): List<VideoResponse> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<VideoResponse>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun isFavorite(videoId: String): Boolean {
        return getFavorites().any { it.id == videoId || it.videoId == videoId || it.nb == videoId }
    }

    fun addFavorite(video: VideoResponse) {
        val current = getFavorites().toMutableList()
        val videoId = video.id ?: video.videoId ?: video.nb
        if (!current.any { it.id == videoId || it.videoId == videoId || it.nb == videoId }) {
            current.add(video)
            prefs.edit().putString(KEY_FAVORITES, gson.toJson(current)).apply()
        }
    }

    fun removeFavorite(videoId: String) {
        val current = getFavorites().toMutableList()
        current.removeAll { it.id == videoId || it.videoId == videoId || it.nb == videoId }
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(current)).apply()
    }

    // --- Watch Progress ---
    // Legacy support
    fun saveWatchProgress(videoId: String, positionMs: Long) {
        val json = prefs.getString(KEY_PROGRESS, null)
        val type = object : TypeToken<MutableMap<String, Long>>() {}.type
        val map: MutableMap<String, Long> = if (json != null) {
            gson.fromJson(json, type) ?: mutableMapOf()
        } else {
            mutableMapOf()
        }
        
        map[videoId] = positionMs
        prefs.edit().putString(KEY_PROGRESS, gson.toJson(map)).apply()
    }

    fun getWatchProgress(videoId: String): Long {
        // Prefer V2 if it exists
        val v2 = getWatchProgressV2(videoId)
        if (v2 != null) return v2.positionMs

        val json = prefs.getString(KEY_PROGRESS, null) ?: return 0L
        val type = object : TypeToken<Map<String, Long>>() {}.type
        val map: Map<String, Long> = gson.fromJson(json, type) ?: emptyMap()
        return map[videoId] ?: 0L
    }

    // New V2 format
    fun saveWatchProgressV2(videoId: String, positionMs: Long, durationMs: Long) {
        val json = prefs.getString(KEY_PROGRESS_V2, null)
        val type = object : TypeToken<MutableMap<String, WatchProgressV2>>() {}.type
        val map: MutableMap<String, WatchProgressV2> = if (json != null) {
            gson.fromJson(json, type) ?: mutableMapOf()
        } else {
            mutableMapOf()
        }
        
        map[videoId] = WatchProgressV2(positionMs, durationMs)
        prefs.edit().putString(KEY_PROGRESS_V2, gson.toJson(map)).apply()
    }

    fun getWatchProgressV2(videoId: String): WatchProgressV2? {
        val json = prefs.getString(KEY_PROGRESS_V2, null) ?: return null
        val type = object : TypeToken<Map<String, WatchProgressV2>>() {}.type
        val map: Map<String, WatchProgressV2> = gson.fromJson(json, type) ?: emptyMap()
        return map[videoId]
    }

    // --- Last Watched Episode ---
    // Storing as Map of seriesId -> episodeId
    fun saveLastWatchedEpisode(seriesId: String, episodeId: String) {
        val json = prefs.getString(KEY_LAST_EPISODE, null)
        val type = object : TypeToken<MutableMap<String, String>>() {}.type
        val map: MutableMap<String, String> = if (json != null) {
            gson.fromJson(json, type) ?: mutableMapOf()
        } else {
            mutableMapOf()
        }
        
        map[seriesId] = episodeId
        prefs.edit().putString(KEY_LAST_EPISODE, gson.toJson(map)).apply()
    }

    fun getLastWatchedEpisode(seriesId: String): String? {
        val json = prefs.getString(KEY_LAST_EPISODE, null) ?: return null
        val type = object : TypeToken<Map<String, String>>() {}.type
        val map: Map<String, String> = gson.fromJson(json, type) ?: emptyMap()
        return map[seriesId]
    }
}
