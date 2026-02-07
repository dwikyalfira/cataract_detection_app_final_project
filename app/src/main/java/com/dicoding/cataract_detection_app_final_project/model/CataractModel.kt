package com.dicoding.cataract_detection_app_final_project.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Data class to hold image processing details for breakdown display
 */
data class ImageProcessingDetails(
    val rawOutput: Float = 0f,
    val meanBrightness: Float = 0f,
    val variance: Float = 0f,
    val edgeDensity: Float = 0f,
    val isValidImage: Boolean = true
)

/**
 * TensorFlow Lite model class for CNN cataract detection
 */
class CataractModel(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private var modelLoaded = false
    private var lastConfidence: Float = 0.0f
    private var lastProcessingDetails: ImageProcessingDetails = ImageProcessingDetails()
    
    // Model input/output specifications
    private val INPUT_SIZE = 224
    private val PIXEL_SIZE = 3
    private val IMAGE_MEAN = 127.5f
    private val IMAGE_STD = 127.5f
    
    // Confidence threshold for "Unknown" detection
    private val CONFIDENCE_THRESHOLD = 0.75f
    
    companion object {
        private const val TAG = "CataractModel"
        private const val MODEL_FILENAME = "cataract_model_90percent.tflite"
    }
    
    init {
        try {
            val modelFiles = context.assets.list("")
            if (modelFiles?.contains(MODEL_FILENAME) == true) {
                Log.d(TAG, "Model file found in assets")
                loadModel()
            } else {
                Log.e(TAG, "Model file not found in assets")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking assets: ${e.message}", e)
            modelLoaded = false
        }
    }
    
    /**
     * Load the TensorFlow Lite model from assets
     */
    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile(MODEL_FILENAME)
            
            if (modelBuffer.capacity() == 0) {
                throw Exception("Model buffer is empty")
            }
            
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            
            interpreter = Interpreter(modelBuffer, options)
            modelLoaded = true
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model: ${e.message}", e)
            modelLoaded = false
        }
    }
    
    /**
     * Load model file from assets
     */
    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    /**
     * Predict cataract from image URI
     * @param imageUri Uri of the image file
     * @return Prediction result ("Normal", "Cataract", or "Unknown")
     */
    fun predictCataract(imageUri: Uri): String {
        if (!modelLoaded || interpreter == null) {
            Log.w(TAG, "Model not loaded, attempting to reinitialize")
            loadModel()

            if (!modelLoaded || interpreter == null) {
                Log.e(TAG, "Model still unavailable after reinitialization")
                return "Error: Model failed to load"
            }
        }
        
        try {
            // Load and preprocess image
            val bitmap = loadImageFromUri(imageUri)
            
            // Check image validity (bad lighting, solid colors, etc)
            if (!validateImage(bitmap)) {
                Log.d(TAG, "Image validation failed")
                lastConfidence = 0.0f
                return "Unknown"
            }
            
            val inputBuffer = preprocessImage(bitmap)
            
            // Prepare output array (Sigmoid activation for binary classification)
            val outputArray = Array(1) { FloatArray(1) }
            
            // Run inference
            interpreter?.run(inputBuffer, outputArray)
            
            // Process results
            val probability = outputArray[0][0]
            Log.d(TAG, "Raw probability: $probability")
            
            // Update processing details with raw output
            lastProcessingDetails = lastProcessingDetails.copy(rawOutput = probability)
            
            // Calculate confidence
            lastConfidence = if (probability > 0.5f) probability else 1.0f - probability
            
            // Return prediction based on threshold
            val result = when {
                lastConfidence < CONFIDENCE_THRESHOLD -> "Unknown"
                probability > 0.5f -> "Normal"
                else -> "Cataract"
            }
            
            Log.d(TAG, "Prediction: $result, Confidence: $lastConfidence")
            return result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during prediction: ${e.message}", e)
            return "Error: ${e.message}"
        }
    }
    
    /**
     * Validate image for basic quality checks (brightness, variance)
     * Returns true if image seems valid, false if likely bad/non-eye
     */
    private fun validateImage(bitmap: Bitmap): Boolean {
        try {
            // Downsample for speed
            val smallBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
            val width = smallBitmap.width
            val height = smallBitmap.height
            val pixels = IntArray(width * height)
            smallBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            
            var sumBrightness = 0L
            var sumSqBrightness = 0L
            
            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                
                // Perceived brightness formula
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toLong()
                
                sumBrightness += brightness
                sumSqBrightness += brightness * brightness
            }
            
            val numPixels = (width * height).toLong()
            val meanBrightness = sumBrightness / numPixels
            
            // Calculate Variance: E[X^2] - (E[X])^2
            val meanSqBrightness = sumSqBrightness / numPixels
            val variance = meanSqBrightness - (meanBrightness * meanBrightness)
            
            // Calculate edge density
            val edgeDensity = calculateEdgeDensity(pixels, width, height)
            
            // Store processing details for breakdown display
            val isValid = meanBrightness in 20..235 && variance >= 100
            lastProcessingDetails = ImageProcessingDetails(
                rawOutput = 0f, // Will be updated after inference
                meanBrightness = meanBrightness.toFloat(),
                variance = variance.toFloat(),
                edgeDensity = edgeDensity,
                isValidImage = isValid
            )
            
            // Check if too dark or too bright (range 0-255)
            if (meanBrightness < 20 || meanBrightness > 235) {
                Log.d(TAG, "Image rejected: Too dark or too bright")
                return false
            }
            
            // Check variance (solid colors will have variance ~0)
            if (variance < 100) {
                Log.d(TAG, "Image rejected: Low variance")
                return false
            }
            
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating image: ${e.message}")
            return true // Fail safe: assume valid if check fails
        }
    }
    
    /**
     * Calculate edge density using simple gradient
     */
    private fun calculateEdgeDensity(pixels: IntArray, width: Int, height: Int): Float {
        var edgeSum = 0L
        var count = 0
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                
                val left = getGrayscale(pixels[idx - 1])
                val right = getGrayscale(pixels[idx + 1])
                val top = getGrayscale(pixels[idx - width])
                val bottom = getGrayscale(pixels[idx + width])
                
                // Simple gradient magnitude
                val gx = kotlin.math.abs(right - left)
                val gy = kotlin.math.abs(bottom - top)
                
                edgeSum += gx + gy
                count++
            }
        }
        
        return if (count > 0) edgeSum.toFloat() / count else 0f
    }
    
    /**
     * Get grayscale value from pixel
     */
    private fun getGrayscale(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
    
    /**
     * Load image from URI and resize it
     */
    private fun loadImageFromUri(imageUri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        return Bitmap.createScaledBitmap(originalBitmap, INPUT_SIZE, INPUT_SIZE, true)
    }
    
    /**
     * Preprocess image for model input
     */
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        inputBuffer.order(ByteOrder.nativeOrder())
        
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val pixelValue = intValues[pixel++]
                
                // Extract RGB values and normalize
                val r = ((pixelValue shr 16) and 0xFF) / IMAGE_STD - IMAGE_MEAN / IMAGE_STD
                val g = ((pixelValue shr 8) and 0xFF) / IMAGE_STD - IMAGE_MEAN / IMAGE_STD
                val b = (pixelValue and 0xFF) / IMAGE_STD - IMAGE_MEAN / IMAGE_STD
                
                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }
        
        return inputBuffer
    }
    
    /**
     * Get the confidence score from the last prediction
     */
    fun getConfidenceScore(): Float = lastConfidence
    
    /**
     * Get the image processing details from the last prediction
     */
    fun getProcessingDetails(): ImageProcessingDetails = lastProcessingDetails
    
    /**
     * Check if the model is ready for inference
     */
    fun isModelReady(): Boolean = modelLoaded && interpreter != null
    
    /**
     * Clean up resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        modelLoaded = false
    }
}
