package com.dicoding.cataract_detection_app_final_project.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserSessionManager(private val context: Context) {

    companion object {
        val KEY_UID = stringPreferencesKey("uid")
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_CREATED_AT = stringPreferencesKey("created_at")
    }

    val userSession: Flow<UserSession?> = context.dataStore.data
        .map { preferences ->
            val uid = preferences[KEY_UID]
            val name = preferences[KEY_NAME]
            val email = preferences[KEY_EMAIL]
            val createdAt = preferences[KEY_CREATED_AT]

            if (uid != null && name != null && email != null) {
                UserSession(uid, name, email, createdAt)
            } else {
                null
            }
        }

    suspend fun saveUserSession(uid: String, name: String, email: String, createdAt: String?) {
        context.dataStore.edit { preferences ->
            preferences[KEY_UID] = uid
            preferences[KEY_NAME] = name
            preferences[KEY_EMAIL] = email
            if (createdAt != null) {
                preferences[KEY_CREATED_AT] = createdAt
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

data class UserSession(
    val uid: String,
    val name: String,
    val email: String,
    val createdAt: String? = null
)
