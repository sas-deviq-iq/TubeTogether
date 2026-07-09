package com.example.tubetogether.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tubetogether.api.DataStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(onVideoClick: (String) -> Unit, onBack: () -> Unit) {
    val group = DataStore.currentGroup

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0A0A)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = group?.title ?: "القائمة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF141414)
                )
            )

            if (group?.content.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد بيانات", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val uniqueVideos = group!!.content!!.distinctBy { it.id ?: it.nb }
                    items(
                        items = uniqueVideos,
                        key = { video -> video.id ?: video.nb ?: video.hashCode() },
                        contentType = { "video_card" }
                    ) { video ->
                        VideoCard(video = video) {
                            val videoId = video.id ?: video.nb ?: ""
                            if (videoId.isNotEmpty()) {
                                onVideoClick(videoId)
                            }
                        }
                    }
                }
            }
        }
    }
}
