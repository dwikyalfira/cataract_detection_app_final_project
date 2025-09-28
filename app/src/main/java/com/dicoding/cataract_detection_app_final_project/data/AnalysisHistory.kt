package com.dicoding.cataract_detection_app_final_project.data

import java.util.Date

data class AnalysisHistory(
    val id: String = "",
    val imageUri: String = "",
    val predictionResult: String = "",
    val confidence: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
) {
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    fun getFormattedDateShort(): String {
        val date = Date(timestamp)
        val formatter = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    fun isHealthy(): Boolean {
        return predictionResult.lowercase().contains("normal") || 
               predictionResult.lowercase().contains("healthy")
    }
}

