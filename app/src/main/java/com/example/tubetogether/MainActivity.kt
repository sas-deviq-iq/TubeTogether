package com.example.tubetogether

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.tubetogether.updater.UpdateManager
import com.example.tubetogether.updater.UpdateInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tubetogether.ui.screens.CategoryListScreen
import com.example.tubetogether.ui.screens.CategoryListScreen
import com.example.tubetogether.ui.screens.DetailsScreen
import com.example.tubetogether.ui.screens.FavoritesScreen
import com.example.tubetogether.ui.screens.HomeScreen
import com.example.tubetogether.ui.screens.PlayerScreen
import com.example.tubetogether.ui.screens.SearchScreen
import com.example.tubetogether.ui.theme.TubetogetherTheme
import com.example.tubetogether.data.LocalDataManager
import java.net.URLDecoder
import java.net.URLEncoder
import coil.Coil
import coil.ImageLoader
import okhttp3.OkHttpClient

@androidx.compose.material3.ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalDataManager.init(applicationContext)
        
        // Setup Coil to hit CDNs directly to prevent slow image loading
        val imageLoader = ImageLoader.Builder(applicationContext)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val originalUrl = chain.request().url.toString()
                        // Route all images through our dynamic proxy to bypass ISP blocking
                        val proxiedUrl = com.example.tubetogether.api.ImageProxy.getProxiedUrl(originalUrl)
                        val request = chain.request().newBuilder()
                            .url(proxiedUrl)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .header("Origin", com.example.tubetogether.utils.CryptoUtil.decrypt("RRd4PV5ZI2JOCmIoQAJiLAMQZCxPAmcsWRoiLkIO"))
                            .header("Referer", com.example.tubetogether.utils.CryptoUtil.decrypt("RRd4PV5ZI2JOCmIoQAJiLAMQZCxPAmcsWRoiLkIOIw=="))
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .build()
        Coil.setImageLoader(imageLoader)

        setContent {
            TubetogetherTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                
                LaunchedEffect(Unit) {
                    val info = UpdateManager.checkForUpdates(context)
                    if (info != null) {
                        updateInfo = info
                    }
                }
                
                if (updateInfo != null) {
                    AlertDialog(
                        onDismissRequest = { /* Force update or allow dismiss? Let's allow dismiss */ updateInfo = null },
                        title = { Text("تحديث جديد متاح!") },
                        text = { Text("إصدار جديد متوفر الآن (v${updateInfo!!.version}).\n\nملاحظات الإصدار:\n${updateInfo!!.releaseNotes}") },
                        confirmButton = {
                            TextButton(onClick = {
                                UpdateManager.downloadAndInstall(context, updateInfo!!.downloadUrl)
                                updateInfo = null
                            }) {
                                Text("تحديث الآن")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateInfo = null }) {
                                Text("لاحقاً")
                            }
                        }
                    )
                }
                
                // Track current route to hide/show Bottom Nav
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route
                
                val showBottomNav = currentRoute == "home" || currentRoute == "search" || currentRoute == "favorites"

                Scaffold(
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar(
                                containerColor = Color(0xFF141414), // Netflix-like bottom nav color
                                contentColor = Color.White
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("الرئيسية") },
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = Color(0xFFE50914) // Netflix Red
                                    )
                                )

                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    label = { Text("البحث") },
                                    selected = currentRoute == "search",
                                    onClick = {
                                        navController.navigate("search") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = Color(0xFFE50914)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                                    label = { Text("المفضلة") },
                                    selected = currentRoute == "favorites",
                                    onClick = {
                                        navController.navigate("favorites") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = Color(0xFFE50914)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Main content surface
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(onVideoClick = { videoId ->
                                    if (videoId == "SEE_ALL_CATEGORY") {
                                        navController.navigate("categoryList")
                                    } else {
                                        navController.navigate("details/$videoId")
                                    }
                                })
                            }
                            composable("search") {
                                SearchScreen(onVideoClick = { videoId ->
                                    navController.navigate("details/$videoId")
                                })
                            }

                            composable("favorites") {
                                FavoritesScreen(onVideoClick = { videoId ->
                                    navController.navigate("details/$videoId")
                                })
                            }
                            composable("categoryList") {
                                CategoryListScreen(
                                    onVideoClick = { videoId ->
                                        navController.navigate("details/$videoId")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("details/{videoId}") { backStackEntry ->
                                val videoId = backStackEntry.arguments?.getString("videoId") ?: return@composable
                                DetailsScreen(
                                    videoId = videoId,
                                    onPlayVideo = { playId ->
                                        navController.navigate("player/$playId")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("player/{playId}") { backStackEntry ->
                                val playId = backStackEntry.arguments?.getString("playId") ?: return@composable
                                PlayerScreen(
                                    playId = playId,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}