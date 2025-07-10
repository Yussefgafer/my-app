package com.chatex.app.presentation.auth

/**
 * Data class representing the UI state for authentication screens
 */
data class AuthUiState(
    val authMode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val isFormValid: Boolean = false,
    val isEmailValid: Boolean = false,
    val showPasswordMismatchError: Boolean = false,
    val error: String? = null
)

/**
 * Enum representing the different authentication modes
 */
enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}
