package com.chatex.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatex.app.data.repository.AuthRepository
import com.chatex.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that handles authentication state and business logic
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Update the email in the UI state
     */
    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                isFormValid = when (_uiState.value.authMode) {
                    AuthMode.LOGIN -> email.isNotBlank() && _uiState.value.password.isNotBlank()
                    AuthMode.REGISTER -> email.isNotBlank() && _uiState.value.name.isNotBlank() && 
                                      _uiState.value.password.isNotBlank() && _uiState.value.confirmPassword.isNotBlank()
                    AuthMode.FORGOT_PASSWORD -> email.isNotBlank()
                }
            )
        }
        checkPasswordsMatch()
    }

    /**
     * Update the password in the UI state
     */
    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                isFormValid = when (_uiState.value.authMode) {
                    AuthMode.LOGIN -> _uiState.value.email.isNotBlank() && password.isNotBlank()
                    AuthMode.REGISTER -> _uiState.value.email.isNotBlank() && _uiState.value.name.isNotBlank() && 
                                       password.isNotBlank() && _uiState.value.confirmPassword.isNotBlank()
                    AuthMode.FORGOT_PASSWORD -> _uiState.value.email.isNotBlank()
                }
            )
        }
        checkPasswordsMatch()
    }

    /**
     * Update the confirm password in the UI state
     */
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmPassword = confirmPassword,
                isFormValid = _uiState.value.email.isNotBlank() && 
                             _uiState.value.name.isNotBlank() && 
                             _uiState.value.password.isNotBlank() && 
                             confirmPassword.isNotBlank()
            )
        }
        checkPasswordsMatch()
    }

    /**
     * Update the name in the UI state
     */
    fun updateName(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = name,
                isFormValid = _uiState.value.email.isNotBlank() && 
                             name.isNotBlank() && 
                             _uiState.value.password.isNotBlank() && 
                             _uiState.value.confirmPassword.isNotBlank()
            )
        }
    }

    /**
     * Check if passwords match and update the UI state accordingly
     */
    private fun checkPasswordsMatch() {
        val currentState = _uiState.value
        if (currentState.authMode == AuthMode.REGISTER) {
            val passwordsMatch = currentState.password == currentState.confirmPassword
            _uiState.update { it.copy(showPasswordMismatchError = !passwordsMatch) }
        }
    }

    /**
     * Set the authentication mode (login, register, forgot password)
     */
    fun setAuthMode(authMode: AuthMode) {
        _uiState.update { it.copy(authMode = authMode, error = null) }
        // Reset form validation when switching modes
        when (authMode) {
            AuthMode.LOGIN -> _uiState.update { it.copy(isFormValid = false) }
            AuthMode.REGISTER -> _uiState.update { it.copy(isFormValid = false) }
            AuthMode.FORGOT_PASSWORD -> _uiState.update { 
                it.copy(isFormValid = it.email.isNotBlank() && it.isEmailValid) 
            }
        }
    }

    /**
     * Handle sign in with email and password
     */
    fun signIn() {
        if (!_uiState.value.isFormValid) return
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(
                _uiState.value.email, 
                _uiState.value.password
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        isSignInSuccessful = true,
                        error = null
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message ?: "An unknown error occurred"
                    )}
                }
            }
        }
    }

    /**
     * Handle user registration
     */
    fun register() {
        if (!_uiState.value.isFormValid || _uiState.value.showPasswordMismatchError) return
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = authRepository.registerWithEmail(
                _uiState.value.email,
                _uiState.value.password,
                _uiState.value.name
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        isSignInSuccessful = true,
                        error = null
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message ?: "Registration failed. Please try again."
                    )}
                }
            }
        }
    }

    /**
     * Handle password reset
     */
    fun resetPassword() {
        if (!_uiState.value.isFormValid) return
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = authRepository.resetPassword(_uiState.value.email)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Password reset email sent. Please check your inbox.",
                        authMode = AuthMode.LOGIN
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to send password reset email. Please try again."
                    )}
                }
            }
        }
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
