package com.chatex.app.util

import com.chatex.app.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user session state and authentication
 */
@Singleton
class SessionManager @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val _authState = MutableStateFlow<Boolean>(false)
    val authState: StateFlow<Boolean> = _authState.asStateFlow()

    private var _currentUser: UserInfo? = null
    val currentUser: UserInfo? get() = _currentUser

    init {
        checkCurrentSession()
    }

    /**
     * Check if there's an active session
     */
    suspend fun checkCurrentSession() {
        try {
            _currentUser = supabaseClient.getCurrentUser()
            _authState.update { _currentUser != null }
        } catch (e: Exception) {
            _currentUser = null
            _authState.update { false }
        }
    }

    /**
     * Sign out the current user
     */
    suspend fun signOut() {
        try {
            supabaseClient.signOut()
            _currentUser = null
            _authState.update { false }
        } catch (e: Exception) {
            // Handle error
            throw e
        }
    }

    /**
     * Check if a user is currently authenticated
     */
    suspend fun isUserAuthenticated(): Boolean {
        return try {
            _currentUser = supabaseClient.getCurrentUser()
            _currentUser != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the current user's ID if authenticated
     */
    fun getCurrentUserId(): String? {
        return _currentUser?.id
    }

    /**
     * Update the current user information
     */
    fun updateCurrentUser(user: UserInfo) {
        _currentUser = user
        _authState.update { true }
    }
}
