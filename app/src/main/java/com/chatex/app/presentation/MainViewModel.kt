package com.chatex.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatex.app.data.local.datastore.AppPreferences
import com.chatex.app.data.model.UserProfile
import com.chatex.app.data.repository.UserRepository
import com.chatex.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the main app screen
 */
sealed class MainUiState {
    object Loading : MainUiState()
    data class Authenticated(val userProfile: UserProfile) : MainUiState()
    object Unauthenticated : MainUiState()
}

/**
 * ViewModel that handles the main app state and navigation
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Observe authentication state
        observeAuthState()
    }

    /**
     * Observe the authentication state and update the UI accordingly
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            sessionManager.authState.collect { isAuthenticated ->
                if (isAuthenticated) {
                    // User is authenticated, load their profile
                    loadUserProfile()
                } else {
                    // User is not authenticated
                    _uiState.value = MainUiState.Unauthenticated
                }
            }
        }
    }

    /**
     * Load the current user's profile
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            
            try {
                // Get the current user ID
                appPreferences.currentUserId.collect { userId ->
                    if (userId != null) {
                        // Fetch the user profile
                        userRepository.getUserProfile(userId).collect { result ->
                            when (result) {
                                is NetworkResult.Success -> {
                                    result.data?.let { profile ->
                                        _uiState.value = MainUiState.Authenticated(profile)
                                    } ?: run {
                                        // User not found, sign out
                                        signOut()
                                    }
                                }
                                is NetworkResult.Error -> {
                                    // Handle error, maybe show a message
                                    _uiState.value = MainUiState.Unauthenticated
                                    // Sign out on error to ensure clean state
                                    signOut()
                                }
                                is NetworkResult.Loading -> {
                                    // Show loading state
                                    _uiState.value = MainUiState.Loading
                                }
                            }
                        }
                    } else {
                        // No user ID found, sign out
                        signOut()
                    }
                }
            } catch (e: Exception) {
                // Handle any unexpected errors
                _uiState.value = MainUiState.Unauthenticated
                signOut()
            }
        }
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                // Clear the session
                sessionManager.signOut()
                // Clear the current user ID
                appPreferences.clearCurrentUserId()
                // Update UI state
                _uiState.value = MainUiState.Unauthenticated
            } catch (e: Exception) {
                // Even if sign out fails, we should still update the UI state
                _uiState.value = MainUiState.Unauthenticated
            }
        }
    }
    
    /**
     * Refresh the user profile data
     */
    fun refreshUserProfile() {
        viewModelScope.launch {
            try {
                val userId = appPreferences.getCurrentUserIdSync()
                if (userId != null) {
                    userRepository.refreshUserProfile(userId)
                }
            } catch (e: Exception) {
                // Handle error, maybe show a message
            }
        }
    }
    
    /**
     * Check if the user is authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return sessionManager.isUserAuthenticated()
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                // Clear user data and session
                userRepository.clearUserData()
                sessionManager.clearSession()
                _uiState.value = MainUiState.Unauthenticated
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Refresh the user's profile data
     */
    fun refreshUserProfile() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            
            // Get the current user ID
            val userId = appPreferences.currentUserId.first()
            if (userId != null) {
                when (val result = userRepository.refreshUserProfile()) {
                    is NetworkResult.Success -> {
                        result.data?.let { profile ->
                            _uiState.value = MainUiState.Authenticated(profile)
                        } ?: run {
                            // User not found, sign out
                            signOut()
                        }
                    }
                    is NetworkResult.Error -> {
                        // Show current state if available, otherwise go to unauthenticated
                        (_uiState.value as? MainUiState.Authenticated)?.let {
                            _uiState.value = it
                        } ?: run {
                            _uiState.value = MainUiState.Unauthenticated
                        }
                    }
                    is NetworkResult.Loading -> {
                        // Keep loading state
                        _uiState.value = MainUiState.Loading
                    }
                }
            } else {
                _uiState.value = MainUiState.Unauthenticated
            }
        }
    }
}
