package com.dicoding.cataract_detection_app_final_project.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val LANGUAGE = stringPreferencesKey("language")
        
        // Theme modes
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
        
        // Languages
        const val LANG_ENGLISH = "en"
        const val LANG_INDONESIAN = "id"
    }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: THEME_SYSTEM
        }

    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE] ?: LANG_ENGLISH
        }

    suspend fun setThemeMode(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = theme
        }
        // Also update SharedPreferences for consistency
        val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_mode", theme).apply()
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
        // Also update SharedPreferences for attachBaseContext access
        val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        prefs.edit().putString("language", language).apply()
    }
    
    // Get theme mode synchronously (use carefully, only for initialization)
    fun getThemeModeSync(): String = runBlocking {
        try {
            context.dataStore.data.first()[THEME_MODE] ?: THEME_SYSTEM
        } catch (e: Exception) {
            THEME_SYSTEM
        }
    }
    
    // Get language synchronously (use carefully, only for initialization)
    fun getLanguageSync(): String = runBlocking {
        try {
            context.dataStore.data.first()[LANGUAGE] ?: LANG_ENGLISH
        } catch (e: Exception) {
            LANG_ENGLISH
        }
    }
    
    // Initialize DataStore from SharedPreferences if needed
    suspend fun initializeFromSharedPreferences() {
        val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val currentData = context.dataStore.data.first()
        
        // Sync language if not in DataStore but exists in SharedPreferences
        if (currentData[LANGUAGE] == null) {
            val language = prefs.getString("language", null)
            if (language != null) {
                context.dataStore.edit { preferences ->
                    preferences[LANGUAGE] = language
                }
            }
        }
        
        // Sync theme if not in DataStore but exists in SharedPreferences
        if (currentData[THEME_MODE] == null) {
            val theme = prefs.getString("theme_mode", null)
            if (theme != null) {
                context.dataStore.edit { preferences ->
                    preferences[THEME_MODE] = theme
                }
            }
        }
    }
}
