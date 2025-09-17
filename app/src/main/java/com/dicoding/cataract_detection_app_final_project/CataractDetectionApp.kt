package com.dicoding.cataract_detection_app_final_project

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.dicoding.cataract_detection_app_final_project.data.UserPreferences
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Application class that handles app-wide configuration including theming and localization.
 */

class CataractDetectionApp : Application() {
    override fun attachBaseContext(base: Context) {
        // Use SharedPreferences for initial locale setting since DataStore isn't available yet
        val prefs = base.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val language = prefs.getString("language", UserPreferences.LANG_INDONESIAN) ?: UserPreferences.LANG_INDONESIAN
        
        android.util.Log.d("CataractDetectionApp", "Language from preferences: $language")
        
        val locale = when (language) {
            UserPreferences.LANG_INDONESIAN -> Locale.forLanguageTag("id")
            else -> Locale.ENGLISH
        }
        
        android.util.Log.d("CataractDetectionApp", "Setting locale to: ${locale.language}")
        
        // Update system locale
        Locale.setDefault(locale)
        
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        
        val context = base.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Force set Indonesian as default language (always)
        val prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        prefs.edit().putString("language", UserPreferences.LANG_INDONESIAN).apply()
        android.util.Log.d("CataractDetectionApp", "Force set Indonesian as default language")
        
        // Check internet connectivity before initializing Firebase
        if (isNetworkAvailable()) {
            // Initialize Firebase
            try {
                FirebaseApp.initializeApp(this)
            } catch (e: Exception) {
                // Firebase might already be initialized or there might be an issue
                // Log the error but don't crash the app
                android.util.Log.w("CataractDetectionApp", "Firebase initialization issue: ${e.message}")
            }
        } else {
            android.util.Log.w("CataractDetectionApp", "No internet connection available")
        }
        
        // Set the default locale and theme from preferences
        val userPreferences = UserPreferences(this)
        
        // Initialize DataStore from SharedPreferences if needed
        runBlocking {
            userPreferences.initializeFromSharedPreferences()
            // Force set Indonesian as default
            userPreferences.forceSetIndonesian()
        }
        
        // Set theme mode
        val themeMode = userPreferences.getThemeModeSync()
        setThemeMode(themeMode)
        
        // Set locale using modern approach
        val language = userPreferences.getLanguageSync()
        setAppLocale(language)
    }
    
    private fun setAppLocale(language: String) {
        android.util.Log.d("CataractDetectionApp", "Setting app locale to: $language")
        
        val locale = when (language) {
            UserPreferences.LANG_INDONESIAN -> Locale.forLanguageTag("id")
            else -> Locale.ENGLISH
        }
        
        // Use modern AppCompatDelegate approach for Android 13+
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        android.util.Log.d("CataractDetectionApp", "App locale set to: ${locale.language}")
    }
    
    private fun setThemeMode(themeMode: String) {
        val mode = when (themeMode) {
            UserPreferences.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            UserPreferences.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        
        // Only update if the mode has changed
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
