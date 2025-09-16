package com.dicoding.cataract_detection_app_final_project.presenter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AuthPresenter {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        // Listen for authentication state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            _isAuthenticated.value = user != null
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
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
                    _errorMessage.value = task.exception?.message ?: "Login failed"
                }
            }
    }
    
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }
        
        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match"
            return
        }
        
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    // Save user data to Firestore
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserData(user.uid, name, email)
                    }
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = task.exception?.message ?: "Registration failed"
                }
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
        
        firestore.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                // User data saved successfully
            }
            .addOnFailureListener {
                // Handle error
            }
    }
    
    fun logout() {
        auth.signOut()
        _errorMessage.value = null
    }
    
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your email"
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _errorMessage.value = "Password reset email sent"
                } else {
                    _errorMessage.value = task.exception?.message ?: "Failed to send reset email"
                }
            }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun getUserData(uid: String, callback: (Map<String, Any>?) -> Unit) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                callback(document.data)
            }
            .addOnFailureListener {
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
        
        firestore.collection("users")
            .document(uid)
            .update(updates)
            .addOnFailureListener {
                // Handle error
            }
    }
}

