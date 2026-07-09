package com.example.tubetogether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.async
import com.example.tubetogether.api.RetrofitClient
import com.example.tubetogether.api.VideoResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onVideoClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val results by viewModel.results.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val focusManager = LocalFocusManager.current

    val performSearch = {
        viewModel.performSearch()
        focusManager.clearFocus()
    }

    LaunchedEffect(query) {
        if (query.trim().isNotEmpty()) {
            delay(800)
            viewModel.performSearch()
        } else if (query.isEmpty()) {
            // Optional: clear results when query is empty, assuming ViewModel handles this
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Deep Premium Dark Background
    ) {
        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { 
                viewModel.setQuery(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("ابحث عن فيلم أو مسلسل...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE50914), // Netflix Red
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFE50914)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    performSearch()
                }
            )
        )

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filter == "all",
                onClick = { viewModel.setFilter("all") },
                label = { Text("الكل") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE50914),
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = filter == "movies",
                onClick = { viewModel.setFilter("movies") },
                label = { Text("أفلام") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE50914),
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = filter == "series",
                onClick = { viewModel.setFilter("series") },
                label = { Text("مسلسلات") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE50914),
                    selectedLabelColor = Color.White
                )
            )
        }

        // Content
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE50914))
            }
        } else if (hasSearched && results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "لا توجد نتائج لـ \"$query\"",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp), // Extra bottom padding for BottomNav
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = results,
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
