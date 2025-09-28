package com.dicoding.cataract_detection_app_final_project.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages image storage for analysis history
 * Copies images from external sources (gallery) to app's internal storage
 * to ensure they persist across app restarts
 */
class ImageStorageManager(private val context: Context) {
    
    private val imagesDir: File by lazy {
        File(context.filesDir, "analysis_images").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Copy image from URI to internal storage and return the internal URI
     * @param sourceUri Original URI (could be content://, file://, etc.)
     * @return Internal file URI that will persist across app restarts
     */
    suspend fun copyImageToInternalStorage(sourceUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            // Check if URI is already from internal storage
            val uriString = sourceUri.toString()
            if (isInternalStorageUri(uriString)) {
                Log.d("ImageStorageManager", "URI is already from internal storage: $uriString")
                return@withContext uriString
            }
            
            // Generate unique filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "analysis_${timestamp}_${System.currentTimeMillis()}.jpg"
            val internalFile = File(imagesDir, filename)
            
            // Try multiple approaches to read the image
            val bitmap = readImageFromUri(sourceUri)
            if (bitmap == null) {
                throw Exception("Could not read image from URI: $sourceUri")
            }
            
            // Compress and save to internal storage
            val outputStream = FileOutputStream(internalFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // Verify the file was created successfully
            if (!internalFile.exists() || internalFile.length() == 0L) {
                Log.e("ImageStorageManager", "Failed to create internal file or file is empty")
                throw Exception("Failed to create internal file or file is empty")
            }
            
            val internalUri = Uri.fromFile(internalFile).toString()
            Log.d("ImageStorageManager", "Image copied to internal storage: $internalUri")
            
            internalUri
            
        } catch (e: Exception) {
            Log.e("ImageStorageManager", "Error copying image to internal storage", e)
            // Instead of falling back to original URI, throw the exception
            // This ensures that gallery images that fail to copy are not saved with inaccessible URIs
            throw Exception("Failed to copy image to internal storage: ${e.message}", e)
        }
    }
    
    /**
     * Read image from URI using multiple approaches for better compatibility
     */
    private fun readImageFromUri(sourceUri: Uri): Bitmap? {
        return try {
            Log.d("ImageStorageManager", "Attempting to read image from URI: $sourceUri")
            
            // First approach: Direct input stream
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (bitmap != null) {
                    Log.d("ImageStorageManager", "Successfully read image using direct input stream")
                    return bitmap
                }
            }
            
            // Second approach: Try with options for better compatibility
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = 1
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            
            val inputStream2: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream2 != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
                inputStream2.close()
                if (bitmap != null) {
                    Log.d("ImageStorageManager", "Successfully read image using options approach")
                    return bitmap
                }
            }
            
            // Third approach: For content URIs, try to get the file path and read directly
            if (sourceUri.scheme == "content") {
                val bitmap = tryReadFromContentUri(sourceUri)
                if (bitmap != null) {
                    Log.d("ImageStorageManager", "Successfully read image using content URI approach")
                    return bitmap
                }
            }
            
            Log.e("ImageStorageManager", "All approaches failed to read image from URI: $sourceUri")
            null
            
        } catch (e: Exception) {
            Log.e("ImageStorageManager", "Exception while reading image from URI: $sourceUri", e)
            null
        }
    }
    
    /**
     * Try to read image from content URI using MediaStore
     */
    private fun tryReadFromContentUri(sourceUri: Uri): Bitmap? {
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(sourceUri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val filePath = it.getString(columnIndex)
                    if (filePath != null) {
                        val file = File(filePath)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(filePath)
                            if (bitmap != null) {
                                Log.d("ImageStorageManager", "Successfully read image from file path: $filePath")
                                return bitmap
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e("ImageStorageManager", "Failed to read from content URI", e)
            null
        }
    }
    
    /**
     * Check if an image URI is from internal storage
     */
    fun isInternalStorageUri(uri: String): Boolean {
        return uri.startsWith("file://") && uri.contains(context.filesDir.absolutePath)
    }
    
    /**
     * Clean up old images (optional - for storage management)
     * @param maxAgeDays Maximum age of images to keep in days
     */
    suspend fun cleanupOldImages(maxAgeDays: Int = 30) = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
            
            imagesDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        Log.d("ImageStorageManager", "Deleted old image: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ImageStorageManager", "Error cleaning up old images", e)
        }
    }
    
    /**
     * Get total size of stored images
     */
    fun getStoredImagesSize(): Long {
        return imagesDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
    
    /**
     * Clear all images for a specific user (when account is deleted)
     * @param userId The user ID to clear images for
     */
    suspend fun clearUserImages(userId: String) = withContext(Dispatchers.IO) {
        try {
            // Since images are stored with timestamps and not user IDs directly,
            // we'll clear all images in the directory when a user account is deleted
            // This is a simple approach - in a more complex app, you might want to
            // store user IDs with images for more granular control
            imagesDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    Log.d("ImageStorageManager", "Deleted user image: ${file.name}")
                }
            }
            Log.d("ImageStorageManager", "Cleared all images for user: $userId")
        } catch (e: Exception) {
            Log.e("ImageStorageManager", "Error clearing user images", e)
        }
    }
}
