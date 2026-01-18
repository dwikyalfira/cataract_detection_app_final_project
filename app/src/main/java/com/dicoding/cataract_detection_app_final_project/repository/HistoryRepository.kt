package com.dicoding.cataract_detection_app_final_project.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.dicoding.cataract_detection_app_final_project.data.AnalysisHistory
import com.dicoding.cataract_detection_app_final_project.data.api.ApiClient
import com.dicoding.cataract_detection_app_final_project.data.api.HistoryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class HistoryRepository(private val context: Context) {
    
    private val apiService = ApiClient.instance
    
    /**
     * Get analysis history from server
     */
    fun getAnalysisHistory(userId: String): Flow<List<AnalysisHistory>> = flow {
        try {
            val response = apiService.getHistory(userId)
            if (response.isSuccessful && response.body()?.status == "success") {
                val historyList = response.body()?.data?.map { it.toAnalysisHistory() } ?: emptyList()
                emit(historyList)
            } else {
                android.util.Log.e("HistoryRepository", "Failed to fetch history: ${response.message()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            android.util.Log.e("HistoryRepository", "Error fetching history: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Save analysis history to server with image upload
     */
    suspend fun saveAnalysisHistory(history: AnalysisHistory) {
        withContext(Dispatchers.IO) {
            try {
                // Get image file from URI
                val imageFile = getFileFromUri(Uri.parse(history.imageUri))
                if (imageFile == null) {
                    android.util.Log.e("HistoryRepository", "Failed to get image file from URI")
                    return@withContext
                }
                
                // Compress image before upload
                val compressedFile = compressImage(imageFile)
                val fileToUpload = compressedFile ?: imageFile
                
                // Create multipart request
                val requestFile = fileToUpload.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", fileToUpload.name, requestFile)
                
                // Create request bodies for other fields
                val userIdBody = history.userId.toRequestBody("text/plain".toMediaTypeOrNull())
                val predictionBody = history.predictionResult.toRequestBody("text/plain".toMediaTypeOrNull())
                val confidenceBody = history.confidence.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val rawOutputBody = history.rawOutput.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val meanBrightnessBody = history.meanBrightness.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val varianceBody = history.variance.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val edgeDensityBody = history.edgeDensity.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                
                // Upload to server
                val response = apiService.uploadHistory(
                    userId = userIdBody,
                    image = imagePart,
                    predictionResult = predictionBody,
                    confidence = confidenceBody,
                    rawOutput = rawOutputBody,
                    meanBrightness = meanBrightnessBody,
                    variance = varianceBody,
                    edgeDensity = edgeDensityBody
                )
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    android.util.Log.d("HistoryRepository", "History uploaded successfully: ${response.body()?.data?.historyId}")
                } else {
                    android.util.Log.e("HistoryRepository", "Failed to upload history: ${response.message()}")
                }
                
                // Clean up temp files
                if (compressedFile != null && compressedFile.exists()) {
                    compressedFile.delete()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("HistoryRepository", "Error saving history: ${e.message}", e)
            }
        }
    }
    
    /**
     * Delete analysis history from server
     */
    suspend fun deleteAnalysisHistory(historyId: String, userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteHistory(userId, historyId)
                if (response.isSuccessful && response.body()?.status == "success") {
                    android.util.Log.d("HistoryRepository", "History deleted successfully: $historyId")
                } else {
                    android.util.Log.e("HistoryRepository", "Failed to delete history: ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HistoryRepository", "Error deleting history: ${e.message}", e)
            }
        }
    }
    
    /**
     * Clear all history for a user (not implemented for server-side - would need new endpoint)
     */
    suspend fun clearAllHistory(userId: String) {
        android.util.Log.w("HistoryRepository", "clearAllHistory not implemented for server-side storage")
        // TODO: Implement server-side clear all endpoint if needed
    }
    
    /**
     * Get file from content URI
     */
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                tempFile
            } else {
                // Try as file path
                val file = File(uri.path ?: return null)
                if (file.exists()) file else null
            }
        } catch (e: Exception) {
            android.util.Log.e("HistoryRepository", "Error getting file from URI: ${e.message}")
            null
        }
    }
    
    /**
     * Compress image to reduce upload size (target ~500KB)
     */
    private fun compressImage(imageFile: File): File? {
        return try {
            val maxSize = 500 * 1024 // 500KB
            if (imageFile.length() <= maxSize) {
                return null // No compression needed
            }
            
            // Load bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imageFile.absolutePath, options)
            
            // Calculate sample size
            var sampleSize = 1
            while ((options.outWidth / sampleSize) * (options.outHeight / sampleSize) > 1920 * 1080) {
                sampleSize *= 2
            }
            
            options.inJustDecodeBounds = false
            options.inSampleSize = sampleSize
            
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options) ?: return null
            
            // Compress to JPEG
            val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            var quality = 85
            
            do {
                FileOutputStream(compressedFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                quality -= 10
            } while (compressedFile.length() > maxSize && quality > 30)
            
            bitmap.recycle()
            
            compressedFile
        } catch (e: Exception) {
            android.util.Log.e("HistoryRepository", "Error compressing image: ${e.message}")
            null
        }
    }
    
    /**
     * Extension function to convert HistoryData to AnalysisHistory
     */
    private fun HistoryData.toAnalysisHistory(): AnalysisHistory {
        return AnalysisHistory(
            id = this.id,
            imageUri = this.imageUrl, // Use full URL from server
            predictionResult = this.predictionResult,
            confidence = this.confidence,
            timestamp = this.timestamp,
            userId = this.userId,
            rawOutput = this.rawOutput,
            meanBrightness = this.meanBrightness,
            variance = this.variance,
            edgeDensity = this.edgeDensity
        )
    }
}
