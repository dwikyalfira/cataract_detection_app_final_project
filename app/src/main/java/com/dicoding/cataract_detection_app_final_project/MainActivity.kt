package com.dicoding.cataract_detection_app_final_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dicoding.cataract_detection_app_final_project.presenter.AuthPresenter
import com.dicoding.cataract_detection_app_final_project.presenter.MainPresenter
import com.dicoding.cataract_detection_app_final_project.presenter.Screen
import com.dicoding.cataract_detection_app_final_project.ui.theme.Cataract_detection_app_final_projectTheme
import com.dicoding.cataract_detection_app_final_project.view.CheckView
import com.dicoding.cataract_detection_app_final_project.view.HomeView
import com.dicoding.cataract_detection_app_final_project.view.LoginView
import com.dicoding.cataract_detection_app_final_project.view.ProfileView
import com.dicoding.cataract_detection_app_final_project.view.RegisterView
import com.dicoding.cataract_detection_app_final_project.view.SplashView


class MainActivity : ComponentActivity() {
    private lateinit var presenter: MainPresenter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize presenter
        presenter = MainPresenter()
        
        setContent {
            Cataract_detection_app_final_projectTheme {
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
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    
    val currentScreen by presenter.currentScreen
    val predictionResult by presenter.predictionResult
    val isLoading by presenter.isLoading
    val isAuthenticated by authPresenter.isAuthenticated.collectAsState()
    val authIsLoading by authPresenter.isLoading.collectAsState()
    val authErrorMessage by authPresenter.errorMessage.collectAsState()
    
    // Start splash timer when app launches
    LaunchedEffect(Unit) {
        if (currentScreen == Screen.Splash) {
            presenter.startSplashTimer()
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
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text("Cataract Detector")
                    },
                    actions = {
                        IconButton(onClick = { authPresenter.logout() }) {
                            Icon(imageVector = Icons.Filled.Logout, contentDescription = "Sign out")
                        }
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
                        scrollBehavior = scrollBehavior
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