package com.dicoding.cataract_detection_app_final_project.presenter

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.dicoding.cataract_detection_app_final_project.data.AnalysisHistory
import com.dicoding.cataract_detection_app_final_project.model.CataractModel
import com.dicoding.cataract_detection_app_final_project.repository.HistoryRepository
import com.dicoding.cataract_detection_app_final_project.utils.ImageStorageManager
import com.dicoding.cataract_detection_app_final_project.utils.ImageCropper
import com.dicoding.cataract_detection_app_final_project.view.ROIRect
import com.dicoding.cataract_detection_app_final_project.view.ImageAdjustments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main presenter handling UI events and navigation
 */
class MainPresenter {
    
    private var cataractModel: CataractModel? = null
    private var historyRepository: HistoryRepository? = null
    private var imageStorageManager: ImageStorageManager? = null
    private var imageCropper: ImageCropper? = null
    private var currentUserId: String = ""
    private var context: Context? = null
    
    // UI States
    private val _currentScreen = mutableStateOf(Screen.Splash)
    val currentScreen: State<Screen> = _currentScreen
    
    private val _predictionResult = mutableStateOf("")
    val predictionResult: State<String> = _predictionResult
    
    private val _confidenceScore = mutableStateOf(0.0f)
    val confidenceScore: State<Float> = _confidenceScore
    
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
    
    private val _showROIView = mutableStateOf(false)
    val showROIView: State<Boolean> = _showROIView
    
    // Navigation callbacks
    private var onNavigateToResult: (() -> Unit)? = null
    private var onNavigateToROI: (() -> Unit)? = null
    
    fun setNavigationCallback(onNavigateToResult: () -> Unit) {
        this.onNavigateToResult = onNavigateToResult
    }
    
    fun setROINavigationCallback(onNavigateToROI: () -> Unit) {
        this.onNavigateToROI = onNavigateToROI
    }
    
    /**
     * Initialize history repository and set current user
     */
    fun initializeHistory(context: Context, userId: String) {
        this.context = context
        this.historyRepository = HistoryRepository(context)
        this.imageStorageManager = ImageStorageManager(context)
        this.imageCropper = ImageCropper(context)
        this.currentUserId = userId
        this.cataractModel = CataractModel(context)
        android.util.Log.d("MainPresenter", "initializeHistory - Model ready after init: ${cataractModel?.isModelReady()}")
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
     * Handle image selected from gallery or camera (preview mode)
     */
    fun onImageSelected(imageUri: String) {
        _selectedImageUri.value = imageUri
    }
    
    /**
     * Show ROI adjustment view
     */
    fun onAdjustROI() {
        if (_selectedImageUri.value != null) {
            onNavigateToROI?.invoke()
        }
    }
    
    /**
     * Handle ROI confirmation and proceed with cropped image
     */
    fun onROIConfirmed(roiRect: ROIRect, adjustments: ImageAdjustments) {
        _showROIView.value = false
        val imageUri = _selectedImageUri.value
        if (imageUri != null && imageCropper != null) {
            _isLoading.value = true
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    android.util.Log.d("MainPresenter", "Cropping image with ROI: $roiRect, adjustments: $adjustments")
                    val originalUri = Uri.parse(imageUri)
                    val croppedUri = imageCropper!!.cropImage(originalUri, roiRect, adjustments)
                    
                    if (croppedUri != null) {
                        android.util.Log.d("MainPresenter", "Image cropped successfully, proceeding with analysis")
                        // Update the selected image URI to the cropped version
                        _selectedImageUri.value = croppedUri.toString()
                        // Proceed with analysis using the cropped image
                        proceedWithAnalysis(croppedUri.toString())
                    } else {
                        android.util.Log.e("MainPresenter", "Failed to crop image")
                        CoroutineScope(Dispatchers.Main).launch {
                            _isLoading.value = false
                            // Fallback to original image
                            proceedWithAnalysis(imageUri)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainPresenter", "Error cropping image: ${e.message}", e)
                    CoroutineScope(Dispatchers.Main).launch {
                        _isLoading.value = false
                        // Fallback to original image
                        proceedWithAnalysis(imageUri)
                    }
                }
            }
        }
    }
    
    /**
     * Cancel ROI adjustment
     */
    fun onCancelROI() {
        // Navigation back will be handled by the UI
    }
    
    /**
     * Proceed with the selected image for processing
     */
    fun onProceedWithImage() {
        val imageUri = _selectedImageUri.value
        if (imageUri != null) {
            // Navigate to ROI adjustment instead of direct analysis
            onAdjustROI()
        } else {
            android.util.Log.e("MainPresenter", "Cannot proceed - no image selected")
        }
    }
    
    /**
     * Internal method to proceed with analysis
     */
    private fun proceedWithAnalysis(imageUri: String) {
        android.util.Log.d("MainPresenter", "proceedWithAnalysis called with URI: $imageUri")
        android.util.Log.d("MainPresenter", "Model ready: ${cataractModel?.isModelReady()}")
        
        if (cataractModel != null) {
            _isLoading.value = true
            // Store the scanned image URI for display in results
            _scannedImageUri.value = imageUri
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    android.util.Log.d("MainPresenter", "Starting TFLite inference...")
                    // Run inference on background thread
                    val uri = Uri.parse(imageUri)
                    val result = cataractModel!!.predictCataract(uri)
                    val confidence = cataractModel!!.getConfidenceScore()
                    
                    android.util.Log.d("MainPresenter", "Inference complete - Result: $result, Confidence: $confidence")
                    
                    // Copy image to internal storage for persistence
                    val persistentImageUri = try {
                        imageStorageManager?.copyImageToInternalStorage(uri) ?: imageUri
                    } catch (e: Exception) {
                        android.util.Log.e("MainPresenter", "Failed to copy image to internal storage", e)
                        // If we can't copy the image to internal storage, we should not save it to history
                        // as it will become inaccessible later. Instead, we'll show an error.
                        throw Exception("Failed to save image for analysis history: ${e.message}")
                    }
                    
                    // Switch back to main thread for UI updates
                    CoroutineScope(Dispatchers.Main).launch {
                        _predictionResult.value = result
                        _confidenceScore.value = confidence
                        
                        // Save to history if repository is initialized
                        historyRepository?.let { repo ->
                            val history = AnalysisHistory(
                                imageUri = persistentImageUri,
                                predictionResult = result,
                                confidence = confidence,
                                userId = currentUserId
                            )
                            repo.saveAnalysisHistory(history)
                        }
                        
                        _isLoading.value = false
                        onNavigateToResult?.invoke()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainPresenter", "Error during inference", e)
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        _predictionResult.value = "Error"
                        _isLoading.value = false
                        onNavigateToResult?.invoke()
                    }
                }
            }
        } else {
            android.util.Log.e("MainPresenter", "Cannot proceed - model not available")
            _isLoading.value = false
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
        _confidenceScore.value = 0.0f
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
    
    /**
     * Clean up resources when presenter is no longer needed
     */
    fun cleanup() {
        cataractModel?.close()
        cataractModel = null
        historyRepository = null
        imageCropper?.cleanupTempFiles()
        imageCropper = null
        context = null
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
