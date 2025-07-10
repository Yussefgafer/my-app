package com.chatex.app.data.model.auth

/**
 * حالة المصادقة الحالية للمستخدم
 */
sealed class AuthState {
    // لم يتم تحديد حالة المصادقة بعد
    object Loading : AuthState()
    
    // غير مسجل الدخول
    object Unauthenticated : AuthState()
    
    // مسجل الدخول بنجاح
    data class Authenticated(
        val userId: String,
        val email: String?,
        val isEmailVerified: Boolean = false,
        val displayName: String? = null,
        val photoUrl: String? = null
    ) : AuthState()
    
    // حالة خطأ
    data class Error(val error: Throwable) : AuthState()
}

/**
 * نموذج بيانات تسجيل الدخول
 */
data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

/**
 * نموذج بيانات تسجيل حساب جديد
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val displayName: String
)

/**
 * نموذج بيانات إعادة تعيين كلمة المرور
 */
data class ResetPasswordRequest(
    val email: String
)

/**
 * نموذج بيانات تغيير كلمة المرور
 */
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmNewPassword: String
)

/**
 * نموذج بيانات تحديث الملف الشخصي
 */
data class UpdateProfileRequest(
    val displayName: String? = null,
    val photoUrl: String? = null
)
