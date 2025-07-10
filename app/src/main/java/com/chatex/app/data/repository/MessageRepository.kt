package com.chatex.app.data.repository

import com.chatex.app.data.local.ChatDatabase
import com.chatex.app.data.local.entity.MessageEntity
import com.chatex.app.data.remote.SupabaseClient
import com.chatex.app.util.NetworkMonitor
import com.chatex.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val database: ChatDatabase,
    private val supabaseClient: SupabaseClient,
    private val networkMonitor: NetworkMonitor
) {
    private val messageDao = database.messageDao()

    fun getMessages(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessages(chatId)
    }

    suspend fun sendMessage(message: MessageEntity): Result<Unit> {
        return try {
            // Save message locally first
            messageDao.insertMessage(message)
            
            // Try to send to server if online
            if (networkMonitor.isOnline()) {
                supabaseClient.sendMessage(message).let { result ->
                    when (result) {
                        is Result.Success -> {
                            messageDao.markAsSynced(message.id)
                            Result.Success(Unit)
                        }
                        is Result.Error -> {
                            // Keep the message in local storage for later sync
                            Result.Error(result.message ?: "Failed to send message")
                        }
                    }
                }
            } else {
                // Will be synced when connection is restored
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun syncMessages() {
        if (!networkMonitor.isOnline()) return
        
        val unsyncedMessages = messageDao.getUnsyncedMessages()
        unsyncedMessages.forEach { message ->
            supabaseClient.sendMessage(message).let { result ->
                if (result is Result.Success) {
                    messageDao.markAsSynced(message.id)
                }
            }
        }
    }

    suspend fun fetchNewMessages(chatId: String) {
        if (!networkMonitor.isOnline()) return
        
        try {
            val lastMessage = messageDao.getMessages(chatId).map { messages -> 
                messages.maxByOrNull { it.timestamp }?.timestamp ?: 0L 
            }
            
            val newMessages = supabaseClient.fetchNewMessages(chatId, lastMessage)
            if (newMessages is Result.Success) {
                messageDao.insertMessages(newMessages.data)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
