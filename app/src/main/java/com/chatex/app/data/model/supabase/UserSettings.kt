package com.chatex.app.data.model.supabase

import kotlinx.serialization.Serializable

/**
 * نموذج إعدادات المستخدم في Supabase
 * @property id معرف المستخدم (UUID)
 * @property darkModeEnabled تفعيل الوضع الداكن
 * @property performanceMode وضع الأداء المحدد
 * @property relayEnabled تفعيل المشاركة في شبكة النقل
 * @property lastSyncedAt آخر مزامنة مع السيرفر
 */
@Serializable
data class UserSettings(
    val id: String = "",
    val darkModeEnabled: Boolean = true,
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    val relayEnabled: Boolean = true,
    val lastSyncedAt: String = ""
)

/**
 * وضع الأداء
 */
@Serializable
enum class PerformanceMode {
    POWER_SAVING, BALANCED, MAX_PERFORMANCE;

    companion object {
        fun fromString(value: String): PerformanceMode {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                BALANCED
            }
        }
    }
}

/**
 * حالة مزامنة الإعدادات
 */
@Serializable
enum class SyncStatus {
    SYNCED,        // تمت المزامنة بنجاح
    PENDING,       // في انتظار المزامنة
    ERROR,         // خطأ في المزامنة
    CONFLICT       // تعارض في التغييرات
}

/**
 * سجل التغييرات في الإعدادات
 * @property id المعرف الفريد للتغيير
 * @property userId معرف المستخدم
 * @property settingName اسم الإعداد
 * @property oldValue القيمة القديمة
 * @property newValue القيمة الجديدة
 * @property syncStatus حالة المزامنة
 * @property timestamp وقت التغيير
 */
@Serializable
data class SettingsChangeLog(
    val id: String = "",
    val userId: String = "",
    val settingName: String = "",
    val oldValue: String = "",
    val newValue: String = "",
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val timestamp: String = ""
)
