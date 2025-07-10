package com.chatex.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chatex.app.data.model.UserProfile
import com.chatex.app.util.fromJson
import com.chatex.app.util.toJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    context: Context
) {
    private val dataStore = context.dataStore

    // Preference keys
    private val currentUserIdKey = stringPreferencesKey("current_user_id")
    private val userProfileKey = stringPreferencesKey("user_profile_")
    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    private val notificationEnabledKey = booleanPreferencesKey("notification_enabled")
    private val lastSyncTimestampKey = stringPreferencesKey("last_sync_timestamp")

    // Current User ID
    val currentUserId: Flow<String?> = dataStore.data
        .map { preferences -> preferences[currentUserIdKey] }

    // Is Dark Mode
    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[isDarkModeKey] ?: true } // Default to dark mode

    // Are Notifications Enabled
    val isNotificationEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[notificationEnabledKey] ?: true }

    // Last Sync Timestamp
    val lastSyncTimestamp: Flow<Long> = dataStore.data
        .map { preferences -> preferences[lastSyncTimestampKey]?.toLongOrNull() ?: 0L }

    /**
     * Save the current user ID
     */
    suspend fun setCurrentUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[currentUserIdKey] = userId
        }
    }

    /**
     * Save user profile to local storage
     */
    suspend fun saveUserProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            val profileJson = profile.toJson()
            preferences[stringPreferencesKey("${userProfileKey.name}${profile.id}")] = profileJson
            
            // Also update current user ID if it's not set
            if (preferences[currentUserIdKey] == null) {
                preferences[currentUserIdKey] = profile.id
            }
        }
    }

    /**
     * Get user profile by ID
     */
    fun getUserProfile(userId: String): Flow<UserProfile?> {
        return dataStore.data.map { preferences ->
            val profileJson = preferences[stringPreferencesKey("${userProfileKey.name}$userId")]
            profileJson?.fromJson<UserProfile>()
        }
    }

    /**
     * Get the current user's profile
     */
    fun getCurrentUserProfile(): Flow<UserProfile?> {
        return dataStore.data.map { preferences ->
            val userId = preferences[currentUserIdKey] ?: return@map null
            val profileJson = preferences[stringPreferencesKey("${userProfileKey.name}$userId")]
            profileJson?.fromJson<UserProfile>()
        }
    }

    /**
     * Update dark mode preference
     */
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[isDarkModeKey] = enabled
        }
    }

    /**
     * Update notification preference
     */
    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[notificationEnabledKey] = enabled
        }
    }

    /**
     * Update last sync timestamp
     */
    suspend fun updateLastSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            preferences[lastSyncTimestampKey] = timestamp.toString()
        }
    }

    /**
     * Clear all user-related data (on logout)
     */
    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            // Clear current user ID but keep other preferences
            preferences.remove(currentUserIdKey)
            
            // Clear all user profiles
            preferences.asMap().keys.forEach { key ->
                if (key.name.startsWith(userProfileKey.name)) {
                    preferences.remove(key)
                }
            }
        }
    }

    /**
     * Clear all preferences (for testing or complete reset)
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
