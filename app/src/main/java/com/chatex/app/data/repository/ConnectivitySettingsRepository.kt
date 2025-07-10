package com.chatex.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.chatex.app.data.model.NetworkStatus
import com.chatex.app.data.model.PerformanceMode
import com.chatex.app.data.model.ConnectivitySettingsState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "connectivity_settings")

@Singleton
class ConnectivitySettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.settingsDataStore

    // مفاتيح التفضيلات
    private object PreferencesKeys {
        val IS_RELAY_ENABLED = booleanPreferencesKey("is_relay_enabled")
        val PERFORMANCE_MODE = stringPreferencesKey("performance_mode")
    }

    // تدفق لحالة الإعدادات المحلية
    val connectivitySettingsFlow: Flow<ConnectivitySettingsState> = dataStore.data
        .catch { exception ->
            // Handle data store exceptions
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // تعيين القيم الافتراضية إذا لم تكن موجودة
            val isRelayEnabled = preferences[PreferencesKeys.IS_RELAY_ENABLED] ?: true
            val performanceMode = try {
                PerformanceMode.valueOf(
                    preferences[PreferencesKeys.PERFORMANCE_MODE] ?: PerformanceMode.BALANCED.name
                )
            } catch (e: Exception) {
                PerformanceMode.BALANCED
            }

            ConnectivitySettingsState(
                isRelayEnabled = isRelayEnabled,
                performanceMode = performanceMode,
                // سيتم تحديث هذه القيم من ViewModel
                networkStatus = NetworkStatus.ONLINE,
                peerCount = 0
            )
        }

    // تحديث إعداد المشاركة في شبكة النقل
    suspend fun updateRelayEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_RELAY_ENABLED] = enabled
        }
        // TODO: مزامنة مع Supabase
    }

    // تحديث وضع الأداء
    suspend fun updatePerformanceMode(mode: PerformanceMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERFORMANCE_MODE] = mode.name
        }
        // TODO: مزامنة مع Supabase
    }

    // مزامنة الإعدادات مع Supabase
    suspend fun syncWithSupabase(userId: String) {
        try {
            // TODO: جلب الإعدادات من Supabase
            // إذا كانت هناك إعدادات أحدث على السيرفر، قم بتحديثها محليًا
            
            // ثم قم بمزامنة الإعدادات المحلية مع السيرفر
            val settings = dataStore.data.first()
            
            // TODO: تحديث Supabase بالإعدادات المحلية
            
        } catch (e: Exception) {
            // معالجة الأخطاء
            throw e
        }
    }
}

// ملحق لتحويل String إلى PerformanceMode
private fun String.toPerformanceMode(): PerformanceMode {
    return try {
        PerformanceMode.valueOf(this)
    } catch (e: IllegalArgumentException) {
        PerformanceMode.BALANCED
    }
}
