package com.chatex.app.data.sync

import com.chatex.app.data.local.datastore.ConnectivitySettingsDataStore
import com.chatex.app.data.model.supabase.SettingsChangeLog
import com.chatex.app.data.model.supabase.SyncStatus
import com.chatex.app.data.model.supabase.UserSettings
import com.chatex.app.data.remote.supabase.SupabaseRepository
import com.chatex.app.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مدير مزامنة الإعدادات بين التخزين المحلي و Supabase
 * @property dataStore مخزن البيانات المحلي
 * @property supabaseRepository مستودع Supabase
 * @property networkMonitor مراقب حالة الاتصال
 * @property coroutineScope نطاق التزامن
 */
@Singleton
class SettingsSyncManager @Inject constructor(
    private val dataStore: ConnectivitySettingsDataStore,
    private val supabaseRepository: SupabaseRepository,
    private val networkMonitor: NetworkMonitor,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var isSyncing = false

    init {
        // بدء مراقبة التغييرات في الإعدادات المحلية
        startLocalSettingsObserver()
        
        // بدء مراقبة حالة الاتصال
        startNetworkObserver()
    }

    /**
     * بدء مزامنة الإعدادات
     * @param userId معرف المستخدم
     */
    fun startSync(userId: String) {
        if (isSyncing) return
        isSyncing = true

        coroutineScope.launch {
            try {
                // 1. المزامنة الأولية: جلب أحدث الإعدادات من السيرفر
                syncFromServer(userId)
                
                // 2. إرسال التغييرات المحلية غير المزامنة
                syncLocalChangesToServer(userId)
                
                // 3. الاشتراك في تحديثات الإعدادات من السيرفر
                subscribeToServerUpdates(userId)
                
            } catch (e: Exception) {
                Timber.e(e, "فشل في مزامنة الإعدادات")
            } finally {
                isSyncing = false
            }
        }
    }

    /**
     * مزامنة الإعدادات من السيرفر إلى التخزين المحلي
     */
    private suspend fun syncFromServer(userId: String) {
        try {
            val serverSettings = supabaseRepository.getUserSettings(userId)
            serverSettings?.let { settings ->
                // تحديث التخزين المحلي بالإعدادات من السيرفر
                dataStore.updateSettingsFromServer(
                    isRelayEnabled = settings.relayEnabled,
                    performanceMode = settings.performanceMode.name
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "فشل في جلب الإعدادات من السيرفر")
            throw e
        }
    }

    /**
     * مزامنة التغييرات المحلية مع السيرفر
     */
    private suspend fun syncLocalChangesToServer(userId: String) {
        try {
            // جلب الإعدادات المحلية
            val localSettings = dataStore.getSettings()
            
            // تحديث السيرفر بالإعدادات المحلية
            val updatedSettings = UserSettings(
                id = userId,
                relayEnabled = localSettings.isRelayEnabled,
                performanceMode = localSettings.performanceMode,
                lastSyncedAt = Date().toString()
            )
            
            supabaseRepository.updateUserSettings(updatedSettings)
            
            // تحديث حالة سجلات التغيير
            val unsyncedLogs = supabaseRepository.getUnsyncedChangeLogs(userId)
            unsyncedLogs.forEach { log ->
                supabaseRepository.updateChangeLogStatus(log.id, SyncStatus.SYNCED)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "فشل في مزامنة التغييرات المحلية مع السيرفر")
            throw e
        }
    }

    /**
     * الاشتراك في تحديثات الإعدادات من السيرفر
     */
    private fun subscribeToServerUpdates(userId: String) {
        coroutineScope.launch {
            supabaseRepository.subscribeToUserSettings(userId)
                .catch { e ->
                    Timber.e(e, "خطأ في الاشتراك بتحديثات الإعدادات")
                }
                .collect { settings ->
                    // تحديث التخزين المحلي بالتغييرات من السيرفر
                    dataStore.updateSettingsFromServer(
                        isRelayEnabled = settings.relayEnabled,
                        performanceMode = settings.performanceMode.name
                    )
                }
        }
    }

    /**
     * مراقبة التغييرات في الإعدادات المحلية
     */
    private fun startLocalSettingsObserver() {
        dataStore.settingsFlow
            .distinctUntilChanged()
            .onEach { settings ->
                // تسجيل التغييرات في سجل التغييرات
                logLocalChanges(settings)
                
                // محاولة مزامنة التغييرات مع السيرفر إذا كان متصلاً
                if (networkMonitor.isOnline.value) {
                    syncLocalChangesToServer(settings.userId)
                }
            }
            .launchIn(coroutineScope)
    }

    /**
     * مراقبة حالة الاتصال
     */
    private fun startNetworkObserver() {
        networkMonitor.isOnline
            .distinctUntilChanged()
            .onEach { isOnline ->
                if (isOnline) {
                    // محاولة المزامنة عند اتصال الإنترنت
                    dataStore.getCurrentUserId()?.let { userId ->
                        startSync(userId)
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    /**
     * تسجيل التغييرات المحلية في سجل التغييرات
     */
    private suspend fun logLocalChanges(settings: UserSettings) {
        val currentSettings = dataStore.getSettings()
        
        // تسجيل تغيير تفعيل المشاركة في شبكة النقل
        if (currentSettings.isRelayEnabled != settings.isRelayEnabled) {
            supabaseRepository.logSettingsChange(
                userId = settings.userId,
                settingName = "relay_enabled",
                oldValue = currentSettings.isRelayEnabled.toString(),
                newValue = settings.isRelayEnabled.toString()
            )
        }
        
        // تسجيل تغيير وضع الأداء
        if (currentSettings.performanceMode != settings.performanceMode) {
            supabaseRepository.logSettingsChange(
                userId = settings.userId,
                settingName = "performance_mode",
                oldValue = currentSettings.performanceMode.name,
                newValue = settings.performanceMode.name
            )
        }
    }
}
