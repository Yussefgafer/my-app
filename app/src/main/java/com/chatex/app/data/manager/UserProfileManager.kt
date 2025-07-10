package com.chatex.app.data.manager

import com.chatex.app.data.local.datastore.AppPreferences
import com.chatex.app.data.model.UserProfile
import com.chatex.app.data.remote.api.UserApi
import com.chatex.app.data.remote.dto.UserProfileDto
import com.chatex.app.util.NetworkResult
import com.chatex.app.util.networkBoundResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user profile data, handling both local and remote data sources
 */
@Singleton
class UserProfileManager @Inject constructor(
    private val userApi: UserApi,
    private val appPreferences: AppPreferences,
    private val coroutineScope: CoroutineScope
) {
    // Current user profile state
    private val _currentUserProfile = mutableStateOf<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    // Profile loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Load cached profile on initialization
        loadCachedProfile()
    }

    /**
     * Fetch user profile from network and update cache
     */
    fun fetchUserProfile(userId: String): Flow<NetworkResult<UserProfile>> {
        return networkBoundResource(
            query = {
                // First emit the cached profile if available
                appPreferences.getUserProfile(userId).map { cachedProfile ->
                    cachedProfile?.let { NetworkResult.Success(it) } 
                        ?: NetworkResult.Error("No cached profile")
                }
            },
            fetch = {
                // Fetch from network
                userApi.getUserProfile(userId)
            },
            saveFetchResult = { response ->
                // Save to cache on successful fetch
                if (response is NetworkResult.Success) {
                    saveProfileToCache(response.data)
                }
            },
            onFetchFailed = { error ->
                _error.value = error?.message ?: "Failed to fetch profile"
            }
        )
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(profile: UserProfile): NetworkResult<UserProfile> {
        return try {
            _isLoading.value = true
            
            // Update on server
            val response = userApi.updateUserProfile(
                userId = profile.id,
                profile = UserProfileDto.fromDomain(profile)
            )
            
            if (response is NetworkResult.Success) {
                // Update local cache
                saveProfileToCache(response.data)
            }
            
            _isLoading.value = false
            response
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = e.message ?: "Failed to update profile"
            NetworkResult.Error(e.message ?: "Failed to update profile")
        }
    }

    /**
     * Update user's display name
     */
    suspend fun updateDisplayName(userId: String, displayName: String): NetworkResult<UserProfile> {
        return try {
            _isLoading.value = true
            
            val currentProfile = _currentUserProfile.value?.copy(displayName = displayName)
                ?: return NetworkResult.Error("No profile found")
                
            updateUserProfile(currentProfile)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Update user's profile photo
     */
    suspend fun updateProfilePhoto(userId: String, photoUrl: String): NetworkResult<UserProfile> {
        return try {
            _isLoading.value = true
            
            val currentProfile = _currentUserProfile.value?.copy(photoUrl = photoUrl)
                ?: return NetworkResult.Error("No profile found")
                
            updateUserProfile(currentProfile)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Clear user profile data (on logout)
     */
    fun clearUserData() {
        coroutineScope.launch {
            appPreferences.clearUserData()
            _currentUserProfile.value = null
        }
    }

    /**
     * Load cached profile from local storage
     */
    private fun loadCachedProfile() {
        coroutineScope.launch {
            appPreferences.getCurrentUserProfile().collect { profile ->
                _currentUserProfile.value = profile
            }
        }
    }

    /**
     * Save profile to cache
     */
    private suspend fun saveProfileToCache(profile: UserProfile) {
        appPreferences.saveUserProfile(profile)
        _currentUserProfile.value = profile
    }

    companion object {
        // Singleton instance
        @Volatile
        private var INSTANCE: UserProfileManager? = null

        fun getInstance(
            userApi: UserApi,
            appPreferences: AppPreferences,
            coroutineScope: CoroutineScope
        ): UserProfileManager {
            return INSTANCE ?: synchronized(this) {
                val instance = UserProfileManager(userApi, appPreferences, coroutineScope)
                INSTANCE = instance
                instance
            }
        }
    }
}
