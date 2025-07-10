package com.chatex.app.data.repository

import com.chatex.app.data.local.datastore.AppPreferences
import com.chatex.app.data.manager.UserProfileManager
import com.chatex.app.data.model.UserProfile
import com.chatex.app.data.remote.api.UserApi
import com.chatex.app.util.NetworkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for handling user data operations.
 * Acts as a single source of truth for user-related data in the application.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val userProfileManager: UserProfileManager,
    private val appPreferences: AppPreferences,
    private val ioDispatcher: CoroutineDispatcher
) {
    // Current user profile state
    val currentUserProfile: Flow<UserProfile?> = userProfileManager.currentUserProfile
        .flowOn(ioDispatcher)

    // Loading state
    val isLoading: Flow<Boolean> = userProfileManager.isLoading
        .flowOn(ioDispatcher)

    // Error state
    val error: Flow<String?> = userProfileManager.error
        .flowOn(ioDispatcher)

    /**
     * Fetches the user profile from the network and updates the local cache.
     * @param userId The ID of the user to fetch.
     * @return A Flow of NetworkResult containing the user profile.
     */
    fun getUserProfile(userId: String): Flow<NetworkResult<UserProfile>> {
        return userProfileManager.fetchUserProfile(userId)
            .flowOn(ioDispatcher)
    }

    /**
     * Updates the user's display name.
     * @param userId The ID of the user to update.
     * @param displayName The new display name.
     * @return A NetworkResult indicating success or failure.
     */
    suspend fun updateDisplayName(userId: String, displayName: String): NetworkResult<UserProfile> {
        return withContext(ioDispatcher) {
            userProfileManager.updateDisplayName(userId, displayName)
        }
    }

    /**
     * Updates the user's profile photo URL.
     * @param userId The ID of the user to update.
     * @param photoUrl The new profile photo URL.
     * @return A NetworkResult indicating success or failure.
     */
    suspend fun updateProfilePhoto(userId: String, photoUrl: String): NetworkResult<UserProfile> {
        return withContext(ioDispatcher) {
            userProfileManager.updateProfilePhoto(userId, photoUrl)
        }
    }

    /**
     * Updates the user's online status.
     * @param userId The ID of the user to update.
     * @param isOnline Whether the user is online or offline.
     */
    suspend fun updateUserStatus(userId: String, isOnline: Boolean) {
        withContext(ioDispatcher) {
            userApi.updateUserStatus(userId, isOnline)
        }
    }

    /**
     * Updates the user's FCM token for push notifications.
     * @param userId The ID of the user to update.
     * @param token The new FCM token.
     */
    suspend fun updateFcmToken(userId: String, token: String) {
        withContext(ioDispatcher) {
            userApi.updateFcmToken(userId, token)
        }
    }

    /**
     * Searches for users by name or email.
     * @param query The search query.
     * @return A Flow of NetworkResult containing a list of matching user profiles.
     */
    fun searchUsers(query: String): Flow<NetworkResult<List<UserProfile>>> {
        return userApi.searchUsers(query)
            .map { result ->
                when (result) {
                    is NetworkResult.Success -> NetworkResult.Success(result.data)
                    is NetworkResult.Error -> NetworkResult.Error(result.message, result.throwable)
                    is NetworkResult.Loading -> NetworkResult.Loading
                }
            }
            .flowOn(ioDispatcher)
    }

    /**
     * Checks if a username is available.
     * @param username The username to check.
     * @return A Flow of NetworkResult containing a boolean indicating availability.
     */
    fun isUsernameAvailable(username: String): Flow<NetworkResult<Boolean>> {
        return userApi.isUsernameAvailable(username)
            .flowOn(ioDispatcher)
    }

    /**
     * Gets multiple user profiles by their IDs.
     * @param userIds The list of user IDs to fetch.
     * @return A Flow of NetworkResult containing a list of user profiles.
     */
    fun getUserProfiles(userIds: List<String>): Flow<NetworkResult<List<UserProfile>>> {
        return userApi.getUserProfiles(userIds)
            .flowOn(ioDispatcher)
    }

    /**
     * Clears all user data (on logout).
     */
    suspend fun clearUserData() {
        withContext(ioDispatcher) {
            userProfileManager.clearUserData()
        }
    }

    /**
     * Gets the current user ID from preferences.
     * @return The current user ID, or null if not logged in.
     */
    fun getCurrentUserId(): Flow<String?> {
        return appPreferences.currentUserId
            .flowOn(ioDispatcher)
    }

    /**
     * Gets the current user's profile.
     * @return A Flow of the current user's profile, or null if not logged in.
     */
    fun getCurrentUserProfile(): Flow<UserProfile?> {
        return currentUserProfile
    }

    /**
     * Refreshes the current user's profile from the network.
     * @return A NetworkResult indicating success or failure.
     */
    suspend fun refreshUserProfile(): NetworkResult<UserProfile> {
        return withContext(ioDispatcher) {
            val userId = appPreferences.currentUserId.first()
            if (userId != null) {
                userApi.getUserProfile(userId)
            } else {
                NetworkResult.Error("No user is currently logged in")
            }
        }
    }
}
