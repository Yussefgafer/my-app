package com.chatex.app.data.repository

import com.chatex.app.data.remote.SupabaseClient
import com.chatex.app.data.remote.dto.UserProfileDto
import com.chatex.app.util.Result
import io.github.jan.supabase.gotrue.user.UserInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface defining the authentication operations
 */
interface AuthRepository {
    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo>
    
    /**
     * Register a new user with email and password
     */
    suspend fun registerWithEmail(email: String, password: String, name: String): Result<UserInfo>
    
    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Get the current authenticated user
     */
    suspend fun getCurrentUser(): UserInfo?
    
    /**
     * Send a password reset email
     */
    suspend fun resetPassword(email: String): Result<Unit>
    
    /**
     * Check if a user is currently authenticated
     */
    suspend fun isUserAuthenticated(): Boolean
}

/**
 * Implementation of AuthRepository using Supabase
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {
    
    override suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            val result = supabaseClient.signInWithEmail(email, password)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sign in")
        }
    }
    
    override suspend fun registerWithEmail(email: String, password: String, name: String): Result<UserInfo> {
        return try {
            // First create the auth user
            val user = supabaseClient.signUpWithEmail(email, password)
            
            // Then create the user profile
            val profile = UserProfileDto(
                id = user.id,
                email = email,
                displayName = name,
                photoUrl = null,
                status = "Hey there! I'm using ChateX",
                lastSeen = System.currentTimeMillis()
            )
            
            supabaseClient.upsertUserProfile(profile)
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Registration failed")
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            supabaseClient.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sign out")
        }
    }
    
    override suspend fun getCurrentUser(): UserInfo? {
        return try {
            supabaseClient.getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabaseClient.resetPassword(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send password reset email")
        }
    }
    
    override suspend fun isUserAuthenticated(): Boolean {
        return try {
            supabaseClient.getCurrentUser() != null
        } catch (e: Exception) {
            false
        }
    }
}
