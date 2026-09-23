package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RecordRed
import com.example.ui.viewmodel.GalleryViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.VideoDetailViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val galleryViewModel: GalleryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val videoDetailViewModel: VideoDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val config by settingsViewModel.config.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = config.darkTheme) {
                MainAppNavigation(
                    homeViewModel = homeViewModel,
                    galleryViewModel = galleryViewModel,
                    settingsViewModel = settingsViewModel,
                    videoDetailViewModel = videoDetailViewModel
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val iconFilled: androidx.compose.ui.graphics.vector.ImageVector, val iconOutlined: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Record", Icons.Filled.Videocam, Icons.Outlined.Videocam)
    object Gallery : Screen("gallery", "My Videos", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainAppNavigation(
    homeViewModel: HomeViewModel,
    galleryViewModel: GalleryViewModel,
    settingsViewModel: SettingsViewModel,
    videoDetailViewModel: VideoDetailViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    // Runtime permissions launcher for Audio and Notifications
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    val bottomNavItems = listOf(Screen.Home, Screen.Gallery, Screen.Settings)
    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Gallery.route, Screen.Settings.route)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav")
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.iconFilled else screen.iconOutlined,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = RecordRed.copy(alpha = 0.2f),
                                selectedIconColor = RecordRed,
                                selectedTextColor = RecordRed,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToPlayer = { recordingId ->
                        navController.navigate("player/$recordingId")
                    }
                )
            }

            composable(Screen.Gallery.route) {
                GalleryScreen(
                    viewModel = galleryViewModel,
                    onNavigateToPlayer = { recordingId ->
                        navController.navigate("player/$recordingId")
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "player/{recordingId}",
                arguments = listOf(navArgument("recordingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recordingId = backStackEntry.arguments?.getLong("recordingId") ?: 0L
                VideoPlayerScreen(
                    recordingId = recordingId,
                    viewModel = videoDetailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
