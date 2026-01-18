package com.dicoding.cataract_detection_app_final_project.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ApiResponse<Void>>

    @FormUrlEncoded
    @POST("change_password.php")
    fun changePassword(
        @Field("uid") uid: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String
    ): Call<ApiResponse<Void>>
    @FormUrlEncoded
    @POST("update_stats.php")
    fun updateUserStats(
        @Field("uid") uid: String,
        @Field("is_healthy") isHealthy: String
    ): Call<ApiResponse<Void>>

    @FormUrlEncoded
    @POST("delete_account.php")
    fun deleteAccount(
        @Field("uid") uid: String,
        @Field("password") password: String
    ): Call<ApiResponse<Void>>

    @FormUrlEncoded
    @POST("forgot_password.php")
    fun forgotPassword(
        @Field("email") email: String
    ): Call<ApiResponse<Map<String, String>>>

    @FormUrlEncoded
    @POST("reset_password.php")
    fun resetPassword(
        @Field("email") email: String,
        @Field("otp") otp: String,
        @Field("new_password") newPassword: String
    ): Call<ApiResponse<Void>>

    @FormUrlEncoded
    @POST("delete_history_stats.php")
    fun deleteHistoryStats(
        @Field("uid") uid: String,
        @Field("is_healthy") isHealthy: String
    ): Call<ApiResponse<Void>>

    // History endpoints
    @Multipart
    @POST("upload_history.php")
    suspend fun uploadHistory(
        @Part("user_id") userId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("prediction_result") predictionResult: RequestBody,
        @Part("confidence") confidence: RequestBody,
        @Part("raw_output") rawOutput: RequestBody,
        @Part("mean_brightness") meanBrightness: RequestBody,
        @Part("variance") variance: RequestBody,
        @Part("edge_density") edgeDensity: RequestBody
    ): Response<ApiResponse<HistoryUploadData>>

    @FormUrlEncoded
    @POST("get_history.php")
    suspend fun getHistory(
        @Field("user_id") userId: String
    ): Response<ApiResponse<List<HistoryData>>>

    @FormUrlEncoded
    @POST("delete_history.php")
    suspend fun deleteHistory(
        @Field("user_id") userId: String,
        @Field("history_id") historyId: String
    ): Response<ApiResponse<Void>>
}

