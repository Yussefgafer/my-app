package com.chatex.app.data.repository

import com.chatex.app.data.local.ChatDatabase
import com.chatex.app.data.local.entity.MessageEntity
import com.chatex.app.data.remote.SupabaseClient
import com.chatex.app.util.ErrorHandler
import com.chatex.app.util.NetworkMonitor
import com.chatex.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val database: ChatDatabase,
    private val supabaseClient: SupabaseClient,
    private val networkMonitor: NetworkMonitor,
    private val errorHandler: ErrorHandler
) {
    private val messageDao = database.messageDao()

    fun getMessages(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessages(chatId)
    }

    suspend fun sendMessage(message: MessageEntity): Result<Unit> {
        return try {
            // Save message locally first with a temporary ID if needed
            val messageToSave = if (message.id.isEmpty()) {
                message.copy(id = "local_${UUID.randomUUID()}")
            } else {
                message
            }
            
            messageDao.insertMessage(messageToSave)
            
            // Try to send to server if online
            if (networkMonitor.isOnline()) {
                syncMessages()
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(errorHandler.getErrorMessage(e))
        }
    }
    
    /**
     * Sync all unsent messages with the server
     */
    suspend fun syncMessages(): Result<Unit> {
        return try {
            if (!networkMonitor.isOnline()) {
                return Result.Error("No network connection")
            }
            
            // Get all unsent messages
            val unsentMessages = messageDao.getUnsentMessages().first()
            
            // Try to send each message
            unsentMessages.forEach { message ->
                supabaseClient.sendMessage(message).onSuccess {
                    messageDao.markAsSynced(message.id)
                }.onFailure {
                    // Update last attempt time and increment retry count
                    messageDao.updateMessage(
                        message.copy(
                            lastSyncAttempt = System.currentTimeMillis(),
                            syncRetryCount = message.syncRetryCount + 1
                        )
                    )
                }
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(errorHandler.getErrorMessage(e))
        }
    }
    
    /**
     * Fetch new messages for a specific chat from the server
     */
    suspend fun fetchNewMessages(chatId: String): Result<Unit> {
        return try {
            if (!networkMonitor.isOnline()) {
                return Result.Error("No network connection")
            }
            
            // Get the timestamp of the most recent message for this chat
            val lastMessageTime = messageDao.getLastMessageTime(chatId).first() ?: 0L
            
            // Fetch new messages from the server
            val result = supabaseClient.getMessagesAfter(chatId, lastMessageTime)
            
            when (result) {
                is Result.Success -> {
                    // Save new messages to local database
                    result.data?.let { messages ->
                        messageDao.insertMessages(messages)
                    }
                    Result.Success(Unit)
                }
                is Result.Error -> {
                    Result.Error(result.message ?: "Failed to fetch new messages")
                }
            }
        } catch (e: Exception) {
            Result.Error(errorHandler.getErrorMessage(e))
        }
    }
