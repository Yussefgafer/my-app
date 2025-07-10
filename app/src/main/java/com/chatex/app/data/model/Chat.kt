package com.chatex.app.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDateTime

data class Chat(
    val id: String,
    val userId: String,
    val userName: String,
    val lastMessage: String,
    val timestamp: LocalDateTime,
    val unreadCount: Int,
    val userAvatar: String,
    val isOnline: Boolean
)

// Sample data for preview and testing
val sampleChats = listOf(
    Chat(
        id = "1",
        userId = "user1",
        userName = "Ahmed Ali",
        lastMessage = "Hey, how are you doing?",
        timestamp = LocalDateTime.now().minusMinutes(30),
        unreadCount = 2,
        userAvatar = "https://randomuser.me/api/portraits/men/1.jpg",
        isOnline = true
    ),
    Chat(
        id = "2",
        userId = "user2",
        userName = "Sara Mohammed",
        lastMessage = "Let's meet tomorrow",
        timestamp = LocalDateTime.now().minusHours(2),
        unreadCount = 0,
        userAvatar = "https://randomuser.me/api/portraits/women/2.jpg",
        isOnline = false
    ),
    Chat(
        id = "3",
        userId = "user3",
        userName = "Omar Khalid",
        lastMessage = "Did you see the news?",
        timestamp = LocalDateTime.now().minusDays(1),
        unreadCount = 5,
        userAvatar = "https://randomuser.me/api/portraits/men/3.jpg",
        isOnline = true
    )
)
