package com.chatex.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chatex.app.util.JsonSerializable
import java.util.*

/**
 * Represents a user profile in the application
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val status: UserStatus = UserStatus.ONLINE,
    val lastSeen: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val metadata: Map<String, String> = emptyMap()
) : JsonSerializable {
    /**
     * Creates a copy of the user profile with updated fields
     */
    fun update(
        displayName: String = this.displayName,
        photoUrl: String? = this.photoUrl,
        bio: String? = this.bio,
        status: UserStatus = this.status,
        preferences: UserPreferences = this.preferences,
        metadata: Map<String, String> = this.metadata
    ): UserProfile {
        return copy(
            displayName = displayName,
            photoUrl = photoUrl,
            bio = bio,
            status = status,
            preferences = preferences,
            metadata = metadata,
            updatedAt = System.currentTimeMillis()
        )
    }

    companion object {
        /**
         * Creates a new user profile with default values
         */
        fun create(
            id: String,
            email: String,
            displayName: String = email.substringBefore("@"),
            photoUrl: String? = null
        ): UserProfile {
            return UserProfile(
                id = id,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                preferences = UserPreferences()
            )
        }
    }
}

/**
 * User status options
 */
enum class UserStatus {
    ONLINE,
    OFFLINE,
    AWAY,
    BUSY,
    INVISIBLE
}

/**
 * User preferences and settings
 */
data class UserPreferences(
    val theme: ThemePreference = ThemePreference.DARK,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val privacySettings: PrivacySettings = PrivacySettings(),
    val chatSettings: ChatSettings = ChatSettings(),
    val mediaSettings: MediaSettings = MediaSettings()
) : JsonSerializable

/**
 * Theme preference options
 */
enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Notification settings
 */
data class NotificationSettings(
    val messageNotifications: Boolean = true,
    val groupNotifications: Boolean = true,
    val callNotifications: Boolean = true,
    val notificationSound: String = "default",
    val vibration: Boolean = true,
    val previewMessage: Boolean = true,
    val showNotificationContent: Boolean = true,
    val muteUntil: Long = 0L
) : JsonSerializable

/**
 * Privacy settings
 */
data class PrivacySettings(
    val lastSeen: PrivacyLevel = PrivacyLevel.EVERYONE,
    val profilePhoto: PrivacyLevel = PrivacyLevel.EVERYONE,
    val status: PrivacyLevel = PrivacyLevel.EVERYONE,
    val readReceipts: Boolean = true,
    val typingIndicators: Boolean = true,
    val blockedUsers: List<String> = emptyList()
) : JsonSerializable

/**
 * Chat settings
 */
data class ChatSettings(
    val enterToSend: Boolean = true,
    val emojiKeyboard: Boolean = true,
    val saveToGallery: Boolean = false,
    val fontSize: Int = 14,
    val wallpaper: String? = null,
    val autoDownloadMedia: Boolean = true
) : JsonSerializable

/**
 * Media settings
 */
data class MediaSettings(
    val imageQuality: ImageQuality = ImageQuality.HIGH,
    val videoQuality: VideoQuality = VideoQuality.MEDIUM,
    val autoPlayGifs: Boolean = true,
    val autoPlayVideos: Boolean = false,
    val saveToGallery: Boolean = true
) : JsonSerializable

/**
 * Image quality options
 */
enum class ImageQuality {
    LOW,    // Faster, lower quality
    MEDIUM, // Balanced
    HIGH,   // Slower, higher quality
    ORIGINAL
}

/**
 * Video quality options
 */
enum class VideoQuality {
    LOW,    // 144p-360p
    MEDIUM, // 480p-720p
    HIGH,   // 1080p
    HD,     // 1440p
    UHD     // 4K+
}

/**
 * Privacy level options
 */
enum class PrivacyLevel {
    EVERYONE,
    MY_CONTACTS,
    NOBODY
}

/**
 * Data Transfer Object for UserProfile
 */
data class UserProfileDto(
    val id: String,
    val email: String,
    val display_name: String,
    val photo_url: String? = null,
    val phone_number: String? = null,
    val bio: String? = null,
    val status: String = UserStatus.ONLINE.name,
    val last_seen: Long = System.currentTimeMillis(),
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis(),
    val is_email_verified: Boolean = false,
    val is_phone_verified: Boolean = false,
    val preferences: String = UserPreferences().toJson(),
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Convert domain model to DTO
         */
        fun fromDomain(profile: UserProfile): UserProfileDto {
            return UserProfileDto(
                id = profile.id,
                email = profile.email,
                display_name = profile.displayName,
                photo_url = profile.photoUrl,
                phone_number = profile.phoneNumber,
                bio = profile.bio,
                status = profile.status.name,
                last_seen = profile.lastSeen,
                created_at = profile.createdAt,
                updated_at = profile.updatedAt,
                is_email_verified = profile.isEmailVerified,
                is_phone_verified = profile.isPhoneVerified,
                preferences = profile.preferences.toJson(),
                metadata = profile.metadata
            )
        }
    }

    /**
     * Convert DTO to domain model
     */
    fun toDomain(): UserProfile {
        return UserProfile(
            id = id,
            email = email,
            displayName = display_name,
            photoUrl = photo_url,
            phoneNumber = phone_number,
            bio = bio,
            status = try {
                UserStatus.valueOf(status)
            } catch (e: IllegalArgumentException) {
                UserStatus.ONLINE
            },
            lastSeen = last_seen,
            createdAt = created_at,
            updatedAt = updated_at,
            isEmailVerified = is_email_verified,
            isPhoneVerified = is_phone_verified,
            preferences = preferences.fromJson(UserPreferences::class.java) ?: UserPreferences(),
            metadata = metadata
        )
    }
}
