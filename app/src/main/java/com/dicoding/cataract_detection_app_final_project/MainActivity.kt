package com.dicoding.cataract_detection_app_final_project

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dicoding.cataract_detection_app_final_project.data.UserPreferences
import com.dicoding.cataract_detection_app_final_project.presenter.AuthPresenter
import com.dicoding.cataract_detection_app_final_project.presenter.MainPresenter
import com.dicoding.cataract_detection_app_final_project.presenter.Screen
import com.dicoding.cataract_detection_app_final_project.ui.theme.Cataract_detection_app_final_projectTheme
import com.dicoding.cataract_detection_app_final_project.view.CheckView
import com.dicoding.cataract_detection_app_final_project.view.HomeView
import com.dicoding.cataract_detection_app_final_project.view.LoginView
import com.dicoding.cataract_detection_app_final_project.view.ProfileView
import com.dicoding.cataract_detection_app_final_project.view.RegisterView
import com.dicoding.cataract_detection_app_final_project.view.SettingsView
import com.dicoding.cataract_detection_app_final_project.view.SplashView
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var presenter: MainPresenter
    private val userPreferences by lazy { UserPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize presenter
        presenter = MainPresenter()

        // Set initial theme mode
        val initialTheme = userPreferences.getThemeModeSync()
        AppCompatDelegate.setDefaultNightMode(
            when (initialTheme) {
                UserPreferences.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                UserPreferences.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(initial = initialTheme)
            val language by userPreferences.language.collectAsState(initial = userPreferences.getLanguageSync())

            // Language changes are handled by the Application class
            // No need to recreate activity here as it's handled in attachBaseContext

            Cataract_detection_app_final_projectTheme(
                themeMode = themeMode
            ) {
                CataractDetectorApp()
            }
        }
    }
}

// Navigation items for bottom navigation
sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavigationItem("home", "Home", Icons.Default.Home)
    object Check : NavigationItem("check", "Check", Icons.Default.CheckCircle)
    object Profile : NavigationItem("profile", "Profile", Icons.Default.Person)
}

// Authentication routes
sealed class AuthRoute(val route: String) {
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CataractDetectorApp() {
    val navController = rememberNavController()
    val presenter = remember { MainPresenter() }
    val authPresenter = remember { AuthPresenter() }
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context = context) }
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    
    // Get the activity for recreation
    val activity = context as? ComponentActivity
    
    // Observe theme preference
    val themeMode by userPreferences.themeMode.collectAsState(initial = UserPreferences.THEME_SYSTEM)
    
    // Observe language preference
    val language by userPreferences.language.collectAsState(initial = UserPreferences.LANG_ENGLISH)
    
    val currentScreen by presenter.currentScreen
    val predictionResult by presenter.predictionResult
    val isLoading by presenter.isLoading
    val isAuthenticated by authPresenter.isAuthenticated.collectAsState()
    val authIsLoading by authPresenter.isLoading.collectAsState()
    val authErrorMessage by authPresenter.errorMessage.collectAsState()
    val currentUser by authPresenter.currentUser.collectAsState()
    
    // User data state
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    
    // Start splash timer when app launches
    LaunchedEffect(Unit) {
        if (currentScreen == Screen.Splash) {
            presenter.startSplashTimer()
        }
    }
    
    // Fetch user data when current user changes
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            authPresenter.getUserData(user.uid) { data ->
                userData = data
            }
        }
    }
    
    // Show splash screen first
    if (currentScreen == Screen.Splash) {
        SplashView()
    } else if (!isAuthenticated) {
        // Show authentication screens
        NavHost(
            navController = navController,
            startDestination = AuthRoute.Login.route
        ) {
            composable(AuthRoute.Login.route) {
                LoginView(
                    onLoginClick = { email, password ->
                        authPresenter.login(email, password)
                    },
                    onRegisterClick = {
                        navController.navigate(AuthRoute.Register.route)
                    },
                    onForgotPasswordClick = {
                        // TODO: Implement forgot password
                    },
                    isLoading = authIsLoading,
                    errorMessage = authErrorMessage
                )
            }
            composable(AuthRoute.Register.route) {
                RegisterView(
                    onRegisterClick = { name, email, password, confirmPassword ->
                        authPresenter.register(name, email, password, confirmPassword)
                    },
                    onLoginClick = {
                        navController.navigate(AuthRoute.Login.route)
                    },
                    isLoading = authIsLoading,
                    errorMessage = authErrorMessage
                )
            }
        }
    } else {
        // Main app with top and bottom navigation
        Scaffold(
            topBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: NavigationItem.Home.route
                
                // Map routes to their corresponding titles
                val title = when (currentRoute) {
                    NavigationItem.Home.route -> "Home"
                    NavigationItem.Check.route -> "Check for Cataract"
                    NavigationItem.Profile.route -> "My Profile"
                    else -> "Cataract Detector"
                }
                
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title)
                    },
                    actions = {
                        // Logout button moved to profile page
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    listOf(
                        NavigationItem.Home,
                        NavigationItem.Check,
                        NavigationItem.Profile
                    ).forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavigationItem.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Settings Screen
                composable("settings") {
                    SettingsView(
                        onBackClick = { navController.popBackStack() },
                        userPreferences = userPreferences,
                        onLanguageChanged = { 
                            // Recreate activity to apply language changes
                            activity?.recreate()
                        }
                    )
                }
                
                // Main Navigation
                composable(NavigationItem.Home.route) {
                    HomeView(
                        onUploadImage = { presenter.onUploadImage() },
                        onCaptureImage = { presenter.onCaptureImage() },
                        onNavigateToInfo = { presenter.navigateTo(Screen.Info) },
                        onNavigateToProfile = { presenter.navigateTo(Screen.Profile) },
                        isLoading = isLoading,
                        scrollBehavior = scrollBehavior
                    )
                }
                composable(NavigationItem.Check.route) {
                    CheckView(
                        onUploadImage = { presenter.onUploadImage() },
                        onCaptureImage = { presenter.onCaptureImage() },
                        isLoading = isLoading,
                        scrollBehavior = scrollBehavior
                    )
                }
                composable(NavigationItem.Profile.route) {
                    ProfileView(
                        onBackToHome = { navController.navigate(NavigationItem.Home.route) },
                        onLogoutClick = { authPresenter.logout() },
                        onSettingsClick = { navController.navigate("settings") },
                        scrollBehavior = scrollBehavior,
                        currentUser = currentUser,
                        userData = userData,
                        isLoading = authIsLoading
                    )
                }
            }
        }
    }
}

@Composable
fun SplashView() {
    TODO("Not yet implemented")
}