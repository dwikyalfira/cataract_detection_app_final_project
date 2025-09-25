package com.dicoding.cataract_detection_app_final_project.presenter

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dicoding.cataract_detection_app_final_project.utils.ErrorTranslator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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
            _errorMessage.value = ErrorTranslator.translateError(context, "Please fill in all fields")
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _errorMessage.value = null
                } else {
                    val errorMsg = task.exception?.message ?: "Login failed"
                    android.util.Log.d("AuthPresenter", "Firebase login error: '$errorMsg'")
                    _errorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                }
            }
    }
    
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _errorMessage.value = ErrorTranslator.translateError(context, "Please fill in all fields")
            return
        }
        
        if (password != confirmPassword) {
            _errorMessage.value = ErrorTranslator.translateError(context, "Passwords do not match")
            return
        }
        
        if (password.length < 6) {
            _errorMessage.value = ErrorTranslator.translateError(context, "Password must be at least 6 characters")
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null
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
                        _successMessage.value = ErrorTranslator.translateError(context, "Account created successfully! You will now be redirected to the login page.")
                    }
                    _errorMessage.value = null
                } else {
                    val errorMsg = task.exception?.message ?: "Registration failed"
                    android.util.Log.d("AuthPresenter", "Firebase register error: '$errorMsg'")
                    _errorMessage.value = ErrorTranslator.translateError(context, errorMsg)
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
            _errorMessage.value = ErrorTranslator.translateError(context, "Please enter your email")
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _successMessage.value = ErrorTranslator.translateError(context, "Password reset email sent")
                    _errorMessage.value = null
                } else {
                    val errorMsg = task.exception?.message ?: "Failed to send reset email"
                    _errorMessage.value = ErrorTranslator.translateError(context, errorMsg)
                    _successMessage.value = null
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
    
    fun clearRegistrationState() {
        _isRegistering.value = false
        // Update authentication state after clearing registration state
        _isAuthenticated.value = _isAuthenticatedInternal.value && !_isRegistering.value
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
    
    fun deleteAccount(currentPassword: String, callback: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, ErrorTranslator.translateError(context, "User not authenticated"))
            return
        }
        
        if (currentPassword.isBlank()) {
            callback(false, ErrorTranslator.translateError(context, "Please enter your current password"))
            return
        }
        
        _isLoading.value = true
        
        // Re-authenticate user with current password
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Delete user data from Firestore first
                    val uid = user.uid
                    firestore.collection("users").document(uid).delete()
                        .addOnCompleteListener { deleteDataTask ->
                            if (deleteDataTask.isSuccessful) {
                                // Delete user's analysis history
                                firestore.collection("analysis_history")
                                    .whereEqualTo("userId", uid)
                                    .get()
                                    .addOnCompleteListener { historyTask ->
                                        if (historyTask.isSuccessful) {
                                            val batch = firestore.batch()
                                            for (document in historyTask.result.documents) {
                                                batch.delete(document.reference)
                                            }
                                            batch.commit()
                                                .addOnCompleteListener { batchTask ->
                                                    // Delete Firebase Auth user
                                                    user.delete()
                                                        .addOnCompleteListener { deleteUserTask ->
                                                            _isLoading.value = false
                                                            if (deleteUserTask.isSuccessful) {
                                                                callback(true, ErrorTranslator.translateError(context, "Account deleted successfully"))
                                                            } else {
                                                                val errorMsg = deleteUserTask.exception?.message ?: "Failed to delete account"
                                                                callback(false, ErrorTranslator.translateError(context, errorMsg))
                                                            }
                                                        }
                                                }
                                        } else {
                                            // Even if history deletion fails, proceed with user deletion
                                            user.delete()
                                                .addOnCompleteListener { deleteUserTask ->
                                                    _isLoading.value = false
                                                    if (deleteUserTask.isSuccessful) {
                                                        callback(true, ErrorTranslator.translateError(context, "Account deleted successfully"))
                                                    } else {
                                                        val errorMsg = deleteUserTask.exception?.message ?: "Failed to delete account"
                                                        callback(false, ErrorTranslator.translateError(context, errorMsg))
                                                    }
                                                }
                                        }
                                    }
                            } else {
                                // Even if user data deletion fails, proceed with user deletion
                                user.delete()
                                    .addOnCompleteListener { deleteUserTask ->
                                        _isLoading.value = false
                                        if (deleteUserTask.isSuccessful) {
                                            callback(true, ErrorTranslator.translateError(context, "Account deleted successfully"))
                                        } else {
                                            val errorMsg = deleteUserTask.exception?.message ?: "Failed to delete account"
                                            callback(false, ErrorTranslator.translateError(context, errorMsg))
                                        }
                                    }
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
