package com.chatex.app.data.local.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chatex.app.data.model.auth.AuthState
import com.chatex.app.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    private val dataStore = context.dataStore

    // Keys
    private val authTokenKey = stringPreferencesKey("auth_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val lastLoginTimestampKey = stringPreferencesKey("last_login_timestamp")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    private val isFirstLaunchKey = booleanPreferencesKey("is_first_launch")

    // Auth Token
    val authToken: Flow<String?> = dataStore.data
        .map { preferences -> preferences[authTokenKey] }

    // Refresh Token
    val refreshToken: Flow<String?> = dataStore.data
        .map { preferences -> preferences[refreshTokenKey] }

    // User ID
    val userId: Flow<String?> = dataStore.data
        .map { preferences -> preferences[userIdKey] }

    // Is User Logged In
    val isLoggedIn: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[isLoggedInKey] ?: false }

    // User Email
    val userEmail: Flow<String?> = dataStore.data
        .map { preferences -> preferences[userEmailKey] }

    // Is First Launch
    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[isFirstLaunchKey] ?: true }

    // Is Dark Mode
    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[isDarkModeKey] ?: true } // Default to dark mode

    /**
     * Save authentication tokens and user data
     */
    suspend fun saveAuthData(
        authToken: String,
        refreshToken: String,
        userId: String,
        email: String
    ) {
        dataStore.edit { preferences ->
            preferences[authTokenKey] = authToken
            preferences[refreshTokenKey] = refreshToken
            preferences[userIdKey] = userId
            preferences[userEmailKey] = email
            preferences[isLoggedInKey] = true
            preferences[lastLoginTimestampKey] = System.currentTimeMillis().toString()
        }
    }

    /**
     * Clear all session data (logout)
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
            // Keep dark mode preference after logout
            val darkMode = preferences[isDarkModeKey] ?: true
            preferences[isDarkModeKey] = darkMode
        }
    }

    /**
     * Save dark mode preference
     */
    fun saveDarkModePreference(isDarkMode: Boolean) {
        applicationScope.launch {
            dataStore.edit { preferences ->
                preferences[isDarkModeKey] = isDarkMode
            }
        }
    }

    /**
     * Mark first launch as completed
     */
    fun setFirstLaunchCompleted() {
        applicationScope.launch {
            dataStore.edit { preferences ->
                preferences[isFirstLaunchKey] = false
            }
        }
    }

    /**
     * Get current auth state
     */
    fun getAuthState(): Flow<AuthState> = dataStore.data.map { preferences ->
        val isLoggedIn = preferences[isLoggedInKey] ?: false
        val userId = preferences[userIdKey]
        val email = preferences[userEmailKey]
        
        if (isLoggedIn && userId != null) {
            AuthState.Authenticated(
                userId = userId,
                email = email,
                isEmailVerified = true // You might want to store and check this as well
            )
        } else {
            AuthState.Unauthenticated
        }
    }

    companion object {
        // Singleton prevents multiple instances of DataStore being created
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context, applicationScope: CoroutineScope): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context, applicationScope)
                INSTANCE = instance
                instance
            }
        }
    }
}
