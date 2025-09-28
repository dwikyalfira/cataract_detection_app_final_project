package com.dicoding.cataract_detection_app_final_project.presenter

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dicoding.cataract_detection_app_final_project.utils.ErrorTranslator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AuthPresenter(private var context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _isAuthenticated = MutableStateFlow(false)
    private val _isAuthenticatedInternal = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
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
        
        // Update Firebase language code
        val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "id") ?: "id"
        val firebaseLangCode = when (lang) {
            "id" -> "id"
            "en" -> "en"
            else -> "id"
        }
        auth.setLanguageCode(firebaseLangCode)
        android.util.Log.d("AuthPresenter", "Firebase language code updated to: $firebaseLangCode")
    }
    
    init {
        try {
            // Set Firebase language code based on user preferences
            val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            val lang = prefs.getString("language", "id") ?: "id"
            val firebaseLangCode = when (lang) {
                "id" -> "id"
                "en" -> "en"
                else -> "id"
            }
            auth.setLanguageCode(firebaseLangCode)
            android.util.Log.d("AuthPresenter", "Firebase language code set to: $firebaseLangCode")
            
            // Listen for authentication state changes
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                android.util.Log.d("AuthPresenter", "Auth state changed - user: ${user?.uid}, email: ${user?.email}")
                _currentUser.value = user
                _isAuthenticatedInternal.value = user != null
                // Only set authenticated to true if not registering
                _isAuthenticated.value = user != null && !_isRegistering.value
            }
        } catch (e: Exception) {
            // Handle Firebase initialization errors gracefully
            android.util.Log.w("AuthPresenter", "Firebase Auth initialization error: ${e.message}")
            _errorMessage.value = ErrorTranslator.translateError(context, "Authentication service unavailable")
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginErrorMessage.value = ErrorTranslator.translateError(context, "Please fill in all fields")
            return
        }
        
        _isLoading.value = true
        _loginErrorMessage.value = null
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _loginErrorMessage.value = null
                } else {
                    val errorMsg = task.exception?.message ?: "Login failed"
                    android.util.Log.d("AuthPresenter", "Firebase login error: '$errorMsg'")
                    _loginErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                }
            }
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
        // Update authentication state to prevent main app from showing
        _isAuthenticated.value = _isAuthenticatedInternal.value && !_isRegistering.value
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    // Save user data to Firestore
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserData(user.uid, name, email)
                        // Sign out user immediately after successful registration
                        auth.signOut()
                        // Set success message to inform user they need to login
                        _registerSuccessMessage.value = context.getString(com.dicoding.cataract_detection_app_final_project.R.string.success_account_created)
                    }
                    _registerErrorMessage.value = null
                } else {
                    val errorMsg = task.exception?.message ?: "Registration failed"
                    android.util.Log.d("AuthPresenter", "Firebase register error: '$errorMsg'")
                    _registerErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                }
                _isRegistering.value = false
                // Update authentication state after registration completes
                _isAuthenticated.value = _isAuthenticatedInternal.value && !_isRegistering.value
            }
    }
    
    private fun saveUserData(uid: String, name: String, email: String) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "createdAt" to System.currentTimeMillis(),
            "totalScans" to 0,
            "healthyScans" to 0,
            "alertScans" to 0
        )
        
        android.util.Log.d("AuthPresenter", "Saving user data for uid: $uid, data: $userData")
        android.util.Log.d("AuthPresenter", "Name being saved: $name")
        android.util.Log.d("AuthPresenter", "Email being saved: $email")
        
        try {
            firestore.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener {
                    android.util.Log.d("AuthPresenter", "User data saved successfully for uid: $uid")
                }
                .addOnFailureListener { exception ->
                    android.util.Log.w("AuthPresenter", "Failed to save user data: ${exception.message}")
                }
        } catch (e: Exception) {
            android.util.Log.w("AuthPresenter", "Firestore operation failed: ${e.message}")
        }
    }
    
    fun logout() {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            auth.signOut()
            // Sign out is synchronous, so we can set loading to false immediately
            _isLoading.value = false
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = ErrorTranslator.translateError(context, e.message ?: "Logout failed")
        }
    }
    
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _forgotPasswordErrorMessage.value = ErrorTranslator.translateError(context, "Please enter your email")
            return
        }
        
        _isLoading.value = true
        _forgotPasswordErrorMessage.value = null
        _forgotPasswordSuccessMessage.value = null
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _forgotPasswordSuccessMessage.value = ErrorTranslator.translateError(context, "Password reset email sent")
                    _forgotPasswordErrorMessage.value = null
                } else {
                    val errorMsg = task.exception?.message ?: "Failed to send reset email"
                    _forgotPasswordErrorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                    _forgotPasswordSuccessMessage.value = null
                }
            }
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
    
    fun deleteAccount(password: String, callback: (Boolean, String) -> Unit) {
        android.util.Log.d("AuthPresenter", "deleteAccount called with password length: ${password.length}")
        
        val user = auth.currentUser
        if (user == null) {
            android.util.Log.d("AuthPresenter", "No user logged in")
            callback(false, ErrorTranslator.translateError(context, "No user logged in"))
            return
        }
        
        if (password.isBlank()) {
            android.util.Log.d("AuthPresenter", "Password is blank")
            callback(false, ErrorTranslator.translateError(context, "Please enter your password"))
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null
        
        val userId = user.uid
        android.util.Log.d("AuthPresenter", "Starting account deletion for user: $userId")
        
        // First, re-authenticate the user with their password
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                android.util.Log.d("AuthPresenter", "Re-authentication result: ${reauthTask.isSuccessful}")
                if (reauthTask.isSuccessful) {
                    android.util.Log.d("AuthPresenter", "Password correct, proceeding with deletion")
                    // Password is correct, proceed with account deletion
                    // Skip Firestore deletion since it's causing hangs, go directly to auth deletion
                    android.util.Log.d("AuthPresenter", "Skipping Firestore deletion, proceeding directly to auth account deletion")
                    deleteAuthAccount(user, userId, callback)
                } else {
                    _isLoading.value = false
                    val errorMsg = reauthTask.exception?.message ?: "Incorrect password"
                    android.util.Log.e("AuthPresenter", "Re-authentication failed: $errorMsg")
                    _errorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                    _successMessage.value = null
                    callback(false, ErrorTranslator.translateError(context, "Incorrect password"))
                }
            }
    }
    
    private fun deleteAuthAccount(user: FirebaseUser, userId: String, callback: (Boolean, String) -> Unit) {
        // Delete the user account from Firebase Auth
        user.delete()
            .addOnCompleteListener { task ->
                _isLoading.value = false
                android.util.Log.d("AuthPresenter", "Auth account deletion result: ${task.isSuccessful}")
                if (task.isSuccessful) {
                    android.util.Log.d("AuthPresenter", "Account deleted successfully, clearing local data")
                    // Clear local user data and history
                    clearUserLocalData(userId)
                    
                    _successMessage.value = context.getString(com.dicoding.cataract_detection_app_final_project.R.string.account_deleted_successfully)
                    _errorMessage.value = null
                    android.util.Log.d("AuthPresenter", "Calling success callback")
                    callback(true, context.getString(com.dicoding.cataract_detection_app_final_project.R.string.account_deleted_successfully))
                    
                    // Sign out after a delay to allow snackbar to show
                    CoroutineScope(Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(2000) // 2 second delay
                        android.util.Log.d("AuthPresenter", "Signing out user after delay")
                        auth.signOut()
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Failed to delete account"
                    android.util.Log.e("AuthPresenter", "Failed to delete auth account: $errorMsg")
                    _errorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                    _successMessage.value = null
                    callback(false, ErrorTranslator.translateError(context, errorMsg))
                }
            }
    }
    
    private fun clearUserLocalData(userId: String) {
        try {
            // Clear analysis history
            CoroutineScope(Dispatchers.IO).launch {
                val historyRepository = com.dicoding.cataract_detection_app_final_project.repository.HistoryRepository(context)
                historyRepository.clearAllHistory(userId)
            }
            
            // Clear any cached images for this user
            CoroutineScope(Dispatchers.IO).launch {
                val imageStorageManager = com.dicoding.cataract_detection_app_final_project.utils.ImageStorageManager(context)
                imageStorageManager.clearUserImages(userId)
            }
            
            android.util.Log.d("AuthPresenter", "Cleared local data for user: $userId")
        } catch (e: Exception) {
            android.util.Log.w("AuthPresenter", "Error clearing local data: ${e.message}")
        }
    }
    
    fun getUserData(uid: String, callback: (Map<String, Any>?) -> Unit) {
        android.util.Log.d("AuthPresenter", "Getting user data for uid: $uid")
        try {
            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    android.util.Log.d("AuthPresenter", "User data retrieved: ${document.data}")
                    callback(document.data)
                }
                .addOnFailureListener { exception ->
                    android.util.Log.w("AuthPresenter", "Failed to get user data: ${exception.message}")
                    callback(null)
                }
        } catch (e: Exception) {
            android.util.Log.w("AuthPresenter", "Firestore operation failed: ${e.message}")
            callback(null)
        }
    }
    
    fun updateUserStats(uid: String, isHealthy: Boolean) {
        val updates = hashMapOf<String, Any>(
            "totalScans" to com.google.firebase.firestore.FieldValue.increment(1)
        )
        
        if (isHealthy) {
            updates["healthyScans"] = com.google.firebase.firestore.FieldValue.increment(1)
        } else {
            updates["alertScans"] = com.google.firebase.firestore.FieldValue.increment(1)
        }
        
        try {
            firestore.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener {
                    // Stats updated successfully
                }
                .addOnFailureListener { exception ->
                    android.util.Log.w("AuthPresenter", "Failed to update user stats: ${exception.message}")
                }
        } catch (e: Exception) {
            android.util.Log.w("AuthPresenter", "Firestore operation failed: ${e.message}")
        }
    }
    
    fun changePassword(currentPassword: String, newPassword: String, callback: (Boolean, String) -> Unit) {
        val user = auth.currentUser
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
        
        // Re-authenticate user with current password
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Update password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            _isLoading.value = false
                            if (updateTask.isSuccessful) {
                                callback(true, ErrorTranslator.translateError(context, "Password changed successfully"))
                            } else {
                                val errorMsg = updateTask.exception?.message ?: "Failed to change password"
                                callback(false, ErrorTranslator.translateError(context, errorMsg))
                            }
                        }
                } else {
                    _isLoading.value = false
                    val errorMsg = reauthTask.exception?.message ?: "Current password is incorrect"
                    callback(false, ErrorTranslator.translateError(context, errorMsg))
                }
            }
    }
    
    
}
