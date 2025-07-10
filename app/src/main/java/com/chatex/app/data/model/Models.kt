package com.chatex.app.data.model

import java.time.LocalDateTime

/**
 * نموذج يمثل رسالة في المحادثة
 * @property id معرف فريد للرسالة
 * @property content محتوى الرسالة النصي
 * @property timestamp وقت إرسال الرسالة
 * @property isSentByUser `true` إذا كانت مرسلة من المستخدم الحالي، `false` إذا كانت واردة
 * @property attachment مرفق الرسالة (اختياري)
 */
data class Message(
    val id: String,
    val content: String,
    val timestamp: LocalDateTime,
    val isSentByUser: Boolean,
    val attachment: Attachment? = null
)

/**
 * نموذج يمثل مرفق في الرسالة
 * @property fileName اسم الملف
 * @property fileSize حجم الملف (مثال: "2.5 MB")
 * @property fileIcon أيقونة تمثل نوع الملف (يتم استخدامها من الموارد)
 */
data class Attachment(
    val fileName: String,
    val fileSize: String,
    val fileIcon: Int // سوف نستخدم R.drawable.ic_file_*
)

/**
 * حالة واجهة شاشة المحادثة
 * @property messages قائمة الرسائل
 * @property isLoading حالة التحميل
 * @property errorMsg رسالة الخطأ (في حالة حدوث خطأ)
 * @property recipientName اسم المستلم
 * @property isRecipientOnline حالة اتصال المستلم
 */
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val recipientName: String = "Larry Mochigo",
    val isRecipientOnline: Boolean = true
)

/**
 * حالة واجهة شاشة المكالمة الصوتية
 * @property callerName اسم المتصل
 * @property callerAvatarUrl رابط صورة المتصل
 * @property callDuration مدة المكالمة بتنسيق "mm:ss"
 * @property isMuted حالة كتم الصوت
 * @property isSpeakerOn حالة تشغيل مكبر الصوت
 */
data class CallUiState(
    val callerName: String = "Justin Austin",
    val callerAvatarUrl: String = "https://randomuser.me/api/portraits/men/1.jpg",
    val callDuration: String = "00:00",
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false
)

/**
 * حالة إعدادات الاتصال
 * @property isRelayEnabled تفعيل المشاركة في شبكة النقل
 * @property performanceMode وضع الأداء المحدد
 * @property networkStatus حالة الاتصال الحالية
 * @property peerCount عدد الأجهزة المتصلة
 */
data class ConnectivitySettingsState(
    val isRelayEnabled: Boolean = true,
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    val networkStatus: NetworkStatus = NetworkStatus.ONLINE,
    val peerCount: Int = 0
)

/**
 * وضع الأداء
 */
enum class PerformanceMode {
    POWER_SAVING, BALANCED, MAX_PERFORMANCE
}

/**
 * حالة الاتصال بالشبكة
 */
enum class NetworkStatus {
    ONLINE, RELAY, SEARCHING, OFFLINE
}
