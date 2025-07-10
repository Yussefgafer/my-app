package com.chatex.app.data.remote.auth

import com.chatex.app.data.model.auth.AuthState
import com.chatex.app.data.model.auth.ChangePasswordRequest
import com.chatex.app.data.model.auth.LoginRequest
import com.chatex.app.data.model.auth.RegisterRequest
import com.chatex.app.data.model.auth.ResetPasswordRequest
import com.chatex.app.data.model.auth.UpdateProfileRequest
import com.chatex.app.util.NetworkUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    /**
     * تسجيل الدخول باستخدام البريد الإلكتروني وكلمة المرور
     */
    suspend fun login(request: LoginRequest): Flow<AuthState> = NetworkUtils.withRetry {
        try {
            val result = supabase.auth.signInWith(Email) {
                email = request.email
                password = request.password
            }
            
            if (result != null) {
                mapUserToAuthState(result.user)
            } else {
                AuthState.Error(IllegalStateException("فشل تسجيل الدخول"))
            }
        } catch (e: Exception) {
            AuthState.Error(e)
        }
    }

    /**
     * تسجيل حساب جديد
     */
    suspend fun register(request: RegisterRequest): Flow<AuthState> = flow {
        try {
            // التحقق من تطابق كلمتي المرور
            if (request.password != request.confirmPassword) {
                emit(AuthState.Error(IllegalArgumentException("كلمتا المرور غير متطابقتين")))
                return@flow
            }
            
            // إنشاء الحساب
            val result = supabase.auth.signUp(
                email = request.email,
                password = request.password,
                data = mapOf("display_name" to request.displayName)
            )
            
            if (result != null) {
                emit(AuthState.Authenticated(
                    userId = result.user?.id ?: "",
                    email = result.user?.email,
                    isEmailVerified = result.user?.emailConfirmedAt != null,
                    displayName = request.displayName
                ))
            } else {
                emit(AuthState.Error(IllegalStateException("فشل إنشاء الحساب")))
            }
        } catch (e: Exception) {
            emit(AuthState.Error(e))
        }
    }

    /**
     * تسجيل الخروج
     */
    suspend fun logout(): Flow<Boolean> = flow {
        try {
            supabase.auth.signOut()
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    /**
     * إعادة تعيين كلمة المرور
     */
    suspend fun resetPassword(request: ResetPasswordRequest): Flow<Boolean> = flow {
        try {
            supabase.auth.resetPasswordForEmail(request.email)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    /**
     * تغيير كلمة المرور
     */
    suspend fun changePassword(request: ChangePasswordRequest): Flow<Boolean> = flow {
        try {
            if (request.newPassword != request.confirmNewPassword) {
                emit(false)
                return@flow
            }
            
            supabase.auth.updateUser {
                password = request.newPassword
            }
            
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    /**
     * تحديث الملف الشخصي
     */
    suspend fun updateProfile(request: UpdateProfileRequest): Flow<AuthState> = flow {
        try {
            val result = supabase.auth.updateUser {
                request.displayName?.let { displayName = it }
                request.photoUrl?.let { data = mapOf("photo_url" to it) }
            }
            
            emit(mapUserToAuthState(result))
        } catch (e: Exception) {
            emit(AuthState.Error(e))
        }
    }

    /**
     * جلب حالة المصادقة الحالية
     */
    fun getCurrentSession(): Flow<AuthState> = flow {
        try {
            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                val user = supabase.auth.retrieveUser()
                emit(mapUserToAuthState(user))
            } else {
                emit(AuthState.Unauthenticated)
            }
        } catch (e: Exception) {
            emit(AuthState.Error(e))
        }
    }

    /**
     * تحويل كائن المستخدم إلى حالة المصادقة
     */
    private fun mapUserToAuthState(user: UserInfo): AuthState {
        return AuthState.Authenticated(
            userId = user.id,
            email = user.email,
            isEmailVerified = user.emailConfirmedAt != null,
            displayName = user.userMetadata?.get("display_name") as? String ?: user.email?.substringBefore("@"),
            photoUrl = user.userMetadata?.get("photo_url") as? String
        )
    }
    
    /**
     * التحقق من صحة الجلسة الحالية
     */
    suspend fun validateSession(): Boolean {
        return try {
            supabase.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }
}
