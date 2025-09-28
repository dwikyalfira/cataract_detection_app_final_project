package com.dicoding.cataract_detection_app_final_project.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dicoding.cataract_detection_app_final_project.data.AnalysisHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "analysis_history")

class HistoryRepository(private val context: Context) {
    
    private val gson = Gson()
    private val historyKey = stringPreferencesKey("analysis_history")
    
    fun getAnalysisHistory(userId: String): Flow<List<AnalysisHistory>> {
        return context.dataStore.data.map { preferences ->
            val historyJson = preferences[historyKey] ?: "[]"
            try {
                val type = object : TypeToken<List<AnalysisHistory>>() {}.type
                val allHistory: List<AnalysisHistory> = gson.fromJson(historyJson, type)
                allHistory.filter { it.userId == userId }
            } catch (e: Exception) {
                android.util.Log.w("HistoryRepository", "Error parsing history: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun saveAnalysisHistory(history: AnalysisHistory) {
        context.dataStore.edit { preferences ->
            val historyJson = preferences[historyKey] ?: "[]"
            try {
                val type = object : TypeToken<List<AnalysisHistory>>() {}.type
                val allHistory: MutableList<AnalysisHistory> = gson.fromJson(historyJson, type)
                
                val newHistory = history.copy(id = generateUniqueId())
                allHistory.add(newHistory)
                
                val userHistory = allHistory.filter { it.userId == history.userId }
                val otherUsersHistory = allHistory.filter { it.userId != history.userId }
                val recentUserHistory = userHistory.sortedByDescending { it.timestamp }.take(50)
                
                val updatedHistory = otherUsersHistory + recentUserHistory
                preferences[historyKey] = gson.toJson(updatedHistory)
                
                android.util.Log.d("HistoryRepository", "Saved analysis history: ${newHistory.id}")
            } catch (e: Exception) {
                android.util.Log.w("HistoryRepository", "Error saving history: ${e.message}")
            }
        }
    }
    
    suspend fun deleteAnalysisHistory(historyId: String, userId: String) {
        context.dataStore.edit { preferences ->
            val historyJson = preferences[historyKey] ?: "[]"
            try {
                val type = object : TypeToken<List<AnalysisHistory>>() {}.type
                val allHistory: MutableList<AnalysisHistory> = gson.fromJson(historyJson, type)
                
                val updatedHistory = allHistory.filter { !(it.id == historyId && it.userId == userId) }
                preferences[historyKey] = gson.toJson(updatedHistory)
                
                android.util.Log.d("HistoryRepository", "Deleted analysis history: $historyId")
            } catch (e: Exception) {
                android.util.Log.w("HistoryRepository", "Error deleting history: ${e.message}")
            }
        }
    }
    
    suspend fun clearAllHistory(userId: String) {
        context.dataStore.edit { preferences ->
            val historyJson = preferences[historyKey] ?: "[]"
            try {
                val type = object : TypeToken<List<AnalysisHistory>>() {}.type
                val allHistory: MutableList<AnalysisHistory> = gson.fromJson(historyJson, type)
                
                val updatedHistory = allHistory.filter { it.userId != userId }
                preferences[historyKey] = gson.toJson(updatedHistory)
                
                android.util.Log.d("HistoryRepository", "Cleared all history for user: $userId")
            } catch (e: Exception) {
                android.util.Log.w("HistoryRepository", "Error clearing history: ${e.message}")
            }
        }
    }
    
    private fun generateUniqueId(): String {
        return "history_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

