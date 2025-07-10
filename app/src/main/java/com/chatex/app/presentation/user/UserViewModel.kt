package com.chatex.app.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatex.app.data.model.UserProfile
import com.chatex.app.data.repository.UserRepository
import com.chatex.app.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the user profile screen
 */
data class UserProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false
)

/**
 * ViewModel for user-related operations
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        // Load the current user's profile when the ViewModel is created
        loadCurrentUserProfile()
    }

    /**
     * Load the current user's profile
     */
    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            // Show loading state
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get the current user ID
                val userId = userRepository.getCurrentUserId()
                    .collectLatest { userId ->
                        if (userId != null) {
                            // Fetch the user profile
                            userRepository.getUserProfile(userId).collect { result ->
                                when (result) {
                                    is NetworkResult.Success -> {
                                        _uiState.value = _uiState.value.copy(
                                            userProfile = result.data,
                                            isLoading = false,
                                            error = null
                                        )
                                    }
                                    is NetworkResult.Error -> {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            error = result.message ?: "Failed to load profile"
                                        )
                                    }
                                    is NetworkResult.Loading -> {
                                        _uiState.value = _uiState.value.copy(isLoading = true)
                                    }
                                }
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "No user is logged in"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unknown error occurred"
                )
            }
        }
    }

    /**
     * Update the user's display name
     */
    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            
            val currentProfile = _uiState.value.userProfile
            if (currentProfile != null) {
                when (val result = userRepository.updateDisplayName(currentProfile.id, displayName)) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            userProfile = currentProfile.copy(displayName = displayName),
                            isUpdating = false,
                            updateSuccess = true
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isUpdating = false,
                            error = result.message ?: "Failed to update display name"
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isUpdating = true)
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = "No user profile available"
                )
            }
        }
    }

    /**
     * Update the user's profile photo
     */
    fun updateProfilePhoto(photoUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            
            val currentProfile = _uiState.value.userProfile
            if (currentProfile != null) {
                when (val result = userRepository.updateProfilePhoto(currentProfile.id, photoUrl)) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            userProfile = currentProfile.copy(photoUrl = photoUrl),
                            isUpdating = false,
                            updateSuccess = true
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isUpdating = false,
                            error = result.message ?: "Failed to update profile photo"
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isUpdating = true)
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = "No user profile available"
                )
            }
        }
    }

    /**
     * Update the user's online status
     */
    fun updateUserStatus(isOnline: Boolean) {
        viewModelScope.launch {
            val currentProfile = _uiState.value.userProfile
            if (currentProfile != null) {
                userRepository.updateUserStatus(currentProfile.id, isOnline)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, updateSuccess = false)
    }

    /**
     * Refresh the user profile from the network
     */
    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = userRepository.refreshUserProfile()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        userProfile = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to refresh profile"
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
}
