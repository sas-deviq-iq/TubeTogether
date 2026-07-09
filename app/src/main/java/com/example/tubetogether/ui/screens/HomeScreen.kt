package com.example.tubetogether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tubetogether.api.RetrofitClient
import com.example.tubetogether.api.VideoGroup
import com.example.tubetogether.api.VideoResponse
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onVideoClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    // Removed local state and LaunchedEffect. ViewModel handles it.

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0A0A) // Deep Premium Dark Background
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE50914)) // Netflix Red
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Premium Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = "TubeTogether",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color(0xFFE50914), // Netflix Red
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "شاهد بلا حدود",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                items(
                    items = groups,
                    key = { group -> group.title ?: group.hashCode() },
                    contentType = { "group_row" }
                ) { group ->
                    if (!group.content.isNullOrEmpty()) {
                        val onSeeAll = remember(group, onVideoClick) {
                            {
                                com.example.tubetogether.api.DataStore.currentGroup = group
                                onVideoClick("SEE_ALL_CATEGORY")
                            }
                        }
                        SectionTitle(
                            title = group.title ?: "",
                            onSeeAllClick = onSeeAll
                        )
                        LazyRow(
                            modifier = Modifier.height(230.dp), // FIXED HEIGHT prevents synchronous measure lag in LazyColumn!
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = group.content!!.take(15), // MASSIVE PERFORMANCE BOOST: Limit preview to 15 items!
                                key = { video -> video.id ?: video.nb ?: video.hashCode() },
                                contentType = { "video_card" }
                            ) { video ->
                                val onCardClick = remember(video, onVideoClick) {
                                    {
                                        val videoId = video.id ?: video.nb ?: ""
                                        if (videoId.isNotEmpty()) {
                                            onVideoClick(videoId)
                                        }
                                    }
                                }
                                VideoCard(video = video, onClick = onCardClick)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "عرض الكل",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE50914),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier
                .clickable { onSeeAllClick() }
                .padding(start = 8.dp)
        )
    }
}

@Composable
fun VideoCard(video: VideoResponse, onClick: () -> Unit) {
    val context = LocalContext.current
    val videoId = video.id ?: video.videoId ?: video.nb ?: ""
    val imgUrl = com.example.tubetogether.api.ImageProxy.getProxiedUrl(video.posterMedium)
    val progress = remember(videoId) { com.example.tubetogether.data.LocalDataManager.getWatchProgressV2(videoId) }

    val model = remember(imgUrl) {
        ImageRequest.Builder(context)
            .data(imgUrl)
            .size(300) // Lower resolution for extreme performance
            .memoryCacheKey(imgUrl) // Explicit caching
            .diskCacheKey(imgUrl)
            .build()
    }

    Column(
        modifier = Modifier
            .width(115.dp) // Optimized width for perfect screen fit
            .height(230.dp) // FIXED HEIGHT guarantees instant layout!
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(12.dp)
                }
        ) {
            AsyncImage(
                model = model,
                contentDescription = video.arTitle ?: video.enTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A))
            )
            
            if (progress != null && progress.durationMs > 0 && progress.positionMs > 0) {
                val percentage = (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = percentage,
                    color = Color(0xFFE50914), // Netflix Red
                    trackColor = Color.DarkGray.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = video.arTitle ?: video.enTitle ?: video.title ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE0E0E0),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // Prevents text overlapping
        )
        Text(
            text = video.year ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            maxLines = 1
        )
    }
}
