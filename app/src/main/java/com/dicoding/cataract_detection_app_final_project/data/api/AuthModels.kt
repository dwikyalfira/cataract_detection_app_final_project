package com.dicoding.cataract_detection_app_final_project.data.api

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T? = null,
    @SerializedName("uid") val uid: String? = null // For registration response
)

data class UserData(
    @SerializedName("uid") val uid: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("total_scans") val totalScans: Int = 0,
    @SerializedName("healthy_scans") val healthyScans: Int = 0,
    @SerializedName("alert_scans") val alertScans: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)

// History API models
data class HistoryData(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("image_path") val imagePath: String,
    @SerializedName("prediction_result") val predictionResult: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("raw_output") val rawOutput: Float = 0f,
    @SerializedName("mean_brightness") val meanBrightness: Float = 0f,
    @SerializedName("variance") val variance: Float = 0f,
    @SerializedName("edge_density") val edgeDensity: Float = 0f,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("timestamp") val timestamp: Long
)

data class HistoryUploadData(
    @SerializedName("history_id") val historyId: String,
    @SerializedName("image_path") val imagePath: String
)

