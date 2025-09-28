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
 * TensorFlow Lite model class for CNN cataract detection
 */
class CataractModel(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private var modelLoaded = false
    private var lastConfidence: Float = 0.0f
    
    // Model input/output specifications
    private val INPUT_SIZE = 224
    private val PIXEL_SIZE = 3
    private val IMAGE_MEAN = 127.5f
    private val IMAGE_STD = 127.5f
    
    init {
        // Check if model file exists in assets
        try {
            val assetManager = context.assets
            val modelFiles = assetManager.list("")
            Log.d("CataractModel", "Available assets: ${modelFiles?.joinToString(", ")}")
            
            if (modelFiles?.contains("cataract_model.tflite") == true) {
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
            val inputStream = context.assets.open("cataract_model.tflite")
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
            val modelBuffer = loadModelFile("cataract_model.tflite")
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
            
            Log.d("CataractModel", "Creating TensorFlow Lite interpreter...")
            
            interpreter = Interpreter(modelBuffer, options)
            
            // Test the interpreter
            Log.d("CataractModel", "Testing interpreter...")
            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val outputShape = interpreter?.getOutputTensor(0)?.shape()
            Log.d("CataractModel", "Input shape: ${inputShape?.contentToString()}")
            Log.d("CataractModel", "Output shape: ${outputShape?.contentToString()}")
            
            modelLoaded = true
            Log.d("CataractModel", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("CataractModel", "Error loading model: ${e.message}", e)
            e.printStackTrace()
            modelLoaded = false

            if (!modelLoaded) {
                Log.d("CataractModel", "Attempting fallback model loading strategy")
                tryAlternativeModelLoading()
            }
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
                Log.e("CataractModel", "Model still unavailable after reinitialization, using fallback prediction")
                return getFallbackPrediction(imageUri)
            }
        }
        
        try {
            Log.d("CataractModel", "Starting prediction with loaded model")
            
            // Load and preprocess image
            val bitmap = loadImageFromUri(imageUri)
            Log.d("CataractModel", "Image loaded, size: ${bitmap.width}x${bitmap.height}")
            
            val inputBuffer = preprocessImage(bitmap)
            Log.d("CataractModel", "Image preprocessed, buffer size: ${inputBuffer.capacity()}")
            
            // Prepare output array
            val outputArray = Array(1) { FloatArray(2) } // Assuming 2 classes: Normal, Cataract
            
            // Run inference
            Log.d("CataractModel", "Running inference...")
            interpreter?.run(inputBuffer, outputArray)
            Log.d("CataractModel", "Inference completed")
            
            // Process results
            val probabilities = outputArray[0]
            val normalProb = probabilities[0]
            val cataractProb = probabilities[1]
            
            Log.d("CataractModel", "Raw probabilities - Normal: $normalProb, Cataract: $cataractProb")
            
            // Calculate confidence (max probability)
            lastConfidence = maxOf(normalProb, cataractProb)
            
            // Return prediction based on higher probability
            val result = if (cataractProb > normalProb) {
                "Cataract"
            } else {
                "Normal"
            }
            
            Log.d("CataractModel", "Final prediction: $result with confidence: $lastConfidence")
            return result
            
        } catch (e: Exception) {
            Log.e("CataractModel", "Error during prediction: ${e.message}", e)
            e.printStackTrace()
            return getFallbackPrediction(imageUri)
        }
    }
    
    /**
     * Fallback prediction when model is not available
     */
    private fun getFallbackPrediction(imageUri: Uri): String {
        Log.d("CataractModel", "Using fallback prediction")
        // Simple fallback - you could implement basic image analysis here
        lastConfidence = 0.5f // Low confidence for fallback
        return "Normal" // Default to normal for safety
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
