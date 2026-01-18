package com.dicoding.cataract_detection_app_final_project.presenter

import android.content.Context
import com.dicoding.cataract_detection_app_final_project.data.api.ApiClient
import com.dicoding.cataract_detection_app_final_project.data.api.ApiResponse
import com.dicoding.cataract_detection_app_final_project.data.api.UserData
import com.dicoding.cataract_detection_app_final_project.utils.ErrorTranslator
import com.dicoding.cataract_detection_app_final_project.utils.UserSession
import com.dicoding.cataract_detection_app_final_project.utils.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AuthPresenter(private var context: Context) {
    private val apiService = ApiClient.instance
    private val sessionManager = UserSessionManager(context)
    
    private val _isAuthenticated = MutableStateFlow(false)
    private val _isAuthenticatedInternal = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Separate error and success messages for each screen
    private val _loginErrorMessage = MutableStateFlow<String?>(null)
    val loginErrorMessage: StateFlow<String?> = _loginErrorMessage.asStateFlow()
    
    private val _registerErrorMessage = MutableStateFlow<String?>(null)
    val registerErrorMessage: StateFlow<String?> = _registerErrorMessage.asStateFlow()
    
    private val _registerSuccessMessage = MutableStateFlow<String?>(null)
    val registerSuccessMessage: StateFlow<String?> = _registerSuccessMessage.asStateFlow()
    
    private val _forgotPasswordErrorMessage = MutableStateFlow<String?>(null)
    val forgotPasswordErrorMessage: StateFlow<String?> = _forgotPasswordErrorMessage.asStateFlow()
    
    private val _forgotPasswordSuccessMessage = MutableStateFlow<String?>(null)
    val forgotPasswordSuccessMessage: StateFlow<String?> = _forgotPasswordSuccessMessage.asStateFlow()
    
    // Legacy properties for backward compatibility (will be removed)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering.asStateFlow()
    
    fun updateContext(newContext: Context) {
        android.util.Log.d("AuthPresenter", "Updating context, new locale: ${newContext.resources.configuration.locales[0]}")
        context = newContext
    }
    
    init {
        // Check for existing session
        CoroutineScope(Dispatchers.Main).launch {
            sessionManager.userSession.collect { session ->
                android.util.Log.d("AuthPresenter", "Session update: ${session?.email}")
                _currentUser.value = session
                _isAuthenticatedInternal.value = session != null
                _isAuthenticated.value = session != null && !_isRegistering.value
            }
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginErrorMessage.value = ErrorTranslator.translateError(context, "Please fill in all fields")
            return
        }
        
        _isLoading.value = true
        _loginErrorMessage.value = null
        
        apiService.login(email, password).enqueue(object : Callback<ApiResponse<UserData>> {
            override fun onResponse(
                call: Call<ApiResponse<UserData>>,
                response: Response<ApiResponse<UserData>>
            ) {
                _isLoading.value = false
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == "success") {
                    _loginErrorMessage.value = null
                    val userData = body.data
                    if (userData != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            sessionManager.saveUserSession(userData.uid, userData.name, email, userData.createdAt)
                        }
                    }
                } else {
                    val errorMsg = body?.message ?: "Login failed"
                    android.util.Log.d("AuthPresenter", "Login error: '$errorMsg'")
                    _loginErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserData>>, t: Throwable) {
                _isLoading.value = false
                val errorMsg = t.message ?: "Network error"
                android.util.Log.d("AuthPresenter", "Login network error: '$errorMsg'")
                _loginErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
            }
        })
    }
    
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _registerErrorMessage.value = ErrorTranslator.translateError(context, "Please fill in all fields")
            return
        }
        
        if (password != confirmPassword) {
            _registerErrorMessage.value = ErrorTranslator.translateError(context, "Passwords do not match")
            return
        }
        
        if (password.length < 6) {
            _registerErrorMessage.value = ErrorTranslator.translateError(context, "Password must be at least 6 characters")
            return
        }
        
        _isLoading.value = true
        _registerErrorMessage.value = null
        _registerSuccessMessage.value = null
        _isRegistering.value = true
        
        apiService.register(name, email, password).enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(
                call: Call<ApiResponse<Void>>,
                response: Response<ApiResponse<Void>>
            ) {
                _isLoading.value = false
                _isRegistering.value = false
                val body = response.body()
                
                if (response.isSuccessful && body != null && body.status == "success") {
                    _registerSuccessMessage.value = context.getString(com.dicoding.cataract_detection_app_final_project.R.string.success_account_created)
                    _registerErrorMessage.value = null
                } else {
                    val errorMsg = body?.message ?: "Registration failed"
                    android.util.Log.d("AuthPresenter", "Register error: '$errorMsg'")
                    _registerErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                _isLoading.value = false
                _isRegistering.value = false
                val errorMsg = t.message ?: "Network error"
                android.util.Log.d("AuthPresenter", "Register network error: '$errorMsg'")
                _registerErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
            }
        })
    }
    
    fun logout() {
        _isLoading.value = true
        _errorMessage.value = null
        
        CoroutineScope(Dispatchers.IO).launch {
            sessionManager.clearSession()
            _isLoading.value = false
        }
    }
    
    /*
    fun deleteAccount(password: String, callback: (Boolean, String) -> Unit) {
        // TODO: Implement delete account with local API
        callback(false, "Feature not available in local mode yet")
    }
    */
    
    fun requestPasswordReset(email: String) {
        if (email.isBlank()) {
            _forgotPasswordErrorMessage.value = ErrorTranslator.translateError(context, "Please enter your email")
            return
        }
        
        _isLoading.value = true
        _forgotPasswordErrorMessage.value = null
        _forgotPasswordSuccessMessage.value = null
        
        apiService.forgotPassword(email).enqueue(object : Callback<ApiResponse<Map<String, String>>> {
            override fun onResponse(
                call: Call<ApiResponse<Map<String, String>>>,
                response: Response<ApiResponse<Map<String, String>>>
            ) {
                _isLoading.value = false
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == "success") {
                    // For development, we might want to log the OTP if returned
                    val otpDebug = body.data?.get("otp_debug")
                    if (otpDebug != null) {
                        android.util.Log.d("AuthPresenter", "OTP Debug: $otpDebug")
                    }
                    _forgotPasswordSuccessMessage.value = context.getString(com.dicoding.cataract_detection_app_final_project.R.string.otp_sent_debug)
                } else {
                    val errorMsg = body?.message ?: "Failed to send reset link"
                    _forgotPasswordErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Map<String, String>>>, t: Throwable) {
                _isLoading.value = false
                val errorMsg = t.message ?: "Network error"
                _forgotPasswordErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
            }
        })
    }

    fun confirmPasswordReset(email: String, otp: String, newPassword: String) {
        if (email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            _forgotPasswordErrorMessage.value = ErrorTranslator.translateError(context, "Please fill in all fields")
            return
        }
        
        _isLoading.value = true
        _forgotPasswordErrorMessage.value = null
        
        apiService.resetPassword(email, otp, newPassword).enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(
                call: Call<ApiResponse<Void>>,
                response: Response<ApiResponse<Void>>
            ) {
                _isLoading.value = false
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == "success") {
                    _forgotPasswordSuccessMessage.value = context.getString(com.dicoding.cataract_detection_app_final_project.R.string.success_password_reset)
                } else {
                    val errorMsg = body?.message ?: "Failed to reset password"
                    _forgotPasswordErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                _isLoading.value = false
                val errorMsg = t.message ?: "Network error"
                _forgotPasswordErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
            }
        })
    }

    /*
    fun getUserData(uid: String, callback: (Map<String, Any>?) -> Unit) {
        // TODO: Implement get user data
        callback(null)
    }
    
    fun updateUserStats(uid: String, isHealthy: Boolean) {
        // TODO: Implement update stats
    }
    */
    
    fun changePassword(currentPassword: String, newPassword: String, callback: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            callback(false, ErrorTranslator.translateError(context, "User not authenticated"))
            return
        }
        
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            callback(false, ErrorTranslator.translateError(context, "Please fill in all fields"))
            return
        }
        
        if (newPassword.length < 6) {
            callback(false, ErrorTranslator.translateError(context, "Password must be at least 6 characters"))
            return
        }
        
        _isLoading.value = true
        
        apiService.changePassword(user.uid, currentPassword, newPassword).enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(
                call: Call<ApiResponse<Void>>,
                response: Response<ApiResponse<Void>>
            ) {
                _isLoading.value = false
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == "success") {
                    callback(true, ErrorTranslator.translateError(context, "Password changed successfully"))
                } else {
                    val errorMsg = body?.message ?: "Failed to change password"
                    callback(false, ErrorTranslator.translateError(context, errorMsg))
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                _isLoading.value = false
                val errorMsg = t.message ?: "Network error"
                callback(false, ErrorTranslator.translateError(context, errorMsg))
            }
        })
    }

    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    // Screen-specific clear methods
    fun clearLoginError() {
        _loginErrorMessage.value = null
    }
    
    fun clearRegisterError() {
        _registerErrorMessage.value = null
    }
    
    fun clearRegisterSuccess() {
        _registerSuccessMessage.value = null
    }
    
    fun clearRegisterMessages() {
        _registerErrorMessage.value = null
        _registerSuccessMessage.value = null
    }
    
    fun clearForgotPasswordError() {
        _forgotPasswordErrorMessage.value = null
    }
    
    fun clearForgotPasswordSuccess() {
        _forgotPasswordSuccessMessage.value = null
    }
    
    fun clearForgotPasswordMessages() {
        _forgotPasswordErrorMessage.value = null
        _forgotPasswordSuccessMessage.value = null
    }
    
    fun clearAllScreenMessages() {
        _loginErrorMessage.value = null
        _registerErrorMessage.value = null
        _registerSuccessMessage.value = null
        _forgotPasswordErrorMessage.value = null
        _forgotPasswordSuccessMessage.value = null
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    fun clearRegistrationState() {
        _isRegistering.value = false
        // Update authentication state after clearing registration state
        _isAuthenticated.value = _isAuthenticatedInternal.value && !_isRegistering.value
    }
    
    // Placeholder for deleteAccount to avoid compilation errors in SettingsView
    fun deleteAccount(password: String, callback: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            callback(false, ErrorTranslator.translateError(context, "User not authenticated"))
            return
        }

        if (password.isBlank()) {
            callback(false, ErrorTranslator.translateError(context, "Please enter your password"))
            return
        }

        _isLoading.value = true

        apiService.deleteAccount(user.uid, password).enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(
                call: Call<ApiResponse<Void>>,
                response: Response<ApiResponse<Void>>
            ) {
                _isLoading.value = false
                val body = response.body()
                if (response.isSuccessful && body != null && body.status == "success") {
                    // Clear session on successful deletion
                    CoroutineScope(Dispatchers.IO).launch {
                        sessionManager.clearSession()
                    }
                    callback(true, ErrorTranslator.translateError(context, "Account deleted successfully"))
                } else {
                    val errorMsg = body?.message ?: "Failed to delete account"
                    callback(false, ErrorTranslator.translateError(context, errorMsg))
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                _isLoading.value = false
                val errorMsg = t.message ?: "Network error"
                callback(false, ErrorTranslator.translateError(context, errorMsg))
            }
        })
    }

    // Placeholder for updateUserStats to avoid compilation errors
    fun updateUserStats(uid: String, isHealthy: Boolean) {
        val isHealthyStr = if (isHealthy) "true" else "false"
        
        apiService.updateUserStats(uid, isHealthyStr).enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(
                call: Call<ApiResponse<Void>>,
                response: Response<ApiResponse<Void>>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    android.util.Log.d("AuthPresenter", "Stats updated successfully")
                } else {
                    android.util.Log.e("AuthPresenter", "Failed to update stats: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                android.util.Log.e("AuthPresenter", "Error updating stats: ${t.message}")
            }
        })
    }
}
