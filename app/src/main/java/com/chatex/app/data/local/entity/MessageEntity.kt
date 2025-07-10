package com.chatex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val type: MessageType = MessageType.TEXT,
    val attachmentUrl: String? = null,
    val localPath: String? = null,
    val isSynced: Boolean = false
) {
    enum class MessageType {
        TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, LOCATION
    }
}
