package com.dicoding.cataract_detection_app_final_project.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImagePicker(private val activity: ComponentActivity) {
    
    private var cameraImageUri: Uri? = null
    private var onImageSelected: ((Uri?) -> Unit)? = null
    private var pendingAction: (() -> Unit)? = null
    
    // Gallery launcher
    private val galleryLauncher: ActivityResultLauncher<Intent> = 
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            android.util.Log.d("ImagePicker", "Gallery result: ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                android.util.Log.d("ImagePicker", "Selected image URI: $selectedImageUri")
                onImageSelected?.invoke(selectedImageUri)
            } else {
                android.util.Log.d("ImagePicker", "Gallery selection cancelled or failed")
                onImageSelected?.invoke(null)
            }
        }
    
    // Camera launcher
    private val cameraLauncher: ActivityResultLauncher<Intent> = 
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onImageSelected?.invoke(cameraImageUri)
            }
        }
    
    // Permission launcher
    private val permissionLauncher: ActivityResultLauncher<Array<String>> = 
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                // Permissions granted, proceed with pending action
                pendingAction?.invoke()
            } else {
                // Handle permission denied
                onImageSelected?.invoke(null)
            }
            pendingAction = null
        }
    
    fun pickImageFromGallery(onImageSelected: (Uri?) -> Unit) {
        android.util.Log.d("ImagePicker", "pickImageFromGallery called")
        this.onImageSelected = onImageSelected
        
        // Check permissions
        if (hasStoragePermission()) {
            android.util.Log.d("ImagePicker", "Storage permission granted, opening gallery")
            openGallery()
        } else {
            android.util.Log.d("ImagePicker", "Storage permission not granted, requesting permission")
            pendingAction = { openGallery() }
            requestStoragePermission()
        }
    }
    
    fun captureImageFromCamera(onImageSelected: (Uri?) -> Unit) {
        this.onImageSelected = onImageSelected
        
        // Check permissions
        if (hasCameraPermission()) {
            openCamera()
        } else {
            pendingAction = { openCamera() }
            requestCameraPermission()
        }
    }
    
    private fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) uses READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestStoragePermission() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)
    }
    
    private fun requestCameraPermission() {
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
    }
    
    private fun openGallery() {
        android.util.Log.d("ImagePicker", "openGallery called")
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        // Try to use ACTION_PICK first, fallback to ACTION_GET_CONTENT
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val chooserIntent = Intent.createChooser(intent, "Select Image").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        }
        android.util.Log.d("ImagePicker", "Launching gallery chooser")
        galleryLauncher.launch(chooserIntent)
    }
    
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        
        // Create a file to store the image
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        
        photoFile?.let { file ->
            cameraImageUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                file
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
            cameraLauncher.launch(intent)
        }
    }
    
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = activity.getExternalFilesDir("Pictures") ?: activity.filesDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}
