package com.chatex.app.util

import android.content.Context
import androidx.work.*
import com.chatex.app.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Schedule periodic sync for messages
     */
    fun schedulePeriodicSync() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES, // Flex interval
            15, TimeUnit.MINUTES  // Repeat interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "periodic_sync",
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Trigger an immediate sync for a specific chat
     */
    fun triggerSync(chatId: String) {
        val inputData = Data.Builder()
            .putString("chat_id", chatId)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueue(syncRequest)
    }

    /**
     * Cancel all pending sync operations
     */
    fun cancelAllSyncs() {
        workManager.cancelAllWork()
    }
}
