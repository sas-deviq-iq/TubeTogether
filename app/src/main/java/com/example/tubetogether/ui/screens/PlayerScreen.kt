package com.example.tubetogether.ui.screens

import android.net.Uri
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.tubetogether.api.RetrofitClient
import com.example.tubetogether.api.StreamSourceResponse
import com.example.tubetogether.data.LocalDataManager
import kotlinx.coroutines.launch
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun PlayerScreen(playId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    
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

    var isLoading by remember { mutableStateOf(true) }
    var isControllerVisible by remember { mutableStateOf(true) }
    var skipIncrementMs by remember { mutableStateOf(10000L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var streamSources by remember { mutableStateOf<List<StreamSourceResponse>>(emptyList()) }
    var selectedResolution by remember { mutableStateOf<String?>(null) }
    var initialSeekDone by remember { mutableStateOf(false) }
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var subtitleUrl by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(playId) {
        try {
            val sources = RetrofitClient.api.getStreamSources(playId)
            streamSources = sources
            val bestSource = sources.firstOrNull()
            selectedResolution = bestSource?.resolution
            val rawUrl = bestSource?.videoUrl ?: bestSource?.url
            if (rawUrl != null) {
                videoUrl = com.example.tubetogether.api.ImageProxy.getProxiedUrl(rawUrl)
            }
            
            val videoDetails = RetrofitClient.api.getVideoDetails(playId)
            
            // Save last watched episode for series
            if (!videoDetails.rootSeries.isNullOrEmpty() && videoDetails.rootSeries != "0") {
                LocalDataManager.saveLastWatchedEpisode(videoDetails.rootSeries, playId)
            }

            var arSub: String? = null
            if (videoDetails.translations != null && videoDetails.translations.isJsonArray) {
                val array = videoDetails.translations.asJsonArray
                for (i in 0 until array.size()) {
                    val item = array.get(i).asJsonObject
                    if (item.has("type") && item.get("type").asString == "ar" &&
                        item.has("extention") && item.get("extention").asString == "srt" &&
                        item.has("file")) {
                        arSub = item.get("file").asString
                        break
                    }
                }
            }
            if (arSub == null) {
                arSub = videoDetails.arTranslationFilePath
            }
            
            if (!arSub.isNullOrEmpty() && !arSub.contains("loading.gif")) {
                subtitleUrl = com.example.tubetogether.api.ImageProxy.getProxiedUrl(arSub)
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: e.toString()
            isLoading = false
        }
    }

    val exoPlayer = remember {
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor { chain ->
                val request = chain.request()
                var response = chain.proceed(request)
                var followCount = 0
                while (response.isRedirect && followCount < 20) {
                    val location = response.header("Location")
                    if (location != null) {
                        response.close()
                        // Follow redirect directly without routing through VPS to prevent 4K buffering
                        val newRequest = request.newBuilder()
                            .url(location)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .build()
                        response = chain.proceed(newRequest)
                        followCount++
                    } else {
                        break
                    }
                }
                response
            }
            .build()
            
        val dataSourceFactory = androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(okHttpClient)
        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
            
        // Optimized buffer for 4K playback: Large enough to avoid stuttering, but small enough to start quickly
        val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                32000,   // minBufferMs (32s)
                120000,  // maxBufferMs (2 minutes, keeps memory usage reasonable)
                1500,    // bufferForPlaybackMs (start playing as soon as 1.5s is downloaded)
                3000     // bufferForPlaybackAfterRebufferMs (resume after 3s of buffering)
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
            
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build().apply {
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isLoading = false
                    } else if (playbackState == Player.STATE_BUFFERING) {
                        isLoading = true
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    isLoading = false
                    val cause = error.cause
                    errorMessage = "Message: ${error.message}\nCause: ${cause?.message}\nClass: ${cause?.javaClass?.name}"
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!isPlaying) {
                        val pos = this@apply.currentPosition
                        val duration = this@apply.duration
                        if (pos > 0 && duration > 0) {
                            LocalDataManager.saveWatchProgressV2(playId, pos, duration)
                        }
                    }
                }
            })
            
            trackSelectionParameters = trackSelectionParameters
                .buildUpon()
                .setPreferredTextLanguage("ar")
                .setSelectUndeterminedTextLanguage(true)
                .build()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            val pos = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            if (pos > 0 && duration > 0) {
                LocalDataManager.saveWatchProgressV2(playId, pos, duration)
            }
            exoPlayer.release()
        }
    }

    LaunchedEffect(videoUrl, subtitleUrl) {
        if (videoUrl != null) {
            val position = exoPlayer.currentPosition
            val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(videoUrl))
            
            if (subtitleUrl != null) {
                val isVtt = subtitleUrl!!.contains(".vtt")
                val mime = if (isVtt) androidx.media3.common.MimeTypes.TEXT_VTT else androidx.media3.common.MimeTypes.APPLICATION_SUBRIP
                val subtitle = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleUrl!!))
                    .setMimeType(mime)
                    .setLanguage("ar")
                    .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                    .build()
                mediaItemBuilder.setSubtitleConfigurations(listOf(subtitle))
            }
            
            val mediaItem = mediaItemBuilder.build()
            
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            
            if (!initialSeekDone) {
                val savedPos = LocalDataManager.getWatchProgress(playId)
                if (savedPos > 0) {
                    exoPlayer.seekTo(savedPos)
                }
                initialSeekDone = true
            } else if (position > 0) {
                exoPlayer.seekTo(position)
            }
            
            exoPlayer.playWhenReady = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                androidx.media3.ui.PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    
                    // Hide the default ExoPlayer settings gear
                    val settingsBtn = findViewById<android.view.View>(androidx.media3.ui.R.id.exo_settings)
                    settingsBtn?.visibility = android.view.View.GONE
                    
                    setControllerVisibilityListener(androidx.media3.ui.PlayerView.ControllerVisibilityListener { visibility ->
                        isControllerVisible = (visibility == android.view.View.VISIBLE)
                    })
                }
            },
            update = { view ->
                // Override default skip buttons with our custom increment
                val ffwdBtn = view.findViewById<android.view.View>(androidx.media3.ui.R.id.exo_ffwd)
                ffwdBtn?.setOnClickListener {
                    exoPlayer.seekTo(exoPlayer.currentPosition + skipIncrementMs)
                }
                val rewBtn = view.findViewById<android.view.View>(androidx.media3.ui.R.id.exo_rew)
                rewBtn?.setOnClickListener {
                    exoPlayer.seekTo(maxOf(0, exoPlayer.currentPosition - skipIncrementMs))
                }
            }
        )
        
        if (isLoading && errorMessage == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Red)
            }
        }
        
        if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                val scrollState = rememberScrollState()
                Text(
                    text = "تفاصيل الخطأ:\n$errorMessage\n\nالرابط: $videoUrl",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.verticalScroll(scrollState)
                )
            }
        }

        // Unified Settings Button (Visible only when ExoPlayer controller is visible)
        if (isControllerVisible) {
            var showSettingsSheet by remember { mutableStateOf(false) }
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier.background(Color(0x80000000), shape = androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
            
            if (showSettingsSheet) {
                androidx.compose.material3.ModalBottomSheet(
                    onDismissRequest = { showSettingsSheet = false },
                    containerColor = Color(0xFF1A1A1A)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Resolution Section
                        if (streamSources.isNotEmpty()) {
                            Text("الجودة", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                            androidx.compose.foundation.layout.Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                streamSources.forEach { source ->
                                    val resName = source.resolution ?: "Unknown"
                                    androidx.compose.material3.FilterChip(
                                        selected = (resName == selectedResolution),
                                        onClick = {
                                            selectedResolution = resName
                                            val rawUrl = source.videoUrl ?: source.url
                                            if (rawUrl != null) {
                                                videoUrl = com.example.tubetogether.api.ImageProxy.getProxiedUrl(rawUrl)
                                            }
                                            showSettingsSheet = false
                                        },
                                        label = { Text(resName) },
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                        }
                        
                        // Speed Section
                        Text("سرعة التشغيل", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                        androidx.compose.foundation.layout.Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            val speeds = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
                            speeds.forEach { speed ->
                                androidx.compose.material3.FilterChip(
                                    selected = (exoPlayer.playbackParameters.speed == speed),
                                    onClick = {
                                        exoPlayer.setPlaybackSpeed(speed)
                                        showSettingsSheet = false
                                    },
                                    label = { Text("${speed}x") },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }

                        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                        
                        // Skip Duration Section
                        Text("مدة التخطي", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                        androidx.compose.foundation.layout.Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            val skips = listOf(5000L to "5 ثواني", 10000L to "10 ثواني", 15000L to "15 ثانية")
                            skips.forEach { (ms, label) ->
                                androidx.compose.material3.FilterChip(
                                    selected = (skipIncrementMs == ms),
                                    onClick = {
                                        skipIncrementMs = ms
                                        showSettingsSheet = false
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                        
                        androidx.compose.foundation.layout.Spacer(Modifier.height(32.dp)) // Padding for bottom nav bar area
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val pos = exoPlayer.currentPosition
            if (pos > 0) {
                LocalDataManager.saveWatchProgress(playId, pos)
            }
            exoPlayer.release()
        }
    }
}
