package com.dicoding.cataract_detection_app_final_project.presenter

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.dicoding.cataract_detection_app_final_project.model.CataractModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main presenter handling UI events and navigation
 */
class MainPresenter {
    
    private val cataractModel = CataractModel()
    
    // UI States
    private val _currentScreen = mutableStateOf(Screen.Splash)
    val currentScreen: State<Screen> = _currentScreen
    
    private val _predictionResult = mutableStateOf("")
    val predictionResult: State<String> = _predictionResult
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    /**
     * Navigate to a specific screen
     */
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
    
    /**
     * Handle image upload (dummy implementation)
     */
    fun onUploadImage() {
        _isLoading.value = true
        // Simulate processing delay
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // 2 second delay
            _predictionResult.value = cataractModel.predictCataract("dummy_path")
            _isLoading.value = false
            navigateTo(Screen.Result)
        }
    }
    
    /**
     * Handle image capture (dummy implementation)
     */
    fun onCaptureImage() {
        _isLoading.value = true
        // Simulate processing delay
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // 2 second delay
            _predictionResult.value = cataractModel.predictCataract("dummy_path")
            _isLoading.value = false
            navigateTo(Screen.Result)
        }
    }
    
    /**
     * Handle back navigation
     */
    fun onBackPressed() {
        when (_currentScreen.value) {
            Screen.Result, Screen.Info, Screen.Profile -> navigateTo(Screen.Home)
            Screen.Home -> {
                // Exit app or show confirmation dialog
            }
            else -> {}
        }
    }
    
    /**
     * Start splash screen timer
     */
    fun startSplashTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(2500) // 2.5 seconds
            navigateTo(Screen.Home)
        }
    }
}

/**
 * Screen enumeration for navigation
 */
enum class Screen {
    Splash,
    Home,
    Result,
    Info,
    Profile
}
