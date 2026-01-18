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
    
    // Confidence threshold for "Unknown" / "Not an Eye" detection
    // Increased to 0.75 to filter out random non-eye objects that might accidentally trigger a match
    private val CONFIDENCE_THRESHOLD = 0.75f
    
    init {
        // Check if model file exists in assets
        try {
            val assetManager = context.assets
            val modelFiles = assetManager.list("")
            Log.d("CataractModel", "Available assets: ${modelFiles?.joinToString(", ")}")
            
            if (modelFiles?.contains("cataract_model_90percent.tflite") == true) {
                Log.d("CataractModel", "Model file found in assets")
                loadModel()
            } else {
                Log.e("CataractModel", "Model file not found in assets")
                // Try alternative loading method
                tryAlternativeModelLoading()
            }
        } catch (e: Exception) {
            Log.e("CataractModel", "Error checking assets: ${e.message}", e)
            modelLoaded = false
        }
    }
    
    /**
     * Try alternative model loading method
     */
    private fun tryAlternativeModelLoading() {
        try {
            Log.d("CataractModel", "Trying alternative model loading...")
            val inputStream = context.assets.open("cataract_model_90percent.tflite")
            val modelBytes = inputStream.readBytes()
            inputStream.close()
            
            Log.d("CataractModel", "Model file read successfully, size: ${modelBytes.size} bytes")
            
            if (modelBytes.isNotEmpty()) {
                val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
                modelBuffer.put(modelBytes)
                modelBuffer.rewind()
                
                val options = Interpreter.Options()
                options.setNumThreads(4)
                
                interpreter = Interpreter(modelBuffer, options)
                modelLoaded = true
                Log.d("CataractModel", "Model loaded successfully using alternative method")
            } else {
                Log.e("CataractModel", "Model file is empty")
                modelLoaded = false
            }
        } catch (e: Exception) {
            Log.e("CataractModel", "Alternative model loading failed: ${e.message}", e)
            modelLoaded = false
        }
    }
    
    /**
     * Load the TensorFlow Lite model from assets
     */
    private fun loadModel() {
        try {
            Log.d("CataractModel", "Starting model loading...")
            val modelBuffer = loadModelFile("cataract_model_90percent.tflite")
            Log.d("CataractModel", "Model file loaded, buffer size: ${modelBuffer.capacity()}")
            
            // Validate model buffer
            if (modelBuffer.capacity() == 0) {
                throw Exception("Model buffer is empty")
            }
            
            val options = Interpreter.Options()
            options.setNumThreads(4)
            // Add GPU delegate if available
            try {
                // options.addDelegate(GpuDelegate())
                Log.d("CataractModel", "GPU delegate not enabled, using CPU")
            } catch (e: Exception) {
                Log.d("CataractModel", "GPU delegate not available, using CPU")
            }
            
            val interpreter =
                Interpreter(modelBuffer, options)
            this.interpreter = interpreter
            
            // Test the interpreter
            Log.d("CataractModel", "Testing interpreter...")
            val inputShape = interpreter.getInputTensor(0)?.shape()
            val outputShape = interpreter.getOutputTensor(0)?.shape()
            Log.d("CataractModel", "Input shape: ${inputShape?.contentToString()}")
            Log.d("CataractModel", "Output shape: ${outputShape?.contentToString()}")
            
            modelLoaded = true
            Log.d("CataractModel", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("CataractModel", "Error loading model: ${e.message}", e)
            e.printStackTrace()
            modelLoaded = false

            Log.d("CataractModel", "Attempting fallback model loading strategy")
            tryAlternativeModelLoading()
        }
    }
    
    /**
     * Load model file from assets
     */
    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        try {
            Log.d("CataractModel", "Loading model file: $modelPath")
            val assetFileDescriptor = context.assets.openFd(modelPath)
            Log.d("CataractModel", "Asset file descriptor opened successfully")
            
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            
            Log.d("CataractModel", "File details - Start offset: $startOffset, Length: $declaredLength")
            
            val mappedBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            Log.d("CataractModel", "Model file mapped successfully, size: ${mappedBuffer.capacity()}")
            
            return mappedBuffer
        } catch (e: Exception) {
            Log.e("CataractModel", "Error loading model file: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Predict cataract from image URI
     * @param imageUri Uri of the image file
     * @return Prediction result ("Normal" or "Cataract")
     */
    fun predictCataract(imageUri: Uri): String {
        if (!modelLoaded || interpreter == null) {
            Log.w("CataractModel", "Model not loaded, attempting to reinitialize")
            loadModel()

            if (!modelLoaded || interpreter == null) {
                Log.e("CataractModel", "Model still unavailable after reinitialization")
                return "Error: Model failed to load"
            }
        }
        
        try {
            Log.d("CataractModel", "Starting prediction with loaded model")
            
            // Load and preprocess image
            val bitmap = loadImageFromUri(imageUri)
            Log.d("CataractModel", "Image loaded, size: ${bitmap.width}x${bitmap.height}")
            
            // Check image validity (bad lighting, solid colors, etc)
            if (!validateImage(bitmap)) {
                Log.d("CataractModel", "Image validation failed (too dark/bright or low variance)")
                lastConfidence = 0.0f
                return "Unknown"
            }
            
            val inputBuffer = preprocessImage(bitmap)
            Log.d("CataractModel", "Image preprocessed, buffer size: ${inputBuffer.capacity()}")
            
            // Prepare output array
            // Model output is [1, 1] (Sigmoid activation for binary classification)
            val outputArray = Array(1) { FloatArray(1) }
            
            // Run inference
            Log.d("CataractModel", "Running inference...")
            interpreter?.run(inputBuffer, outputArray)
            Log.d("CataractModel", "Inference completed")
            
            // Process results
            // For sigmoid: 0.0 = Class 0 (Normal), 1.0 = Class 1 (Cataract)
            // Threshold is usually 0.5
            val probability = outputArray[0][0]
            
            Log.d("CataractModel", "Raw probability: $probability")
            
            // Update processing details with raw output
            lastProcessingDetails = lastProcessingDetails.copy(rawOutput = probability)
            
            // Calculate confidence
            // If prob > 0.5, it's Normal with confidence 'prob'
            // If prob <= 0.5, it's Cataract with confidence '1 - prob'
            lastConfidence = if (probability > 0.5f) probability else 1.0f - probability
            
            // Return prediction based on threshold
            // User reported results were swapped, so we invert the logic:
            // High probability (> 0.5) -> Normal
            // Low probability (<= 0.5) -> Cataract
            val result = if (lastConfidence < CONFIDENCE_THRESHOLD) {
                Log.d("CataractModel", "Confidence $lastConfidence below threshold $CONFIDENCE_THRESHOLD -> Unknown")
                "Unknown"
            } else if (probability > 0.5f) {
                "Normal"
            } else {
                "Cataract"
            }
            
            Log.d("CataractModel", "Final prediction: $result with confidence: $lastConfidence")
            return result
            
        } catch (e: Exception) {
            Log.e("CataractModel", "Error during prediction: ${e.message}", e)
            e.printStackTrace()
            return "Error: ${e.message}"
        }
    }
    
    /**
     * Validate image for basic quality checks (brightness, variance, edge density)
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
            
            // Calculate edge density using simple gradient
            val edgeDensity = calculateEdgeDensity(pixels, width, height)
            
            Log.d("CataractModel", "Image Stats - Mean Brightness: $meanBrightness, Variance: $variance, Edge Density: $edgeDensity")
            
            // Store processing details for breakdown display
            val isValid = meanBrightness in 20..235 && variance >= 100
            lastProcessingDetails = ImageProcessingDetails(
                rawOutput = 0f, // Will be updated after inference
                meanBrightness = meanBrightness.toFloat(),
                variance = variance.toFloat(),
                edgeDensity = edgeDensity,
                isValidImage = isValid
            )
            
            // 1. Check if too dark or too bright
            // Range 0-255. < 20 is very dark, > 235 is blown out white
            if (meanBrightness < 20 || meanBrightness > 235) {
                Log.d("CataractModel", "Image rejected: Too dark or too bright")
                return false
            }
            
            // 2. Check variance (consistency)
            // Solid colors will have variance ~0.
            // Eye images typically have good contrast (pupil vs iris vs sclera).
            // Threshold of 100 is conservative (std dev 10).
            if (variance < 100) {
                Log.d("CataractModel", "Image rejected: Low variance (solid color or blurred)")
                return false
            }
            
            return true
            
        } catch (e: Exception) {
            Log.e("CataractModel", "Error validating image: ${e.message}")
            return true // Fail safe: assume valid if check fails
        }
    }
    
    /**
     * Calculate edge density using simple Sobel-like gradient
     */
    private fun calculateEdgeDensity(pixels: IntArray, width: Int, height: Int): Float {
        var edgeSum = 0L
        var count = 0
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                
                // Get grayscale values
                val left = getGrayscale(pixels[idx - 1])
                val right = getGrayscale(pixels[idx + 1])
                val top = getGrayscale(pixels[idx - width])
                val bottom = getGrayscale(pixels[idx + width])
                
                // Simple gradient magnitude
                val gx = kotlin.math.abs(right - left)
                val gy = kotlin.math.abs(bottom - top)
                val gradient = gx + gy
                
                edgeSum += gradient
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
     * Fallback prediction when model is not available
     */
    private fun getFallbackPrediction(imageUri: Uri): String {
        Log.d("CataractModel", "Using fallback prediction")
        lastConfidence = 0.0f
        return "Error: Unknown prediction failure"
    }
    
    /**
     * Load image from URI and resize it
     */
    private fun loadImageFromUri(imageUri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        // Resize bitmap to model input size
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
     * @return Confidence score (0.0 to 1.0)
     */
    fun getConfidenceScore(): Float {
        return lastConfidence
    }
    
    /**
     * Get the image processing details from the last prediction
     * @return ImageProcessingDetails containing brightness, variance, edge density, etc.
     */
    fun getProcessingDetails(): ImageProcessingDetails {
        return lastProcessingDetails
    }
    
    /**
     * Check if the model is ready for inference
     * @return True if model is ready, false otherwise
     */
    fun isModelReady(): Boolean {
        return modelLoaded && interpreter != null
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        modelLoaded = false
    }
}
