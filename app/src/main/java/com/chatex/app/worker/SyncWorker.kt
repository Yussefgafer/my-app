package com.chatex.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chatex.app.data.repository.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageRepository: MessageRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Sync unsent messages
            messageRepository.syncMessages()
            
            // Fetch new messages for all active chats
            // You might want to pass chat IDs as input data
            val chatId = inputData.getString("chat_id")
            if (chatId != null) {
                messageRepository.fetchNewMessages(chatId)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
