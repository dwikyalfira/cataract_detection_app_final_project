package com.dicoding.cataract_detection_app_final_project

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
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
import com.dicoding.cataract_detection_app_final_project.utils.ImagePicker
import com.dicoding.cataract_detection_app_final_project.utils.ImageStorageManager
import com.dicoding.cataract_detection_app_final_project.view.CNNExplanationView
import com.dicoding.cataract_detection_app_final_project.view.CheckView
import com.dicoding.cataract_detection_app_final_project.view.ForgotPasswordView
import com.dicoding.cataract_detection_app_final_project.view.HistoryResultView
import com.dicoding.cataract_detection_app_final_project.view.HistoryView
import com.dicoding.cataract_detection_app_final_project.view.HomeView
import com.dicoding.cataract_detection_app_final_project.view.LoginView
import com.dicoding.cataract_detection_app_final_project.view.ProfileView
import com.dicoding.cataract_detection_app_final_project.view.RegisterView
import com.dicoding.cataract_detection_app_final_project.view.ResultView
import com.dicoding.cataract_detection_app_final_project.view.ROIView
import com.dicoding.cataract_detection_app_final_project.view.SettingsView
import com.dicoding.cataract_detection_app_final_project.view.SplashView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val userPreferences by lazy { UserPreferences(this) }
    private lateinit var imagePicker: ImagePicker

    @SuppressLint("UseKtx")
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("user_preferences", MODE_PRIVATE)
        
        // Get language from preferences, default to Indonesian
        val lang = prefs.getString("language", UserPreferences.LANG_INDONESIAN) ?: UserPreferences.LANG_INDONESIAN
        
        // Ensure Indonesian is set as default if no language is set
        if (!prefs.contains("language")) {
            prefs.edit { putString("language", UserPreferences.LANG_INDONESIAN) }
        }
        
        android.util.Log.d("MainActivity", "attachBaseContext - Language from preferences: $lang")
        
        val locale = when (lang) {
            UserPreferences.LANG_INDONESIAN -> Locale.forLanguageTag("id")
            UserPreferences.LANG_ENGLISH -> Locale.ENGLISH
            else -> Locale.forLanguageTag("id") // Default to Indonesian
        }
        
        android.util.Log.d("MainActivity", "attachBaseContext - Setting locale to: ${locale.language}")
        
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

        imagePicker = ImagePicker(this)
        
        // Log current locale for debugging
        android.util.Log.d("MainActivity", "onCreate - Current locale: ${Locale.getDefault().language}")
        
        // Initialize UserPreferences to ensure language is set
        lifecycleScope.launch {
            userPreferences.initializeFromSharedPreferences()
            android.util.Log.d("MainActivity", "onCreate - UserPreferences initialized")
            
            // Force apply the current language setting
            val currentLanguage = userPreferences.getLanguageSync()
            android.util.Log.d("MainActivity", "onCreate - Current language from preferences: $currentLanguage")
            
            val locale = when (currentLanguage) {
                UserPreferences.LANG_INDONESIAN -> Locale.forLanguageTag("id")
                UserPreferences.LANG_ENGLISH -> Locale.ENGLISH
                else -> Locale.forLanguageTag("id")
            }
            
            // Force set the application locale
            val localeList = LocaleListCompat.create(locale)
            AppCompatDelegate.setApplicationLocales(localeList)
            android.util.Log.d("MainActivity", "onCreate - Forced locale to: ${locale.language}")
        }

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
            
            // Debug logging
            android.util.Log.d("MainActivity", "Current theme mode: $themeMode, Initial theme: $initialTheme")

            CataractDetectionExpressiveTheme(
                themeMode = themeMode,
                dynamicColor = true
            ) {
                CataractDetectorApp(
                    context = this@MainActivity,
                    userPreferences = userPreferences, 
                    onRecreate = { 
                        android.util.Log.d("MainActivity", "Recreating activity for language change")
                        // Force restart the app to ensure locale changes are applied
                        restartApp()
                    },
                    imagePicker = imagePicker
                )
            }
        }
    }
    
    private fun restartApp() {
        android.util.Log.d("MainActivity", "Restarting app for language change")
        
        // Get the current language preference
        val prefs = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val language = prefs.getString("language", UserPreferences.LANG_INDONESIAN) ?: UserPreferences.LANG_INDONESIAN
        
        android.util.Log.d("MainActivity", "Current language preference: $language")
        
        // Apply the locale immediately
        val locale = when (language) {
            UserPreferences.LANG_INDONESIAN -> Locale.forLanguageTag("id")
            UserPreferences.LANG_ENGLISH -> Locale.ENGLISH
            else -> Locale.forLanguageTag("id")
        }
        
        // Set system locale
        Locale.setDefault(locale)
        
        // Set AppCompatDelegate locale
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        android.util.Log.d("MainActivity", "Locale set to: ${locale.language}")
        
        // Force complete app restart by finishing and starting new intent
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        
        // Alternative: Force kill the process if restart doesn't work
        android.os.Process.killProcess(android.os.Process.myPid())
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

@Composable
fun AnimatedNavigationIcon(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "icon_scale"
    )
    
    val iconColor by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "icon_color"
    )
    
    // Reset pressed state after animation
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
    
    IconButton(
        onClick = {
            isPressed = true
            onClick()
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.scale(scale),
            tint = Color(
                red = MaterialTheme.colorScheme.primary.red * iconColor,
                green = MaterialTheme.colorScheme.primary.green * iconColor,
                blue = MaterialTheme.colorScheme.primary.blue * iconColor,
                alpha = 1f
            )
        )
    }
}

sealed class AuthRoute(val route: String) {
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
    object ForgotPassword : AuthRoute("forgot_password")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CataractDetectorApp(context: Context, userPreferences: UserPreferences, onRecreate: () -> Unit, imagePicker: ImagePicker) {
    val navController = rememberNavController()
    val presenter = remember { MainPresenter() }
    presenter.setNavigationCallback { 
        navController.navigate("result")
    }
    presenter.setROINavigationCallback {
        navController.navigate("roi_adjustment")
    }
    val authPresenter = remember { AuthPresenter(context) }
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    val currentScreen by presenter.currentScreen
    val isAuthenticated by authPresenter.isAuthenticated.collectAsState()
    val authIsLoading by authPresenter.isLoading.collectAsState()
    
    // Screen-specific error and success messages
    val loginErrorMessage by authPresenter.loginErrorMessage.collectAsState()
    val registerErrorMessage by authPresenter.registerErrorMessage.collectAsState()
    val registerSuccessMessage by authPresenter.registerSuccessMessage.collectAsState()
    val forgotPasswordErrorMessage by authPresenter.forgotPasswordErrorMessage.collectAsState()
    val forgotPasswordSuccessMessage by authPresenter.forgotPasswordSuccessMessage.collectAsState()
    
    // Legacy messages (for backward compatibility)
    val authErrorMessage by authPresenter.errorMessage.collectAsState()
    val authSuccessMessage by authPresenter.successMessage.collectAsState()
    val currentUser by authPresenter.currentUser.collectAsState()

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isOnRegistrationScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (currentScreen == Screen.Splash) {
            presenter.startSplashTimer()
        }
    }

    // Show toast on login page when registration is successful
    LaunchedEffect(registerSuccessMessage) {
        registerSuccessMessage?.let {
            // Show toast on login page
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            authPresenter.getUserData(user.uid) { data ->
                userData = data
            }
            // Initialize history repository
            presenter.initializeHistory(context, user.uid)
            
            // Clean up old images (run in background)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val imageStorageManager = ImageStorageManager(context)
                imageStorageManager.cleanupOldImages(30) // Keep images for 30 days
            }
        }
    }

    if (currentScreen == Screen.Splash) {
        SplashView()
    } else if (!isAuthenticated || isOnRegistrationScreen) {
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
                    errorMessage = loginErrorMessage
                )
            }
            composable(AuthRoute.Register.route) {
                LaunchedEffect(Unit) {
                    isOnRegistrationScreen = true
                }
                DisposableEffect(Unit) {
                    onDispose {
                        isOnRegistrationScreen = false
                    }
                }
                RegisterView(
                    onRegisterClick = { name, email, password, confirmPassword ->
                        authPresenter.register(name, email, password, confirmPassword)
                    },
                    onLoginClick = {
                        isOnRegistrationScreen = false
                        navController.navigate(AuthRoute.Login.route)
                    },
                    onClearRegistrationState = {
                        authPresenter.clearRegistrationState()
                    },
                    onClearRegistrationScreenState = {
                        isOnRegistrationScreen = false
                    },
                    isLoading = authIsLoading,
                    errorMessage = registerErrorMessage,
                    successMessage = registerSuccessMessage
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
                    errorMessage = forgotPasswordErrorMessage,
                    successMessage = forgotPasswordSuccessMessage
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
                    "settings" -> stringResource(R.string.settings)
                    "cnn_explanation" -> stringResource(R.string.cnn_title)
                    "history" -> stringResource(R.string.analysis_history)
                    "history_result" -> stringResource(R.string.analysis_result)
                    else -> stringResource(R.string.app_name)
                }

                val showBackButton = currentRoute == "settings" || currentRoute == "cnn_explanation" || 
                                   currentRoute == "history" || currentRoute == "history_result" ||
                                   currentRoute == "result"

                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    title = { 
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        ) 
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = { 
                                when (currentRoute) {
                                    "history" -> navController.navigate(NavigationItem.Profile.route) { 
                                        popUpTo(NavigationItem.Profile.route) { inclusive = false } 
                                    }
                                    "history_result" -> navController.popBackStack()
                                    "result" -> navController.navigate(NavigationItem.Home.route) {
                                        popUpTo("result") { inclusive = true }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                    else -> navController.popBackStack()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: NavigationItem.Home.route
                
                // Hide bottom navigation bar on child pages
                val isChildPage = currentRoute == "settings" || currentRoute == "cnn_explanation" || 
                                currentRoute == "history" || currentRoute == "history_result" || 
                                currentRoute == "result"
                
                if (isChildPage) {
                    // Hide the bottom bar completely
                    Box(modifier = Modifier.height(0.dp))
                } else {
                    NavigationBar {
                        val currentDestination = navBackStackEntry?.destination

                        listOf(
                            NavigationItem.Home,
                            NavigationItem.Check,
                            NavigationItem.Profile
                        ).forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = stringResource(item.titleResId)
                                    )
                                },
                                label = { Text(stringResource(item.titleResId)) },
                                selected = isSelected,
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
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavigationItem.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("settings") {
                    SettingsView(
                        userPreferences = userPreferences,
                        authPresenter = authPresenter,
                        onLanguageChanged = onRecreate,
                        onDeleteAccountClick = { password, callback -> 
                            authPresenter.deleteAccount(password, callback)
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
                composable(NavigationItem.Home.route) {
                    HomeView(
                        onUploadImage = { 
                            imagePicker.pickImageFromGallery { uri ->
                                if (uri != null) {
                                    presenter.onImageSelected(uri.toString())
                                }
                            }
                        },
                        onCaptureImage = { 
                            imagePicker.captureImageFromCamera { uri ->
                                if (uri != null) {
                                    presenter.onImageSelected(uri.toString())
                                }
                            }
                        },
                        onNavigateToInfo = { presenter.navigateTo(Screen.Info) },
                        onNavigateToProfile = { presenter.navigateTo(Screen.Profile) },
                        onNavigateToCNN = { navController.navigate("cnn_explanation") },
                        isLoading = presenter.isLoading.value,
                        scrollBehavior = scrollBehavior
                    )
                }
                composable(NavigationItem.Check.route) {
                    CheckView(
                        onPickImage = { 
                            imagePicker.pickImageFromGallery { uri ->
                                if (uri != null) {
                                    presenter.onImageSelected(uri.toString())
                                }
                            }
                        },
                        onCaptureImage = { 
                            imagePicker.captureImageFromCamera { uri ->
                                if (uri != null) {
                                    presenter.onImageSelected(uri.toString())
                                }
                            }
                        },
                        onProceedWithImage = { presenter.onProceedWithImage() },
                        onRetakeImage = { 
                            presenter.onClearSelectedImage()
                            imagePicker.captureImageFromCamera { uri ->
                                if (uri != null) {
                                    presenter.onImageSelected(uri.toString())
                                }
                            }
                        },
                        onPickDifferentImage = { 
                            presenter.onClearSelectedImage()
                            imagePicker.pickImageFromGallery { uri ->
                                if (uri != null) {
                                    presenter.onImageSelected(uri.toString())
                                }
                            }
                        },
                        onImageCropped = { uri ->
                            presenter.onImageSelected(uri.toString())
                        },
                        selectedImageUri = presenter.selectedImageUri.value,
                        isLoading = presenter.isLoading.value,
                        scrollBehavior = scrollBehavior
                    )
                }
                composable(NavigationItem.Profile.route) {
                    ProfileView(
                        onBackToHome = { navController.navigate(NavigationItem.Home.route) },
                        onLogoutClick = { authPresenter.logout() },
                        onSettingsClick = { navController.navigate("settings") },
                        onHistoryClick = { navController.navigate("history") },
                        scrollBehavior = scrollBehavior,
                        currentUser = currentUser,
                        userData = userData,
                        isLoading = authIsLoading
                    )
                }
                composable("roi_adjustment") {
                    val selectedImageUri = presenter.selectedImageUri.value
                    if (selectedImageUri != null) {
                        ROIView(
                            imageUri = selectedImageUri,
                            onROIConfirmed = { roiRect, adjustments ->
                                presenter.onROIConfirmed(roiRect, adjustments)
                            },
                            onCancel = {
                                navController.popBackStack()
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
                composable("history") {
                    HistoryView(
                        userId = currentUser?.uid ?: "",
                        onViewAnalysis = { history ->
                            presenter.setHistoryForViewing(history)
                            navController.navigate("history_result")
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
                composable("history_result") {
                    val history = presenter.getCurrentHistoryForViewing()
                    HistoryResultView(
                        history = history,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("result") {
                    ResultView(
                        predictionResult = presenter.predictionResult.value,
                        confidenceScore = presenter.confidenceScore.value,
                        scannedImageUri = presenter.scannedImageUri.value,
                        isNavigating = presenter.isNavigating.value,
                        onBackToHome = { 
                            navController.navigate(NavigationItem.Home.route) {
                                popUpTo("result") { inclusive = true }
                                launchSingleTop = true
                                restoreState = false
                            }
                            presenter.onClearAllImagesDelayed()
                        },
                        onTryAnotherImage = {
                            navController.navigate(NavigationItem.Check.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                            presenter.onClearAllImagesDelayed()
                        }
                    )
                }
                composable("cnn_explanation") {
                    CNNExplanationView(
                        scrollBehavior = scrollBehavior
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
