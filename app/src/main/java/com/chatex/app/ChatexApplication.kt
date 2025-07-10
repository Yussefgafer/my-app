package com.chatex.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Configuration
import androidx.work.WorkManager
import com.chatex.app.data.local.ChatDatabase
import com.chatex.app.data.local.datastore.ConnectivitySettingsDataStore
import com.chatex.app.data.sync.SettingsSyncManager
import com.chatex.app.util.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chatex_settings")

/**
 * Main Application class
 */
@HiltAndroidApp
class ChatexApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var settingsSyncManager: SettingsSyncManager

    @Inject
    lateinit var connectivitySettingsDataStore: ConnectivitySettingsDataStore
    
    @Inject
    lateinit var syncManager: SyncManager
    
    @Inject
    lateinit var chatDatabase: ChatDatabase

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Start settings sync if user is logged in
        // This will be called after Hilt is initialized
        startSettingsSync()
        
        // Initialize WorkManager
        WorkManager.initialize(this, workManagerConfiguration)
    }

    private fun startSettingsSync() {
        // TODO: Replace with actual user ID after authentication is implemented
        val userId = ""
        
        if (userId.isNotEmpty()) {
            settingsSyncManager.startSync(userId)
            syncManager.schedulePeriodicSync()
        }
    }
}
