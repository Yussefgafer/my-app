package com.chatex.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatex.app.R
import com.chatex.app.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onSignInSuccess: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    // Handle authentication state changes
    LaunchedEffect(uiState.isSignInSuccessful) {
        if (uiState.isSignInSuccessful) {
            onSignInSuccess()
            onNavigateToMain()
        }
    }
    
    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            if (uiState.authMode != AuthMode.LOGIN) {
                TopAppBar(
                    title = { 
                        Text(
                            text = when (uiState.authMode) {
                                AuthMode.REGISTER -> "Create Account"
                                AuthMode.FORGOT_PASSWORD -> "Reset Password"
                                else -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setAuthMode(AuthMode.LOGIN) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Show loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Logo
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(100.dp),
                    tint = Purple40
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Title
                Text(
                    text = when (uiState.authMode) {
                        AuthMode.LOGIN -> "Welcome Back"
                        AuthMode.REGISTER -> "Create Account"
                        AuthMode.FORGOT_PASSWORD -> "Reset Password"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Auth Form
                when (uiState.authMode) {
                    AuthMode.LOGIN -> LoginForm(
                        email = uiState.email,
                        password = uiState.password,
                        onEmailChange = { viewModel.updateEmail(it) },
                        onPasswordChange = { viewModel.updatePassword(it) },
                        onSignInClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.signIn()
                        },
                        onForgotPasswordClick = { viewModel.setAuthMode(AuthMode.FORGOT_PASSWORD) },
                        onSignUpClick = { viewModel.setAuthMode(AuthMode.REGISTER) },
                        isFormValid = uiState.isFormValid,
                        isLoading = uiState.isLoading
                    )
                    
                    AuthMode.REGISTER -> RegisterForm(
                        name = uiState.name,
                        email = uiState.email,
                        password = uiState.password,
                        confirmPassword = uiState.confirmPassword,
                        onNameChange = { viewModel.updateName(it) },
                        onEmailChange = { viewModel.updateEmail(it) },
                        onPasswordChange = { viewModel.updatePassword(it) },
                        onConfirmPasswordChange = { viewModel.updateConfirmPassword(it) },
                        onSignUpClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.register()
                        },
                        onSignInClick = { viewModel.setAuthMode(AuthMode.LOGIN) },
                        isFormValid = uiState.isFormValid,
                        isLoading = uiState.isLoading,
                        passwordMatchError = uiState.showPasswordMismatchError
                    )
                    
                    AuthMode.FORGOT_PASSWORD -> ForgotPasswordForm(
                        email = uiState.email,
                        onEmailChange = { viewModel.updateEmail(it) },
                        onResetClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.resetPassword()
                        },
                        onBackToLogin = { viewModel.setAuthMode(AuthMode.LOGIN) },
                        isEmailValid = uiState.isEmailValid,
                        isLoading = uiState.isLoading
                    )
                }
                
                // Social Login Options (only show on login screen)
                if (uiState.authMode == AuthMode.LOGIN) {
                    Spacer(modifier = Modifier.height(32.dp))
                    SocialLoginOptions(
                        onGoogleClick = { /* TODO */ },
                        onFacebookClick = { /* TODO */ },
                        onAppleClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}
