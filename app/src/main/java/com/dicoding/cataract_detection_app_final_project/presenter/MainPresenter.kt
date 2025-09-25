package com.dicoding.cataract_detection_app_final_project.presenter

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.dicoding.cataract_detection_app_final_project.data.AnalysisHistory
import com.dicoding.cataract_detection_app_final_project.model.CataractModel
import com.dicoding.cataract_detection_app_final_project.repository.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main presenter handling UI events and navigation
 */
class MainPresenter {
    
    private val cataractModel = CataractModel()
    private var historyRepository: HistoryRepository? = null
    private var currentUserId: String = ""
    
    // UI States
    private val _currentScreen = mutableStateOf(Screen.Splash)
    val currentScreen: State<Screen> = _currentScreen
    
    private val _predictionResult = mutableStateOf("")
    val predictionResult: State<String> = _predictionResult
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _selectedImageUri = mutableStateOf<String?>(null)
    val selectedImageUri: State<String?> = _selectedImageUri
    
    private val _scannedImageUri = mutableStateOf<String?>(null)
    val scannedImageUri: State<String?> = _scannedImageUri
    
    private val _isFromHistory = mutableStateOf(false)
    val isFromHistory: State<Boolean> = _isFromHistory
    
    private val _currentHistoryForViewing = mutableStateOf<AnalysisHistory?>(null)
    
    private val _isNavigating = mutableStateOf(false)
    val isNavigating: State<Boolean> = _isNavigating
    
    // Navigation callback
    private var onNavigateToResult: (() -> Unit)? = null
    
    fun setNavigationCallback(onNavigateToResult: () -> Unit) {
        this.onNavigateToResult = onNavigateToResult
    }
    
    /**
     * Initialize history repository and set current user
     */
    fun initializeHistory(context: Context, userId: String) {
        historyRepository = HistoryRepository(context)
        currentUserId = userId
    }
    
    /**
     * Set analysis from history for viewing
     */
    fun setAnalysisFromHistory(history: AnalysisHistory) {
        _predictionResult.value = history.predictionResult
        _scannedImageUri.value = history.imageUri
        _isFromHistory.value = true
    }
    
    /**
     * Set history for viewing in dedicated history result view
     */
    fun setHistoryForViewing(history: AnalysisHistory) {
        _currentHistoryForViewing.value = history
    }
    
    /**
     * Get current history for viewing
     */
    fun getCurrentHistoryForViewing(): AnalysisHistory? {
        return _currentHistoryForViewing.value
    }
    
    /**
     * Navigate to a specific screen
     */
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
    
    /**
     * Handle image pick (dummy implementation)
     */
    fun onPickImage() {
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
     * Handle image selected from gallery or camera (preview mode)
     */
    fun onImageSelected(imageUri: String) {
        _selectedImageUri.value = imageUri
    }
    
    /**
     * Proceed with the selected image for processing
     */
    fun onProceedWithImage() {
        val imageUri = _selectedImageUri.value
        if (imageUri != null) {
            _isLoading.value = true
            // Store the scanned image URI for display in results
            _scannedImageUri.value = imageUri
            // Simulate processing delay
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000) // 2 second delay
                val result = cataractModel.predictCataract(imageUri)
                _predictionResult.value = result
                
                // Save to history if repository is initialized
                historyRepository?.let { repo ->
                    val history = AnalysisHistory(
                        imageUri = imageUri,
                        predictionResult = result,
                        confidence = 0.85f, // Dummy confidence value
                        userId = currentUserId
                    )
                    repo.saveAnalysisHistory(history)
                }
                
                _isLoading.value = false
                onNavigateToResult?.invoke()
            }
        }
    }
    
    /**
     * Clear selected image (for retake or upload different)
     */
    fun onClearSelectedImage() {
        _selectedImageUri.value = null
    }
    
    /**
     * Clear all images and reset to initial state
     */
    fun onClearAllImages() {
        _selectedImageUri.value = null
        _scannedImageUri.value = null
        _predictionResult.value = ""
        _isLoading.value = false
        _isFromHistory.value = false
        _currentHistoryForViewing.value = null
        _isNavigating.value = false
    }
    
    /**
     * Clear all images with a delay to allow navigation to complete
     */
    fun onClearAllImagesDelayed() {
        _isNavigating.value = true
        CoroutineScope(Dispatchers.Main).launch {
            delay(100) // Small delay to allow navigation to complete
            onClearAllImages()
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
