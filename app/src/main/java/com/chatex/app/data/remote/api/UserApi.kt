package com.chatex.app.data.remote.api

import com.chatex.app.data.model.UserProfile
import com.chatex.app.data.remote.dto.UserProfileDto
import com.chatex.app.util.NetworkResult
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.postgrest.rpc
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface defining the API endpoints for user-related operations
 */
interface UserApi {
    /**
     * Get user profile by ID
     */
    suspend fun getUserProfile(userId: String): NetworkResult<UserProfile>

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(userId: String, profile: UserProfileDto): NetworkResult<UserProfile>

    /**
     * Update user's display name
     */
    suspend fun updateDisplayName(userId: String, displayName: String): NetworkResult<UserProfile>

    /**
     * Update user's profile photo
     */
    suspend fun updateProfilePhoto(userId: String, photoUrl: String): NetworkResult<UserProfile>

    /**
     * Search for users by name or email
     */
    suspend fun searchUsers(query: String): NetworkResult<List<UserProfile>>

    /**
     * Check if a username is available
     */
    suspend fun isUsernameAvailable(username: String): NetworkResult<Boolean>

    /**
     * Get multiple user profiles by IDs
     */
    suspend fun getUserProfiles(userIds: List<String>): NetworkResult<List<UserProfile>>

    /**
     * Update user's online status
     */
    suspend fun updateUserStatus(userId: String, isOnline: Boolean): NetworkResult<Unit>

    /**
     * Update user's FCM token for push notifications
     */
    suspend fun updateFcmToken(userId: String, token: String): NetworkResult<Unit>
}

/**
 * Implementation of UserApi using Supabase
 */
@Singleton
class SupabaseUserApi @Inject constructor(
    private val supabaseClient: io.github.jan.supabase.SupabaseClient
) : UserApi {

    private val userProfilesTable = "user_profiles"

    override suspend fun getUserProfile(userId: String): NetworkResult<UserProfile> {
        return try {
            val response = supabaseClient.postgrest[userProfilesTable]
                .select {
                    eq("id", userId)
                }
                .decodeSingle<UserProfileDto>()
            
            NetworkResult.Success(response.toDomain())
        } catch (e: Exception) {
            NetworkResult.Error("Failed to fetch user profile: ${e.message}", e)
        }
    }

    override suspend fun updateUserProfile(
        userId: String, 
        profile: UserProfileDto
    ): NetworkResult<UserProfile> {
        return try {
            val response = supabaseClient.postgrest[userProfilesTable]
                .upsert(profile) {
                    onConflict("id")
                    returning(Returning.MINIMAL)
                }
                .decodeSingle<UserProfileDto>()
            
            NetworkResult.Success(response.toDomain())
        } catch (e: Exception) {
            NetworkResult.Error("Failed to update user profile: ${e.message}", e)
        }
    }

    override suspend fun updateDisplayName(
        userId: String, 
        displayName: String
    ): NetworkResult<UserProfile> {
        return try {
            val response = supabaseClient.postgrest[userProfilesTable]
                .update(
                    {
                        set("display_name", displayName)
                        set("updated_at", System.currentTimeMillis())
                    }
                ) {
                    eq("id", userId)
                    select()
                }
                .decodeSingle<UserProfileDto>()
            
            NetworkResult.Success(response.toDomain())
        } catch (e: Exception) {
            NetworkResult.Error("Failed to update display name: ${e.message}", e)
        }
    }

    override suspend fun updateProfilePhoto(
        userId: String, 
        photoUrl: String
    ): NetworkResult<UserProfile> {
        return try {
            val response = supabaseClient.postgrest[userProfilesTable]
                .update(
                    {
                        set("photo_url", photoUrl)
                        set("updated_at", System.currentTimeMillis())
                    }
                ) {
                    eq("id", userId)
                    select()
                }
                .decodeSingle<UserProfileDto>()
            
            NetworkResult.Success(response.toDomain())
        } catch (e: Exception) {
            NetworkResult.Error("Failed to update profile photo: ${e.message}", e)
        }
    }

    override suspend fun searchUsers(query: String): NetworkResult<List<UserProfile>> {
        return try {
            val response = supabaseClient.postgrest.rpc(
                "search_users",
                mapOf("search_term" to query)
            )
            
            val results = response.decodeAs<List<UserProfileDto>>()
            NetworkResult.Success(results.map { it.toDomain() })
        } catch (e: Exception) {
            NetworkResult.Error("Failed to search users: ${e.message}", e)
        }
    }

    override suspend fun isUsernameAvailable(username: String): NetworkResult<Boolean> {
        return try {
            val count = supabaseClient.postgrest[userProfilesTable]
                .select("count") {
                    eq("username", username.lowercase())
                }
                .decodeSingle<Map<String, Long>>()
                .getOrElse("count") { 1L }
            
            NetworkResult.Success(count == 0L)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to check username availability: ${e.message}", e)
        }
    }

    override suspend fun getUserProfiles(userIds: List<String>): NetworkResult<List<UserProfile>> {
        return try {
            if (userIds.isEmpty()) {
                return NetworkResult.Success(emptyList())
            }
            
            val response = supabaseClient.postgrest[userProfilesTable]
                .select {
                    `in`("id", userIds)
                }
                .decodeAs<List<UserProfileDto>>()
            
            NetworkResult.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            NetworkResult.Error("Failed to fetch user profiles: ${e.message}", e)
        }
    }

    override suspend fun updateUserStatus(
        userId: String, 
        isOnline: Boolean
    ): NetworkResult<Unit> {
        return try {
            supabaseClient.postgrest[userProfilesTable]
                .update(
                    {
                        set("is_online", isOnline)
                        set("last_seen", if (!isOnline) System.currentTimeMillis() else null)
                    }
                ) {
                    eq("id", userId)
                }
            
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to update user status: ${e.message}", e)
        }
    }

    override suspend fun updateFcmToken(userId: String, token: String): NetworkResult<Unit> {
        return try {
            supabaseClient.postgrest[userProfilesTable]
                .update(
                    {
                        set("fcm_token", token)
                    }
                ) {
                    eq("id", userId)
                }
            
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error("Failed to update FCM token: ${e.message}", e)
        }
    }

    companion object {
        // Custom RPC function for searching users
        const val SEARCH_USERS_FUNCTION = """
            create or replace function search_users(search_term text)
            returns setof user_profiles as $$
            begin
                return query
                select *
                from user_profiles
                where
                    to_tsvector('english', display_name) @@ websearch_to_tsquery('english', search_term) or
                    to_tsvector('english', username) @@ websearch_to_tsquery('english', search_term) or
                    email ilike '%' || search_term || '%'
                order by
                    greatest(
                        ts_rank_cd(to_tsvector('english', display_name), websearch_to_tsquery('english', search_term)),
                        ts_rank_cd(to_tsvector('english', username), websearch_to_tsquery('english', search_term))
                    ) desc;
            end;
            $$ language plpgsql;
        """.trimIndent()
    }
}
