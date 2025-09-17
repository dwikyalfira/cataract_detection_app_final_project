package com.dicoding.cataract_detection_app_final_project

import android.content.Context
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
import com.dicoding.cataract_detection_app_final_project.theme.CataractDetectionExpressiveTheme
import com.dicoding.cataract_detection_app_final_project.view.CheckView
import com.dicoding.cataract_detection_app_final_project.view.ForgotPasswordView
import com.dicoding.cataract_detection_app_final_project.view.HomeView
import com.dicoding.cataract_detection_app_final_project.view.LoginView
import com.dicoding.cataract_detection_app_final_project.view.ProfileView
import com.dicoding.cataract_detection_app_final_project.view.RegisterView
import com.dicoding.cataract_detection_app_final_project.view.SettingsView
import com.dicoding.cataract_detection_app_final_project.view.SplashView
import com.dicoding.cataract_detection_app_final_project.R
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var presenter: MainPresenter
    private val userPreferences by lazy { UserPreferences(this) }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("user_preferences", MODE_PRIVATE)
        // Force Indonesian as default
        prefs.edit().putString("language", UserPreferences.LANG_INDONESIAN).apply()
        val lang = UserPreferences.LANG_INDONESIAN
        
        android.util.Log.d("MainActivity", "Language from preferences: $lang")
        
        val locale = Locale.forLanguageTag("id")
        
        android.util.Log.d("MainActivity", "Setting locale to: ${locale.language}")
        
        // Set system locale
        Locale.setDefault(locale)
        
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        presenter = MainPresenter()

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

            CataractDetectionExpressiveTheme(
                darkTheme = themeMode == UserPreferences.THEME_DARK,
                dynamicColor = true
            ) {
                CataractDetectorApp(userPreferences = userPreferences, onRecreate = { recreate() })
            }
        }
    }
}

sealed class NavigationItem(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    object Home : NavigationItem("home", R.string.home, Icons.Default.Home)
    object Check : NavigationItem("check", R.string.check, Icons.Default.CheckCircle)
    object Profile : NavigationItem("profile", R.string.profile, Icons.Default.Person)
}

sealed class AuthRoute(val route: String) {
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
    object ForgotPassword : AuthRoute("forgot_password")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CataractDetectorApp(userPreferences: UserPreferences, onRecreate: () -> Unit) {
    val navController = rememberNavController()
    val presenter = remember { MainPresenter() }
    val authPresenter = remember { AuthPresenter() }
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    val currentScreen by presenter.currentScreen
    val isAuthenticated by authPresenter.isAuthenticated.collectAsState()
    val authIsLoading by authPresenter.isLoading.collectAsState()
    val authErrorMessage by authPresenter.errorMessage.collectAsState()
    val authSuccessMessage by authPresenter.successMessage.collectAsState()
    val currentUser by authPresenter.currentUser.collectAsState()

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(Unit) {
        if (currentScreen == Screen.Splash) {
            presenter.startSplashTimer()
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            authPresenter.getUserData(user.uid) { data ->
                userData = data
            }
        }
    }

    if (currentScreen == Screen.Splash) {
        SplashView()
    } else if (!isAuthenticated) {
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
                        navController.navigate(AuthRoute.ForgotPassword.route)
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
            composable(AuthRoute.ForgotPassword.route) {
                ForgotPasswordView(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSendResetClick = { email ->
                        authPresenter.resetPassword(email)
                    },
                    isLoading = authIsLoading,
                    errorMessage = authErrorMessage,
                    successMessage = authSuccessMessage
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: NavigationItem.Home.route

                val title = when (currentRoute) {
                    NavigationItem.Home.route -> stringResource(R.string.home)
                    NavigationItem.Check.route -> stringResource(R.string.check_for_cataract)
                    NavigationItem.Profile.route -> stringResource(R.string.my_profile)
                    else -> stringResource(R.string.app_name)
                }

                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    title = { 
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        ) 
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
                            icon = { Icon(item.icon, contentDescription = stringResource(item.titleResId)) },
                            label = { Text(stringResource(item.titleResId)) },
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
                composable("settings") {
                    SettingsView(
                        onBackClick = { navController.popBackStack() },
                        userPreferences = userPreferences,
                        onLanguageChanged = onRecreate
                    )
                }
                composable(NavigationItem.Home.route) {
                    HomeView(
                        onUploadImage = { presenter.onUploadImage() },
                        onCaptureImage = { presenter.onCaptureImage() },
                        onNavigateToInfo = { presenter.navigateTo(Screen.Info) },
                        onNavigateToProfile = { presenter.navigateTo(Screen.Profile) },
                        isLoading = presenter.isLoading.value,
                        scrollBehavior = scrollBehavior
                    )
                }
                composable(NavigationItem.Check.route) {
                    CheckView(
                        onUploadImage = { presenter.onUploadImage() },
                        onCaptureImage = { presenter.onCaptureImage() },
                        isLoading = presenter.isLoading.value,
                        scrollBehavior = scrollBehavior
                    )
                }
                composable(NavigationItem.Profile.route) {
                    val scope = rememberCoroutineScope()
                    
                    ProfileView(
                        onBackToHome = { navController.navigate(NavigationItem.Home.route) },
                        onLogoutClick = { authPresenter.logout() },
                        onSettingsClick = { navController.navigate("settings") },
                        scrollBehavior = scrollBehavior,
                        currentUser = currentUser,
                        userData = userData,
                        isLoading = authIsLoading,
                        onUpdateName = { newName ->
                            scope.launch {
                                authPresenter.updateUserName(newName)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashView() {
    // TODO: Implement splash screen
}