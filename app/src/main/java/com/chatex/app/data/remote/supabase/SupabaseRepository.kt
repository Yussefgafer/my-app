package com.chatex.app.data.remote.supabase

import com.chatex.app.data.model.supabase.SettingsChangeLog
import com.chatex.app.data.model.supabase.SyncStatus
import com.chatex.app.data.model.supabase.UserSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private companion object {
        const val USERS_TABLE = "users"
        const val SETTINGS_TABLE = "user_settings"
        const val CHANGE_LOGS_TABLE = "settings_change_logs"
    }

    /**
     * جلب إعدادات المستخدم من Supabase
     * @param userId معرف المستخدم
     */
    suspend fun getUserSettings(userId: String): UserSettings? {
        return try {
            supabase
                .from(SETTINGS_TABLE)
                .select {
                    eq("id", userId)
                }
                .decodeSingleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * تحديث إعدادات المستخدم في Supabase
     * @param userSettings إعدادات المستخدم الجديدة
     */
    suspend fun updateUserSettings(userSettings: UserSettings): UserSettings {
        return supabase
            .from(SETTINGS_TABLE)
            .upsert(userSettings, onConflict = "id") {
                select(Returning.MINIMAL)
            }
            .decodeSingle()
    }

    /**
     * الاشتراك في تحديثات إعدادات المستخدم
     * @param userId معرف المستخدم
     */
    fun subscribeToUserSettings(userId: String): Flow<UserSettings> = flow {
        supabase
            .from("$SETTINGS_TABLE:id=eq.$userId")
            .postgrest
            .channel()
            .postgresChangeFlow<UserSettings> { table = SETTINGS_TABLE }
            .collect { change ->
                change.record?.let { settings ->
                    emit(settings)
                }
            }
    }

    /**
     * تسجيل تغيير في الإعدادات في سجل التغييرات
     * @param userId معرف المستخدم
     * @param settingName اسم الإعداد
     * @param oldValue القيمة القديمة
     * @param newValue القيمة الجديدة
     */
    suspend fun logSettingsChange(
        userId: String,
        settingName: String,
        oldValue: String,
        newValue: String
    ) {
        val changeLog = SettingsChangeLog(
            id = UUID.randomUUID().toString(),
            userId = userId,
            settingName = settingName,
            oldValue = oldValue,
            newValue = newValue,
            syncStatus = SyncStatus.PENDING,
            timestamp = System.currentTimeMillis().toString()
        )

        supabase
            .from(CHANGE_LOGS_TABLE)
            .insert(changeLog)
    }

    /**
     * تحديث حالة مزامنة سجل التغيير
     * @param changeId معرف التغيير
     * @param status حالة المزامنة الجديدة
     */
    suspend fun updateChangeLogStatus(changeId: String, status: SyncStatus) {
        supabase
            .from(CHANGE_LOGS_TABLE)
            .update({
                set("sync_status", status.name)
            }) {
                eq("id", changeId)
            }
    }

    /**
     * جلب سجلات التغيير غير المزامنة
     * @param userId معرف المستخدم
     */
    suspend fun getUnsyncedChangeLogs(userId: String): List<SettingsChangeLog> {
        return supabase
            .from(CHANGE_LOGS_TABLE)
            .select {
                eq("user_id", userId)
                eq("sync_status", SyncStatus.PENDING.name)
            }
            .decodeList()
    }
}
