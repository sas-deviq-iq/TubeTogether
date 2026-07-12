package com.example.tubetogether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tubetogether.api.RetrofitClient
import com.example.tubetogether.api.VideoResponse
import com.example.tubetogether.data.LocalDataManager
import kotlinx.coroutines.launch

@Composable
fun DetailsScreen(videoId: String, onPlayVideo: (String) -> Unit, onBack: () -> Unit) {
    var details by remember { mutableStateOf<VideoResponse?>(null) }
    var episodes by remember { mutableStateOf<List<VideoResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // For series
    var selectedSeason by remember { mutableStateOf("1") }
    var isFavorite by remember { mutableStateOf(LocalDataManager.isFavorite(videoId)) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(videoId) {
        coroutineScope.launch {
            try {
                val videoDetails = RetrofitClient.api.getVideoDetails(videoId)
                details = videoDetails

                if (videoDetails.kind == "2") {
                    // It's a series
                    episodes = RetrofitClient.api.getSeriesEpisodes(videoId)
                    val minSeason = episodes.mapNotNull { it.season?.toIntOrNull() }.minOrNull()?.toString()
                    selectedSeason = minSeason ?: "1"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.localizedMessage ?: e.toString()
            } finally {
                isLoading = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(text = "خطأ في تحميل التفاصيل:\n$errorMessage", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            details?.let { video ->
                val imgUrl = com.example.tubetogether.api.ImageProxy.getProxiedUrl(video.posterLarge ?: video.posterMedium)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Poster",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Overlay could be added here
                        
                        if (video.kind != "2") {
                            val progress = remember(videoId) { LocalDataManager.getWatchProgressV2(videoId) }
                            if (progress != null && progress.durationMs > 0 && progress.positionMs > 0) {
                                val percentage = (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = percentage,
                                    color = Color(0xFFE50914), // Netflix Red
                                    trackColor = Color.DarkGray.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .height(6.dp) // Slightly thicker for the big poster
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = video.arTitle ?: video.enTitle ?: video.title ?: "",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                if (isFavorite) {
                                    LocalDataManager.removeFavorite(videoId)
                                    isFavorite = false
                                } else {
                                    LocalDataManager.addFavorite(video)
                                    isFavorite = true
                                }
                            }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color(0xFFE50914) else Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "السنة: ${video.year ?: "غير معروف"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating & Categories
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!video.stars.isNullOrEmpty() && video.stars != "0") {
                                Text(
                                    text = "⭐ ${video.stars}",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            
                            val catString = video.categories?.mapNotNull { it.arTitle ?: it.enTitle }?.joinToString(" • ")
                            if (!catString.isNullOrEmpty()) {
                                Text(
                                    text = catString,
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Synopsis
                        val synopsis = video.arContent ?: video.enContent
                        if (!synopsis.isNullOrEmpty()) {
                            Text(
                                text = "القصة",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = synopsis,
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        if (video.kind == "2") {
                            // Series UI
                            val seasons = episodes.mapNotNull { it.season }.distinct().sortedBy { it.toIntOrNull() ?: 0 }
                            
                            if (seasons.isNotEmpty()) {
                                ScrollableTabRow(
                                    selectedTabIndex = seasons.indexOf(selectedSeason).takeIf { it >= 0 } ?: 0,
                                    containerColor = Color.Transparent,
                                    edgePadding = 0.dp,
                                    divider = {}
                                ) {
                                    seasons.forEach { season ->
                                        Tab(
                                            selected = season == selectedSeason,
                                            onClick = { selectedSeason = season },
                                            text = { Text("موسم $season", fontWeight = if (season == selectedSeason) FontWeight.Bold else FontWeight.Normal) },
                                            selectedContentColor = Color.White,
                                            unselectedContentColor = Color.Gray
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))

                        Spacer(modifier = Modifier.height(16.dp))
                                
                                val currentEpisodes = episodes.filter { it.season == selectedSeason }.sortedBy { it.episodeNumber?.toIntOrNull() ?: 0 }
                                
                                currentEpisodes.forEach { episode ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                val epId = episode.nb ?: return@clickable
                                                onPlayVideo(epId)
                                            },
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(120.dp, 68.dp).background(Color.DarkGray)) {
                                                val epImgUrl = com.example.tubetogether.api.ImageProxy.getProxiedUrl(episode.posterMedium ?: episode.posterLarge)
                                                if (!epImgUrl.isNullOrEmpty()) {
                                                    AsyncImage(
                                                        model = epImgUrl,
                                                        contentDescription = "Episode Thumbnail",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }

                                                val progress = remember(episode.nb) {
                                                    episode.nb?.let { LocalDataManager.getWatchProgressV2(it) } 
                                                }
                                                if (progress != null && progress.durationMs > 0 && progress.positionMs > 0) {
                                                    val percentage = (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
                                                    LinearProgressIndicator(
                                                        progress = percentage,
                                                        color = Color(0xFFE50914), // Netflix Red
                                                        trackColor = Color.DarkGray.copy(alpha = 0.5f),
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .fillMaxWidth()
                                                            .height(3.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "الحلقة ${episode.episodeNumber ?: ""}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = episode.arTitle ?: episode.enTitle ?: episode.title ?: "",
                                                    color = Color.LightGray,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Movie UI
                            val savedProgress = LocalDataManager.getWatchProgress(videoId)
                            val hasProgress = savedProgress > 0L

                            Button(
                                onClick = { onPlayVideo(videoId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasProgress) Color(0xFFE50914) else Color.White,
                                    contentColor = if (hasProgress) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("▶", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (hasProgress) "إكمال المشاهدة" else "تشغيل", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Cast
                        if (!video.actorsInfo.isNullOrEmpty()) {
                            Text(
                                text = "الممثلين",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(video.actorsInfo) { actor ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(80.dp)
                                    ) {
                                        Box(modifier = Modifier.size(70.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.DarkGray)) {
                                            val actorImg = com.example.tubetogether.api.ImageProxy.getProxiedUrl(actor.staffImg)
                                            if (!actorImg.isNullOrEmpty() && !actorImg.contains("not_available")) {
                                                AsyncImage(
                                                    model = actorImg,
                                                    contentDescription = actor.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.align(Alignment.Center).size(40.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = actor.name ?: "",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}