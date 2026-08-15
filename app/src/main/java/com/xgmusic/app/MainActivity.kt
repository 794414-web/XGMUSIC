package com.xgmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xgmusic.app.ui.screens.HomeScreen
import com.xgmusic.app.ui.screens.LibraryScreen
import com.xgmusic.app.ui.screens.PlayerScreen
import com.xgmusic.app.ui.screens.SearchScreen
import com.xgmusic.app.ui.screens.SettingsScreen
import com.xgmusic.app.ui.theme.XGMUSICTheme
import com.xgmusic.app.ui.components.MiniPlayerBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XGMUSICTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold()
                }
            }
        }
    }
}

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as XGMusicApp

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("search") {
                            popUpTo("home")
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("搜索") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("library") {
                            popUpTo("home")
                        }
                    },
                    icon = { Icon(Icons.Default.MusicLibrary, contentDescription = "Library") },
                    label = { Text("音乐库") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo("home")
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("设置") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    playerManager = app.container.playerManager,
                    onPlayClick = { music ->
                        app.container.playerManager.play(music)
                    }
                )
            }
            composable("search") {
                SearchScreen(
                    playerManager = app.container.playerManager,
                    pluginManager = app.container.pluginManager,
                    onPlayClick = { music ->
                        app.container.playerManager.play(music)
                    }
                )
            }
            composable("library") {
                LibraryScreen(
                    playerManager = app.container.playerManager
                )
            }
            composable("settings") {
                SettingsScreen(
                    pluginManager = app.container.pluginManager,
                    userPreferenceRepository = app.container.userPreferenceRepository
                )
            }
            composable("player") {
                PlayerScreen(
                    playerManager = app.container.playerManager,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Mini Player (always visible except on player screen)
        MiniPlayerBar(
            playerManager = app.container.playerManager,
            onClick = {
                navController.navigate("player")
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}
