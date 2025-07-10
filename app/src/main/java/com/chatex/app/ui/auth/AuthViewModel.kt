package com.chatex.app.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatex.app.data.local.session.SessionManager
import com.chatex.app.data.model.auth.AuthResult
import com.chatex.app.data.model.auth.AuthState
import com.chatex.app.data.model.auth.ChangePasswordRequest
import com.chatex.app.data.model.auth.LoginRequest
import com.chatex.app.data.model.auth.RegisterRequest
import com.chatex.app.data.model.auth.ResetPasswordRequest
import com.chatex.app.data.model.auth.UpdateProfileRequest
import com.chatex.app.data.remote.auth.AuthRepository
import com.chatex.app.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing authentication state and interactions
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Session state
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        // Check current auth state on initialization
        checkAuthState()
        observeSessionChanges()
    }

    /**
     * Observe session changes and update UI state accordingly
     */
    private fun observeSessionChanges() {
        viewModelScope.launch {
            sessionManager.getAuthState().collect { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        _sessionState.value = SessionState.Authenticated(
                            userId = authState.userId,
                            email = authState.email,
                            isEmailVerified = authState.isEmailVerified
                        )
                        _uiState.value = AuthUiState.Authenticated(
                            userId = authState.userId,
                            email = authState.email,
                            displayName = authState.displayName ?: "",
                            isEmailVerified = authState.isEmailVerified
                        )
                    }
                    is AuthState.Unauthenticated -> {
                        _sessionState.value = SessionState.Unauthenticated
                        _uiState.value = AuthUiState.Unauthenticated
                    }
                    is AuthState.Loading -> {
                        _sessionState.value = SessionState.Loading
                        _uiState.value = AuthUiState.Loading
                    }
                    is AuthState.Error -> {
                        _sessionState.value = SessionState.Error(authState.error.message ?: "Authentication error")
                        _uiState.value = AuthUiState.Error(authState.error.message ?: "Authentication error")
                    }
                }
            }
        }
    }

    /**
     * Check the current authentication state
     */
    private fun checkAuthState() {
        viewModelScope.launch {
            _isLoading.value = true
            
            authRepository.getCurrentSession()
                .catch { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Unknown error occurred")
                    _isLoading.value = false
                }
                .collect { authState ->
                    _isLoading.value = false
                    
                    when (authState) {
                        is AuthState.Authenticated -> {
                            // Save session data
                            sessionManager.saveAuthData(
                                authToken = authState.accessToken ?: "",
                                refreshToken = authState.refreshToken ?: "",
                                userId = authState.userId,
                                email = authState.email
                            )
                            
                            _uiState.value = AuthUiState.Authenticated(
                                userId = authState.userId,
                                email = authState.email,
                                displayName = authState.displayName ?: "",
                                isEmailVerified = authState.isEmailVerified
                            )
                        }
                        is AuthState.Unauthenticated -> {
                            _uiState.value = AuthUiState.Unauthenticated
                        }
                        is AuthState.Error -> {
                            _uiState.value = AuthUiState.Error(authState.error.message ?: "Authentication failed")
                        }
                        is AuthState.Loading -> {
                            _uiState.value = AuthUiState.Loading
                        }
                    }
                }
        }
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validate password strength
     */
    private fun validatePassword(password: String): AuthResult {
        if (password.length < 8) {
            return AuthResult.Error("Password must be at least 8 characters long")
        }
        if (!password.any { it.isDigit() }) {
            return AuthResult.Error("Password must contain at least one digit")
        }
        if (!password.any { it.isLetter() }) {
            return AuthResult.Error("Password must contain at least one letter")
        }
        return AuthResult.Success
    }

    /**
     * Handle login with email and password
     */
    fun login(email: String, password: String, rememberMe: Boolean = false) {
        // Input validation
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password are required"
            return
        }

        if (!isValidEmail(email)) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            val request = LoginRequest(email, password, rememberMe)
            
            authRepository.login(request)
                .catch { e ->
                    _errorMessage.value = e.message ?: "Login failed. Please try again."
                    _isLoading.value = false
                }
                .collect { authState ->
                    _isLoading.value = false
                    
                    when (authState) {
                        is AuthState.Authenticated -> {
                            // Save session data
                            sessionManager.saveAuthData(
                                authToken = authState.accessToken ?: "",
                                refreshToken = authState.refreshToken ?: "",
                                userId = authState.userId,
                                email = authState.email
                            )
                            
                            _uiState.value = AuthUiState.Authenticated(
                                userId = authState.userId,
                                email = authState.email,
                                displayName = authState.displayName ?: "",
                                isEmailVerified = authState.isEmailVerified
                            )
                        }
                        is AuthState.Error -> {
                            _errorMessage.value = authState.error.message ?: "Login failed. Please try again."
                        }
                        else -> {}
                    }
                }
        }
    }

    /**
     * Register a new user
     */
    fun register(email: String, password: String, confirmPassword: String, displayName: String) {
        // Input validation
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || displayName.isBlank()) {
            _errorMessage.value = "All fields are required"
            return
        }

        if (!isValidEmail(email)) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }

        val passwordValidation = validatePassword(password)
        if (passwordValidation is AuthResult.Error) {
            _errorMessage.value = passwordValidation.errorMessage
            return
        }

        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match"
            return
        }

        if (displayName.length < 3) {
            _errorMessage.value = "Display name must be at least 3 characters"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            val request = RegisterRequest(email, password, confirmPassword, displayName)
            
            authRepository.register(request)
                .catch { e ->
                    _errorMessage.value = e.message ?: "Registration failed. Please try again."
                    _isLoading.value = false
                }
                .collect { authState ->
                    _isLoading.value = false
                    
                    when (authState) {
                        is AuthState.Authenticated -> {
                            // Save session data
                            sessionManager.saveAuthData(
                                authToken = authState.accessToken ?: "",
                                refreshToken = authState.refreshToken ?: "",
                                userId = authState.userId,
                                email = authState.email
                            )
                            
                            _uiState.value = AuthUiState.Authenticated(
                                userId = authState.userId,
                                email = authState.email,
                                displayName = displayName,
                                isEmailVerified = authState.isEmailVerified
                            )
                        }
                        is AuthState.Error -> {
                            _errorMessage.value = authState.error.message ?: "Registration failed. Please try again."
                        }
                        else -> {}
                    }
                }
        }
    }

    /**
     * Log out the current user
     */
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                authRepository.logout()
                // Clear session data
                sessionManager.clearSession()
                _uiState.value = AuthUiState.Unauthenticated
            } catch (e: Exception) {
                _errorMessage.value = "Failed to log out. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Initiate password reset for the given email
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Email is required"
            return
        }

        if (!isValidEmail(email)) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                authRepository.resetPassword(ResetPasswordRequest(email))
                _uiState.value = AuthUiState.PasswordResetEmailSent(email)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to send password reset email"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear any error messages
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return _sessionState.value is SessionState.Authenticated
    }

    /**
     * Get current user ID if authenticated
     */
    fun getCurrentUserId(): String? {
        return (_sessionState.value as? SessionState.Authenticated)?.userId
    }

    /**
     * Get current user email if authenticated
     */
    fun getCurrentUserEmail(): String? {
        return (_sessionState.value as? SessionState.Authenticated)?.email
    }

        _isLoading.value = true
        
        viewModelScope.launch {
            val request = ResetPasswordRequest(email)
            
            authRepository.resetPassword(request)
                .collect { success ->
                    _isLoading.value = false
                    
                    if (success) {
                        _uiState.value = AuthUiState.PasswordResetEmailSent(email)
                    } else {
                        _errorMessage.value = "فشل إرسال بريد إعادة تعيين كلمة المرور"
                    }
                }
        }
    }

    /**
     * مسح رسالة الخطأ
     */
    fun clearErrorMessage() {
}

/**
 * UI states for authentication
 */
sealed class AuthUiState {
    object Loading : AuthUiState()
    object Unauthenticated : AuthUiState()
    data class Authenticated(
        val userId: String,
        val email: String,
        val displayName: String,
        val isEmailVerified: Boolean = false,
        val profileImageUrl: String? = null
    ) : AuthUiState()

    data class PasswordResetEmailSent(val email: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

/**
 * Session state for the application
 */
sealed class SessionState {
    object Loading : SessionState()
    object Unauthenticated : SessionState()
    data class Authenticated(
        val userId: String,
        val email: String,
        val isEmailVerified: Boolean = false
    ) : SessionState()

    data class Error(val message: String) : SessionState()
}

/**
 * Authentication result for operations
 */
sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val errorMessage: String) : AuthResult()
}
