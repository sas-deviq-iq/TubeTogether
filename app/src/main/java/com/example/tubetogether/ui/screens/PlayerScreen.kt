package com.example.tubetogether.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tubetogether.data.LocalDataManager
import org.json.JSONObject

private const val VIDKING_BASE = "https://www.vidking.net/embed"
private const val PLAYER_COLOR = "e50914" // matches app's Netflix-red theme

private data class PlayableId(
    val mediaType: String, // "movie" or "tv"
    val tmdbId: String,
    val season: String? = null,
    val episode: String? = null,
    val seriesKey: String // used as the LocalDataManager progress key
)

private fun parsePlayId(playId: String): PlayableId? {
    val parts = playId.split(":")
    return when {
        parts.size >= 2 && parts[0] == "movie" -> PlayableId("movie", parts[1], seriesKey = playId)
        parts.size >= 4 && parts[0] == "tv" -> PlayableId("tv", parts[1], parts[2], parts[3], seriesKey = playId)
        else -> null
    }
}

private fun buildEmbedUrl(playable: PlayableId, startSeconds: Long): String {
    val path = if (playable.mediaType == "movie") {
        "movie/${playable.tmdbId}"
    } else {
        "tv/${playable.tmdbId}/${playable.season}/${playable.episode}"
    }
    val params = mutableListOf(
        "color=$PLAYER_COLOR",
        "autoPlay=true",
        "nextEpisode=true",
        "episodeSelector=true"
    )
    if (startSeconds > 0) params.add("progress=$startSeconds")
    return "$VIDKING_BASE/$path?${params.joinToString("&")}"
}

private class ProgressBridge(private val onEvent: (JSONObject) -> Unit) {
    @JavascriptInterface
    fun onMessage(json: String) {
        try {
            onEvent(JSONObject(json))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(playId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    val playable = remember(playId) { parsePlayId(playId) }

    // Force landscape mode and immersive fullscreen for video player
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        val window = activity?.window
        if (window != null) {
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (window != null) {
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
                val insetsController = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
                insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (playable == null) {
            isLoading = false
        } else {
            val startSeconds = LocalDataManager.getWatchProgress(playable.seriesKey) / 1000
            val embedUrl = remember(playId) { buildEmbedUrl(playable, startSeconds) }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true

                        addJavascriptInterface(
                            ProgressBridge { data ->
                                val payload = data.optJSONObject("data") ?: return@ProgressBridge
                                val currentTimeSec = payload.optDouble("currentTime", 0.0)
                                val durationSec = payload.optDouble("duration", 0.0)
                                if (currentTimeSec > 0 && durationSec > 0) {
                                    LocalDataManager.saveWatchProgressV2(
                                        playable.seriesKey,
                                        (currentTimeSec * 1000).toLong(),
                                        (durationSec * 1000).toLong()
                                    )
                                }
                            },
                            "AndroidBridge"
                        )

                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                view?.evaluateJavascript(
                                    """
                                    window.addEventListener('message', function(event) {
                                        try { AndroidBridge.onMessage(JSON.stringify(event.data)); } catch(e) {}
                                    });
                                    """.trimIndent(),
                                    null
                                )
                            }
                        }

                        loadUrl(embedUrl)
                    }
                }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Red)
            }
        }
    }
}
