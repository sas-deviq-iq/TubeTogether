package com.example.tubetogether.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.tubetogether.api.ImageProxy
import com.example.tubetogether.data.LocalDataManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onVideoClick: (String) -> Unit) {
    var favorites by remember { mutableStateOf(LocalDataManager.getFavorites()) }

    // Re-fetch when the screen resumes
    LaunchedEffect(Unit) {
        favorites = LocalDataManager.getFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المفضلة", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141414),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF141414)
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد أفلام أو مسلسلات في المفضلة", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                items(favorites) { video ->
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .aspectRatio(0.7f)
                            .clickable {
                                val id = video.id ?: video.videoId ?: video.nb
                                if (id != null) onVideoClick(id)
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageProxy.getProxiedUrl(video.posterMedium ?: video.posterLarge),
                                contentDescription = video.title ?: video.arTitle ?: video.enTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
