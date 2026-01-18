package com.dicoding.cataract_detection_app_final_project.utils

import android.content.Context
import com.dicoding.cataract_detection_app_final_project.R
import com.google.firebase.auth.FirebaseAuthException

object ErrorTranslator {
    
    /**
     * Translates Firebase authentication error messages to localized strings
     */
    fun translateAuthError(context: Context, errorMessage: String?): String {
        if (errorMessage == null) {
            return context.getString(R.string.error_login_failed)
        }
        
        // First try to extract Firebase error code from the message
        val firebaseErrorCode = extractFirebaseErrorCode(errorMessage)
        if (firebaseErrorCode != null) {
            val translatedError = translateFirebaseErrorCode(context, firebaseErrorCode)
            if (translatedError != null) {
                return translatedError
            }
        }
        
        return when {
            // Common Firebase Auth error codes
            errorMessage.contains("ERROR_INVALID_EMAIL") -> 
                context.getString(R.string.error_invalid_email)
            errorMessage.contains("ERROR_USER_NOT_FOUND") -> 
                context.getString(R.string.error_user_not_found)
            errorMessage.contains("ERROR_WRONG_PASSWORD") -> 
                context.getString(R.string.error_wrong_password)
            errorMessage.contains("ERROR_USER_DISABLED") -> 
                context.getString(R.string.error_user_disabled)
            errorMessage.contains("ERROR_TOO_MANY_REQUESTS") -> 
                context.getString(R.string.error_too_many_requests)
            errorMessage.contains("ERROR_NETWORK_REQUEST_FAILED") -> 
                context.getString(R.string.error_network_error)
            errorMessage.contains("ERROR_EMAIL_ALREADY_IN_USE") -> 
                context.getString(R.string.error_email_already_in_use)
            errorMessage.contains("ERROR_WEAK_PASSWORD") -> 
                context.getString(R.string.error_weak_password)
            
            // Additional Firebase error patterns
            errorMessage.contains("invalid-email") -> 
                context.getString(R.string.error_invalid_email)
            errorMessage.contains("user-not-found") -> 
                context.getString(R.string.error_user_not_found)
            errorMessage.contains("wrong-password") -> 
                context.getString(R.string.error_wrong_password)
            errorMessage.contains("user-disabled") -> 
                context.getString(R.string.error_user_disabled)
            errorMessage.contains("too-many-requests") -> 
                context.getString(R.string.error_too_many_requests)
            errorMessage.contains("network-request-failed") -> 
                context.getString(R.string.error_network_error)
            errorMessage.contains("email-already-in-use") -> 
                context.getString(R.string.error_email_already_in_use)
            errorMessage.contains("weak-password") -> 
                context.getString(R.string.error_weak_password)
            
            // Common error message patterns
            errorMessage.contains("network", ignoreCase = true) -> 
                context.getString(R.string.error_network_error)
            errorMessage.contains("password", ignoreCase = true) && 
            errorMessage.contains("incorrect", ignoreCase = true) -> 
                context.getString(R.string.error_wrong_password)
            errorMessage.contains("user not found", ignoreCase = true) -> 
                context.getString(R.string.error_user_not_found)
            errorMessage.contains("invalid email", ignoreCase = true) -> 
                context.getString(R.string.error_invalid_email)
            errorMessage.contains("too many attempts", ignoreCase = true) -> 
                context.getString(R.string.error_too_many_requests)
            errorMessage.contains("account disabled", ignoreCase = true) -> 
                context.getString(R.string.error_user_disabled)
            errorMessage.contains("email already in use", ignoreCase = true) -> 
                context.getString(R.string.error_email_already_in_use)
            errorMessage.contains("weak password", ignoreCase = true) -> 
                context.getString(R.string.error_weak_password)
            
            // Additional common patterns
            errorMessage.contains("authentication failed", ignoreCase = true) -> 
                context.getString(R.string.error_login_failed)
            errorMessage.contains("sign in failed", ignoreCase = true) -> 
                context.getString(R.string.error_login_failed)
            errorMessage.contains("create user failed", ignoreCase = true) -> 
                context.getString(R.string.error_registration_failed)
            errorMessage.contains("registration failed", ignoreCase = true) -> 
                context.getString(R.string.error_registration_failed)
            errorMessage.contains("connection", ignoreCase = true) -> 
                context.getString(R.string.error_network_error)
            errorMessage.contains("timeout", ignoreCase = true) -> 
                context.getString(R.string.error_network_error)
            
            // Firebase specific error messages
            errorMessage.contains("The email address is badly formatted", ignoreCase = true) -> 
                context.getString(R.string.error_invalid_email)
            errorMessage.contains("email address is badly formatted", ignoreCase = true) -> 
                context.getString(R.string.error_invalid_email)
            errorMessage.contains("badly formatted", ignoreCase = true) -> 
                context.getString(R.string.error_invalid_email)
            errorMessage.contains("The supplied auth credential is incorrect", ignoreCase = true) -> 
                context.getString(R.string.error_wrong_password)
            errorMessage.contains("supplied auth credential is incorrect", ignoreCase = true) -> 
                context.getString(R.string.error_wrong_password)
            errorMessage.contains("auth credential is incorrect", ignoreCase = true) -> 
                context.getString(R.string.error_wrong_password)
            errorMessage.contains("credential is incorrect", ignoreCase = true) -> 
                context.getString(R.string.error_wrong_password)
            errorMessage.contains("malformed or has expired", ignoreCase = true) -> 
                context.getString(R.string.error_wrong_password)
            errorMessage.contains("has expired", ignoreCase = true) -> 
                context.getString(R.string.error_wrong_password)
            
            // Default fallback
            else -> errorMessage
        }
    }
    
    /**
     * Translates custom error messages to localized strings
     */
    fun translateCustomError(context: Context, errorMessage: String): String {
        return when (errorMessage) {
            "Please fill in all fields" -> 
                context.getString(R.string.error_fill_all_fields)
            "Passwords do not match" -> 
                context.getString(R.string.error_passwords_do_not_match)
            "Password must be at least 6 characters" -> 
                context.getString(R.string.error_password_too_short)
            "Please enter your email" -> 
                context.getString(R.string.error_enter_email)
            "Authentication service unavailable" -> 
                context.getString(R.string.error_authentication_service_unavailable)
            "Login failed" -> 
                context.getString(R.string.error_login_failed)
            "Registration failed" -> 
                context.getString(R.string.error_registration_failed)
            "Failed to send reset email" -> 
                context.getString(R.string.error_reset_email_failed)
            "Logout failed" -> 
                context.getString(R.string.error_logout_failed)
            "Password reset email sent" -> 
                context.getString(R.string.success_password_reset_sent)
            "Invalid password" -> 
                context.getString(R.string.error_wrong_password)
            "Password changed successfully" ->
                context.getString(R.string.success_password_changed)
            "Failed to change password" ->
                context.getString(R.string.error_change_password_failed)
            "Failed to reset password" ->
                context.getString(R.string.error_reset_password_failed)
            "Account deleted successfully" ->
                context.getString(R.string.account_deleted_successfully)
            else -> errorMessage
        }
    }
    
    /**
     * Main function to translate any error message
     */
    fun translateError(context: Context, errorMessage: String?): String {
        if (errorMessage == null) {
            return context.getString(R.string.error_login_failed)
        }
        
        // Debug logging
        android.util.Log.d("ErrorTranslator", "Original error: '$errorMessage'")
        
        // Ensure we have a properly localized context
        val localizedContext = getLocalizedContext(context)
        
        // First try custom error translation
        val customTranslation = translateCustomError(localizedContext, errorMessage)
        if (customTranslation != errorMessage) {
            android.util.Log.d("ErrorTranslator", "Custom translation: '$customTranslation'")
            return customTranslation
        }
        
        // Then try Firebase auth error translation
        val authTranslation = translateAuthError(localizedContext, errorMessage)
        android.util.Log.d("ErrorTranslator", "Auth translation: '$authTranslation'")
        return authTranslation
    }
    
    /**
     * Ensures the context is properly localized
     */
    private fun getLocalizedContext(context: Context): Context {
        // Get the current locale from the context
        val currentLocale = context.resources.configuration.locales[0]
        
        // If the locale is already Indonesian or English, return the context as is
        if (currentLocale.language == "id" || currentLocale.language == "en") {
            return context
        }
        
        // Otherwise, try to get the locale from the system preferences
        val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "id") ?: "id"
        
        val locale = when (lang) {
            "id" -> java.util.Locale.forLanguageTag("id")
            "en" -> java.util.Locale.ENGLISH
            else -> java.util.Locale.forLanguageTag("id")
        }
        
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    /**
     * Extracts Firebase error code from error message
     */
    private fun extractFirebaseErrorCode(errorMessage: String): String? {
        // Common Firebase error code patterns
        val patterns = listOf(
            "ERROR_INVALID_EMAIL",
            "ERROR_USER_NOT_FOUND", 
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_DISABLED",
            "ERROR_TOO_MANY_REQUESTS",
            "ERROR_NETWORK_REQUEST_FAILED",
            "ERROR_EMAIL_ALREADY_IN_USE",
            "ERROR_WEAK_PASSWORD"
        )
        
        for (pattern in patterns) {
            if (errorMessage.contains(pattern)) {
                return pattern
            }
        }
        
        return null
    }
    
    /**
     * Translates Firebase error code to localized string
     */
    private fun translateFirebaseErrorCode(context: Context, errorCode: String): String? {
        return when (errorCode) {
            "ERROR_INVALID_EMAIL" -> context.getString(R.string.error_invalid_email)
            "ERROR_USER_NOT_FOUND" -> context.getString(R.string.error_user_not_found)
            "ERROR_WRONG_PASSWORD" -> context.getString(R.string.error_wrong_password)
            "ERROR_USER_DISABLED" -> context.getString(R.string.error_user_disabled)
            "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.error_too_many_requests)
            "ERROR_NETWORK_REQUEST_FAILED" -> context.getString(R.string.error_network_error)
            "ERROR_EMAIL_ALREADY_IN_USE" -> context.getString(R.string.error_email_already_in_use)
            "ERROR_WEAK_PASSWORD" -> context.getString(R.string.error_weak_password)
            else -> null
        }
    }
}

