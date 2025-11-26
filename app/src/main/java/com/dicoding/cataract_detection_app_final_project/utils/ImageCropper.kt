package com.dicoding.cataract_detection_app_final_project.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.dicoding.cataract_detection_app_final_project.view.ROIRect
import com.dicoding.cataract_detection_app_final_project.view.ImageAdjustments
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for cropping images based on Region of Interest (ROI)
 */
class ImageCropper(private val context: Context) {
    
    /**
     * Crop image based on ROI coordinates and return the cropped image URI
     * @param originalImageUri Original image URI
     * @param roiRect Region of Interest rectangle (normalized coordinates 0.0-1.0)
     * @param adjustments Image adjustments (scale, offset)
     * @return URI of the cropped image
     */
    fun cropImage(originalImageUri: Uri, roiRect: ROIRect, adjustments: ImageAdjustments = ImageAdjustments()): Uri? {
        return try {
            Log.d("ImageCropper", "Starting image crop with ROI: $roiRect")
            
            // Load original image
            val originalBitmap = loadBitmapFromUri(originalImageUri)
            if (originalBitmap == null) {
                Log.e("ImageCropper", "Failed to load original bitmap")
                return null
            }
            
            Log.d("ImageCropper", "Original image size: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Apply image adjustments (scaling)
            val adjustedBitmap = applyImageAdjustments(originalBitmap, adjustments)
            Log.d("ImageCropper", "Adjusted image size: ${adjustedBitmap.width}x${adjustedBitmap.height}")
            
            // Convert normalized ROI to pixel coordinates
            val pixelROI = roiRect.denormalize(
                adjustedBitmap.width.toFloat(),
                adjustedBitmap.height.toFloat()
            )
            
            Log.d("ImageCropper", "Pixel ROI: $pixelROI")
            
            // Ensure ROI is within image bounds
            val clampedROI = clampROIToImageBounds(pixelROI, adjustedBitmap.width, adjustedBitmap.height)
            
            // Crop the bitmap
            val croppedBitmap = Bitmap.createBitmap(
                adjustedBitmap,
                clampedROI.left.toInt(),
                clampedROI.top.toInt(),
                clampedROI.width.toInt(),
                clampedROI.height.toInt()
            )
            
            Log.d("ImageCropper", "Cropped image size: ${croppedBitmap.width}x${croppedBitmap.height}")
            
            // Save cropped image to temporary file
            val croppedUri = saveBitmapToTempFile(croppedBitmap)
            
            // Clean up
            originalBitmap.recycle()
            adjustedBitmap.recycle()
            croppedBitmap.recycle()
            
            Log.d("ImageCropper", "Image crop completed successfully")
            croppedUri
            
        } catch (e: Exception) {
            Log.e("ImageCropper", "Error cropping image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Apply image adjustments (scaling, etc.)
     */
    private fun applyImageAdjustments(bitmap: Bitmap, adjustments: ImageAdjustments): Bitmap {
        if (adjustments.scale == 1.0f) {
            return bitmap
        }
        
        val newWidth = (bitmap.width * adjustments.scale).toInt()
        val newHeight = (bitmap.height * adjustments.scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Load bitmap from URI
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            Log.e("ImageCropper", "Error loading bitmap from URI: ${e.message}", e)
            null
        }
    }
    
    /**
     * Clamp ROI coordinates to image bounds
     */
    private fun clampROIToImageBounds(roi: ROIRect, imageWidth: Int, imageHeight: Int): ROIRect {
        return ROIRect(
            left = roi.left.coerceIn(0f, imageWidth.toFloat()),
            top = roi.top.coerceIn(0f, imageHeight.toFloat()),
            right = roi.right.coerceIn(0f, imageWidth.toFloat()),
            bottom = roi.bottom.coerceIn(0f, imageHeight.toFloat())
        )
    }
    
    /**
     * Save bitmap to temporary file and return URI
     */
    private fun saveBitmapToTempFile(bitmap: Bitmap): Uri? {
        return try {
            // Create temporary file
            val tempDir = File(context.cacheDir, "cropped_images")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            
            val tempFile = File.createTempFile(
                "cropped_${System.currentTimeMillis()}",
                ".jpg",
                tempDir
            )
            
            // Compress and save bitmap
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // Return file URI
            Uri.fromFile(tempFile)
            
        } catch (e: IOException) {
            Log.e("ImageCropper", "Error saving cropped image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get image dimensions from URI without loading the full bitmap
     */
    fun getImageDimensions(uri: Uri): Pair<Int, Int>? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            if (options.outWidth > 0 && options.outHeight > 0) {
                Pair(options.outWidth, options.outHeight)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ImageCropper", "Error getting image dimensions: ${e.message}", e)
            null
        }
    }
    
    /**
     * Clean up temporary files
     */
    fun cleanupTempFiles() {
        try {
            val tempDir = File(context.cacheDir, "cropped_images")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.startsWith("cropped_")) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ImageCropper", "Error cleaning up temp files: ${e.message}", e)
        }
    }
}
